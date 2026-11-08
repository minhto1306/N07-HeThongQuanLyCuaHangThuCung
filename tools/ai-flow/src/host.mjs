import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { Engine } from './engine.mjs';
import { json, save, redact, safePath } from './files.mjs';

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));
const hostPaths = project => ({
  root: safePath(project, '.ai-work/host'),
  heartbeat: safePath(project, '.ai-work/host/heartbeat.json'),
  requests: safePath(project, '.ai-work/host/requests'),
  responses: safePath(project, '.ai-work/host/responses')
});
export function hostAvailable(project) {
  const paths = hostPaths(project);
  if (!fs.existsSync(paths.heartbeat)) return false;
  const heartbeat = json(paths.heartbeat);
  return heartbeat.status === 'online' && Date.now() - Date.parse(heartbeat.at) < 10000;
}
export function handlerMap(engine) {
  return {
    worker_preflight: args => args.model ? engine.setModel(args.model) : engine.doctor(),
    worker_approve_plan: args => engine.approvePlan(args),
    worker_dispatch: args => engine.dispatch(args),
    worker_status: args => engine.status(args.taskId),
    worker_result: args => engine.result(args.taskId),
    worker_cancel: args => engine.cancel(args.taskId),
    worker_review: args => engine.review(args),
    worker_finish_smoke: args => engine.markLive(args.probeId, args.smokeId)
  };
}
export async function hostCall(project, name, args, timeoutMs = 90000) {
  if (!hostAvailable(project)) throw new Error('Worker host is offline. Start tools/ai-flow/Mo-Worker-Host.cmd in your own CMD.');
  const paths = hostPaths(project), id = crypto.randomUUID(), session = json(paths.heartbeat).session;
  const request = path.join(paths.requests, id + '.json');
  save(request, { id, session, name, args, createdAt: new Date().toISOString(), expiresAt: new Date(Date.now() + timeoutMs).toISOString() });
  const response = path.join(paths.responses, id + '.json');
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    if (fs.existsSync(response)) {
      const result = json(response);
      if (!result.ok) throw new Error(result.error);
      return result.result;
    }
    await sleep(150);
  }
  // Expired requests are never replayed in a later host session.
  throw new Error('Host response timeout; inspect worker_status before repeating an operation. Request id: ' + id);
}
export async function runHost(project) {
  const paths = hostPaths(project);
  if (hostAvailable(project)) throw new Error('Another worker host is already online for this project');
  fs.mkdirSync(paths.requests, { recursive: true }); fs.mkdirSync(paths.responses, { recursive: true });
  const engine = new Engine(project), handlers = handlerMap(engine), session = crypto.randomUUID();
  const pending = new Set(); let stopping = false;
  const beat = status => save(paths.heartbeat, { session, pid: process.pid, status, at: new Date().toISOString() });
  beat('online');
  const heartbeatTimer = setInterval(() => beat(stopping ? 'stopping' : 'online'), 2000);
  const stop = () => { stopping = true; };
  process.once('SIGINT', stop); process.once('SIGTERM', stop);
  process.stdout.write('MIO worker host ONLINE. Keep this CMD open. Ctrl+C stops the worker. No API key required.\n');
  try {
    while (!stopping) {
      for (const file of fs.readdirSync(paths.requests).filter(name => /^[a-f0-9-]{36}\.json$/.test(name))) {
        const response = path.join(paths.responses, file);
        if (pending.has(file) || fs.existsSync(response)) continue;
        const request = json(path.join(paths.requests, file));
        if (request.session !== session || Date.parse(request.expiresAt) < Date.now()) {
          save(response, { ok: false, error: 'Expired request or previous host session; not replayed' }); continue;
        }
        if (!handlers[request.name] || !request.args || typeof request.args !== 'object') {
          save(response, { ok: false, error: 'Unsupported host request' }); continue;
        }
        pending.add(file);
        Promise.resolve().then(() => handlers[request.name](request.args))
          .then(result => save(response, { ok: true, result }))
          .catch(error => save(response, { ok: false, error: redact(error.message) }))
          .finally(() => pending.delete(file));
      }
      await sleep(250);
    }
  } finally {
    clearInterval(heartbeatTimer); beat('stopping'); await engine.close(); beat('offline');
    process.removeListener('SIGINT', stop); process.removeListener('SIGTERM', stop);
  }
}
