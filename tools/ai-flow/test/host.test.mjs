import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { spawn } from 'node:child_process';
import { hostCall, hostAvailable } from '../src/host.mjs';
import { save, text, json } from '../src/files.mjs';

test('foreground host serves status/approval, rejects arbitrary commands and never replays old-session requests', async t => {
  const base = path.resolve(process.env.MIO_TEST_TMP || os.tmpdir()); fs.mkdirSync(base, { recursive: true });
  const root = fs.mkdtempSync(path.join(base, 'mio-host-'));
  text(path.join(root, 'README.md'), 'fixture');
  save(path.join(root, 'tools/ai-flow/config.json'), { agy: process.execPath, model: null, timeoutMs: 10000, maxContextBytes: 500000 });
  const cli = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../src/cli.mjs');
  const child = spawn(process.execPath, [cli, 'host', '--project', root], { windowsHide: true, stdio: ['pipe', 'pipe', 'pipe'] });
  let diagnostics = ''; child.stderr.setEncoding('utf8').on('data', chunk => diagnostics += chunk);
  t.after(async () => {
    child.kill('SIGKILL');
    await new Promise(resolve => child.exitCode !== null ? resolve() : child.once('close', resolve));
    const resolved = fs.realpathSync(root);
    assert.equal(path.dirname(resolved), fs.realpathSync(base)); assert.ok(path.basename(resolved).startsWith('mio-host-'));
    fs.rmSync(resolved, { recursive: true, force: true });
  });
  for (let i = 0; i < 100 && !hostAvailable(root); i++) await new Promise(resolve => setTimeout(resolve, 50));
  assert.equal(hostAvailable(root), true, diagnostics);
  assert.deepEqual(await hostCall(root, 'worker_status', {}), []);
  await assert.rejects(() => hostCall(root, 'run_shell', { command: 'anything' }), /Unsupported/);
  const oldId = '00000000-0000-0000-0000-000000000001';
  save(path.join(root, '.ai-work/host/requests', oldId + '.json'), { id: oldId, session: 'old-session',
    name: 'worker_status', args: {}, expiresAt: new Date(Date.now() + 60000).toISOString() });
  const response = path.join(root, '.ai-work/host/responses', oldId + '.json');
  for (let i = 0; i < 40 && !fs.existsSync(response); i++) await new Promise(resolve => setTimeout(resolve, 50));
  assert.equal(json(response).ok, false);
  assert.match(json(response).error, /not replayed/);
});
