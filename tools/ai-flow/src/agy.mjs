import { spawn, spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import { text, redact } from './files.mjs';

export const workerSchema = {
  type: 'object', additionalProperties: false,
  properties: {
    summary: { type: 'string' },
    observations: { type: 'array', items: { type: 'string' } },
    blockers: { type: 'array', items: { type: 'string' } },
    files: { type: 'array', items: {
      type: 'object', additionalProperties: false,
      properties: { path: { type: 'string' }, content: { type: 'string' }, delete: { type: 'boolean' } },
      required: ['path', 'content', 'delete']
    } }
  }, required: ['summary', 'observations', 'blockers', 'files']
};
export function accountEnv() {
  const env = { ...process.env };
  // Account-only bridge: do not inherit API-key/Vertex/ADC billing routes.
  for (const key of Object.keys(env)) {
    if (/^(GEMINI_API_KEY|GOOGLE_API_KEY|OPENAI_API_KEY|ANTHROPIC_API_KEY|GOOGLE_APPLICATION_CREDENTIALS|GOOGLE_GENAI_USE_VERTEXAI|GOOGLE_CLOUD_PROJECT|GOOGLE_CLOUD_LOCATION)$/i.test(key)) delete env[key];
  }
  return env;
}
export function runProcess(command, args, { cwd, stdin = '', timeout = 120000, signal, limit = 8_000_000 } = {}) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, { cwd, env: accountEnv(), windowsHide: true, shell: false,
      detached: process.platform !== 'win32', stdio: ['pipe', 'pipe', 'pipe'] });
    let stdout = '', stderr = '', stopped = null;
    const kill = reason => {
      stopped = reason;
      if (!child.pid) return;
      if (process.platform === 'win32') {
        spawnSync('taskkill.exe', ['/PID', String(child.pid), '/T', '/F'], { windowsHide: true, stdio: 'ignore', timeout: 3000 });
        // Some restricted Windows hosts deny taskkill/WMI even for the current job.
        // The direct child handle still permits termination of the worker itself.
        child.kill('SIGKILL');
      }
      else { try { process.kill(-child.pid, 'SIGKILL'); } catch {} }
    };
    const onAbort = () => kill('cancelled');
    signal?.addEventListener('abort', onAbort, { once: true });
    const timer = setTimeout(() => kill('timeout'), timeout);
    const receive = stream => chunk => {
      if (stream === 'out') stdout += chunk; else stderr += chunk;
      if (stdout.length + stderr.length > limit) kill('output_limit');
    };
    child.stdout.setEncoding('utf8').on('data', receive('out'));
    child.stderr.setEncoding('utf8').on('data', receive('err'));
    const cleanup = () => { clearTimeout(timer); signal?.removeEventListener('abort', onAbort); };
    child.on('error', error => { cleanup(); reject(error); });
    child.on('close', code => { cleanup(); resolve({ code, stdout, stderr, stopped }); });
    child.stdin.on('error', () => {});
    child.stdin.end(stdin);
    if (signal?.aborted) onAbort();
  });
}
export function classify(output) {
  if (/sign in|not logged|authentication required|unauthenticated/i.test(output)) return 'auth_required';
  if (/quota|resource.exhausted|rate.limit|429/i.test(output)) return 'quota_exhausted';
  if (/model.*(?:unavailable|not found|unsupported|invalid)/i.test(output)) return 'model_unavailable';
  return 'worker_failed';
}
export async function listModels(config, project) {
  const result = await runProcess(config.agy, ['models'], { cwd: project, timeout: 45000, limit: 1_000_000 });
  const combined = result.stdout + '\n' + result.stderr;
  const modelIds = [...new Set(combined.match(/\bgemini-[a-z0-9][a-z0-9._-]*/gi) || [])];
  return { ok: result.code === 0 && !result.stopped && modelIds.length > 0,
    modelIds, reason: result.stopped || (result.code === 0 ? 'no_model_ids' : classify(combined)),
    message: redact(combined.split('\n').filter(x => !/^[IEW]\d{4}|^ERROR: logging/.test(x)).join('\n')).slice(-12000) };
}
export async function agyWorker({ config, prompt, attemptDir, signal, processRunner = runProcess }) {
  // A fresh empty working directory prevents loading unrelated project rules/plugins.
  const cwd = path.join(attemptDir, 'worker');
  fs.mkdirSync(cwd, { recursive: true });
  const schemaPath = path.join(attemptDir, 'response-schema.json');
  text(schemaPath, JSON.stringify(workerSchema));
  const settings = JSON.stringify({ toolPermission: 'request-review',
    permissions: { allow: [], ask: [], deny: ['read_file(*)', 'write_file(*)', 'command(*)', 'unsandboxed(*)', 'mcp(*)', 'read_url(*)', 'execute_url(*)'] } });
  const result = await processRunner(config.agy, [
    '--model', config.model, '--disable-slash-commands',
    '--settings', settings, '--input-format', 'stream-json', '--output-format', 'stream-json',
    '--json-schema', schemaPath
  ], { cwd, stdin: JSON.stringify({ event: 'user', message: { content: prompt } }) + '\n',
    timeout: config.timeoutMs, signal });
  // Persist only redacted model output; never copy CLI credential stores.
  text(path.join(attemptDir, 'worker.stdout.ndjson'), redact(result.stdout));
  text(path.join(attemptDir, 'worker.stderr.log'), redact(result.stderr));
  if (result.stopped) throw new Error(result.stopped);
  if (result.code !== 0) throw new Error(classify(result.stdout + result.stderr));
  const events = result.stdout.split(/\r?\n/).filter(Boolean).map(line => { try { return JSON.parse(line); } catch { return null; } }).filter(Boolean);
  // A denied or unexpected tool attempt invalidates the run; do not trust a final SUCCESS alone.
  // Antigravity represents schema-constrained final output as its internal finish tool.
  // It carries the answer, not a file/command/MCP operation; all other tool steps fail closed.
  if (events.some(e => (e.step_update?.step_type === 'tool' &&
      (e.step_update.tool_name !== 'finish' || e.step_update.tool_info?.name !== 'finish' || e.step_update.tool_info?.error)) ||
      e.step_update?.subagent_info)) throw new Error('unexpected_tool_use');
  const envelope = events.filter(e => e.event === 'result').at(-1)?.result;
  if (!envelope || envelope.status !== 'SUCCESS') throw new Error(classify(envelope?.error || result.stderr || 'missing_success_result'));
  const output = envelope.structured_output || JSON.parse(envelope.response);
  return { output, conversationId: envelope.conversation_id || null, usage: envelope.usage || null };
}
