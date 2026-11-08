import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { agyWorker, runProcess } from '../src/agy.mjs';
import { text, save } from '../src/files.mjs';
import { compileJava } from '../src/verify.mjs';

function temp(t) {
  const base = path.resolve(process.env.MIO_TEST_TMP || os.tmpdir()); fs.mkdirSync(base, { recursive: true });
  const root = fs.mkdtempSync(path.join(base, 'mio-protocol-'));
  t.after(() => {
    const resolved = fs.realpathSync(root);
    assert.equal(path.dirname(resolved), fs.realpathSync(base)); assert.ok(path.basename(resolved).startsWith('mio-protocol-'));
    fs.rmSync(resolved, { recursive: true, force: true });
  }); return root;
}
const envelope = { event: 'result', result: { status: 'SUCCESS', conversation_id: 'fixture', structured_output: {
  summary: 'source observed', observations: ['Java Swing'], blockers: [], files: []
} } };
test('adapter uses account CLI, denies tools, sends stdin JSON and parses documented result envelope', async t => {
  const root = temp(t);
  const result = await agyWorker({ config: { agy: 'agy.exe', model: 'gemini-3.8-flash', timeoutMs: 10000 }, prompt: 'fixture prompt', attemptDir: root,
    processRunner: async (command, args, options) => {
      assert.equal(command, 'agy.exe'); assert.equal(args.includes('--dangerously-skip-permissions'), false);
      assert.equal(args[args.indexOf('--model') + 1], 'gemini-3.8-flash');
      const settings = JSON.parse(args[args.indexOf('--settings') + 1]);
      for (const deny of ['command(*)', 'write_file(*)', 'read_file(*)', 'mcp(*)']) assert.ok(settings.permissions.deny.includes(deny));
      assert.equal(JSON.parse(options.stdin).message.content, 'fixture prompt');
      return { code: 0, stopped: null, stdout: JSON.stringify(envelope) + '\n', stderr: '' };
    } });
  assert.equal(result.conversationId, 'fixture'); assert.deepEqual(result.output.files, []);
});
test('adapter rejects unexpected tool execution even when result says SUCCESS', async t => {
  const root = temp(t);
  await assert.rejects(() => agyWorker({ config: { agy: 'agy.exe', model: 'gemini-3.8-flash', timeoutMs: 10000 }, prompt: 'fixture', attemptDir: root,
    processRunner: async () => ({ code: 0, stdout: JSON.stringify({ event: 'step_update', step_update: { step_type: 'tool' } }) + '\n' + JSON.stringify(envelope), stderr: '' }) }), /unexpected_tool_use/);
});
test('adapter classifies account/quota failures and does not retry', async t => {
  const root = temp(t); let calls = 0;
  await assert.rejects(() => agyWorker({ config: { agy: 'agy.exe', model: 'gemini-3.8-flash', timeoutMs: 10000 }, prompt: 'fixture', attemptDir: root,
    processRunner: async () => { calls++; return { code: 1, stdout: '', stderr: 'Please sign in' }; } }), /auth_required/);
  assert.equal(calls, 1);
});
test('subprocess passes literal input without shell evaluation and captures UTF-8', async t => {
  const root = temp(t), script = path.join(root, 'echo.mjs');
  text(script, "process.stdin.setEncoding('utf8'); let s=''; for await (const c of process.stdin) s+=c; process.stdout.write(s);");
  const input = 'Tiếng Việt $(do-not-execute) `literal`';
  const result = await runProcess(process.execPath, [script], { cwd: root, stdin: input });
  assert.equal(result.code, 0); assert.equal(result.stdout, input);
});
test('subprocess cancellation terminates long-running worker', async t => {
  const root = temp(t), script = path.join(root, 'wait.mjs');
  text(script, 'setInterval(() => {}, 1000);');
  const controller = new AbortController();
  const pending = runProcess(process.execPath, [script], { cwd: root, signal: controller.signal, timeout: 10000 });
  setTimeout(() => controller.abort(), 200);
  const result = await pending; assert.equal(result.stopped, 'cancelled');
});
test('MCP stdio handshake, tool discovery, validation and missing-readiness gate', async t => {
  const root = temp(t);
  text(path.join(root, 'README.md'), 'fixture');
  save(path.join(root, 'tools/ai-flow/config.json'), { agy: process.execPath, model: null, timeoutMs: 10000, maxContextBytes: 500000 });
  const requests = [
    { jsonrpc: '2.0', id: 1, method: 'initialize', params: { protocolVersion: '2024-11-05', capabilities: {}, clientInfo: { name: 'test', version: '1' } } },
    { jsonrpc: '2.0', method: 'notifications/initialized' },
    { jsonrpc: '2.0', id: 2, method: 'tools/list' },
    { jsonrpc: '2.0', id: 3, method: 'tools/call', params: { name: 'worker_status', arguments: {} } },
    { jsonrpc: '2.0', id: 4, method: 'tools/call', params: { name: 'worker_dispatch', arguments: { planId: 42, taskId: 'x' } } }
  ];
  const cli = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../src/cli.mjs');
  const result = await runProcess(process.execPath, [cli, 'serve', '--project', root], { cwd: root, stdin: requests.map(x => JSON.stringify(x)).join('\n') + '\n' });
  assert.equal(result.code, 0, result.stderr);
  const replies = result.stdout.trim().split('\n').map(x => JSON.parse(x));
  assert.equal(replies.find(x => x.id === 1).result.serverInfo.name, 'mio-antigravity-worker');
  assert.equal(replies.find(x => x.id === 2).result.tools.length, 8);
  assert.deepEqual(JSON.parse(replies.find(x => x.id === 3).result.content[0].text), []);
  assert.equal(replies.find(x => x.id === 4).result.isError, true);
});
test('Java verification overlays proposed source without copying or modifying original files', async t => {
  const root = temp(t), original = path.join(root, 'original'), candidate = path.join(root, 'candidate');
  fs.mkdirSync(candidate, { recursive: true });
  text(path.join(original, 'src/A.java'), 'class A { B value; }');
  text(path.join(original, 'src/B.java'), 'class B { invalid original code }');
  text(path.join(candidate, 'src/B.java'), 'class B { int value = 1; }');
  const result = await compileJava(candidate, path.join(root, 'verification'), { originalRoot: original, deleted: [] });
  assert.equal(result.passed, true);
  assert.equal(result.fileCount, 2);
  assert.equal(fs.readFileSync(path.join(original, 'src/B.java'), 'utf8'), 'class B { invalid original code }');
  assert.equal(fs.existsSync(path.join(candidate, 'src/A.java')), false);
});
