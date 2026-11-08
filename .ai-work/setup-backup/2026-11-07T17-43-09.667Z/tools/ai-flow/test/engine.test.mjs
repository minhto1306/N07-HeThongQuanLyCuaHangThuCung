import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { Engine } from '../src/engine.mjs';
import { json, save, text, hash, safePath, redact } from '../src/files.mjs';
import { accountEnv } from '../src/agy.mjs';

function fixture(t, runner) {
  const tempBase = path.resolve(process.env.MIO_TEST_TMP || os.tmpdir());
  fs.mkdirSync(tempBase, { recursive: true });
  const root = fs.mkdtempSync(path.join(tempBase, 'mio-flow-test-'));
  t.after(() => {
    const resolved = fs.realpathSync(root);
    assert.equal(path.dirname(resolved), fs.realpathSync(tempBase));
    assert.ok(path.basename(resolved).startsWith('mio-flow-test-'));
    fs.rmSync(resolved, { recursive: true, force: true });
  });
  text(path.join(root, 'docs', 'sample.md'), 'before\n');
  text(path.join(root, 'README.md'), 'Java Swing -> BUS -> DAO\n');
  save(path.join(root, 'tools', 'ai-flow', 'config.json'), { agy: process.execPath,
    model: 'gemini-3.8-flash', timeoutMs: 10000, maxContextBytes: 500000 });
  const engine = new Engine(root, { runner: runner || (async () => ({ output: { summary: 'done', observations: [], blockers: [], files: [
    { path: 'docs/sample.md', content: 'after\n', delete: false }
  ] } })), models: async () => ({ ok: true, modelIds: ['gemini-3.8-flash'], message: 'fixture only' }) });
  save(path.join(root, '.ai-work', 'preflight.json'), { ready: true, liveValidated: true, configuredModel: 'gemini-3.8-flash', at: new Date().toISOString() });
  return engine;
}
function task(extra = {}) {
  return { id: 'task', goal: 'change marker', design: 'Only replace marker document', askWhen: 'missing context',
    steps: ['replace'], acceptance: ['after'], allowedFiles: ['docs/sample.md'], contextFiles: ['README.md'],
    checks: [{ kind: 'contains', path: 'docs/sample.md', text: 'after' }], ...extra };
}
function approve(engine, tasks = [task()]) { engine.approvePlan({ id: 'plan', summary: 'fixture scope', approval: 'Test approval', tasks }); }
function start(engine, extra = {}) { return engine.dispatch({ planId: 'plan', taskId: 'task', ...extra }); }
function accept(engine) {
  return engine.review({ taskId: 'task', decision: 'accept', evidence: 'Read exact diff and verified marker', proposalDigest: engine.state('task').proposalDigest });
}

test('proposal is isolated, checks run, backup and non-Git diff exist; accept is idempotent-safe', async t => {
  const e = fixture(t); approve(e); start(e); await e.wait();
  assert.equal(e.state('task').status, 'awaiting_review');
  assert.equal(fs.readFileSync(path.join(e.root, 'docs/sample.md'), 'utf8'), 'before\n');
  assert.match(e.result('task').diff, /-before[\s\S]*\+after/);
  assert.equal(fs.readFileSync(path.join(e.taskDir('task'), 'attempt-1/backup/docs/sample.md'), 'utf8'), 'before\n');
  assert.equal(accept(e).status, 'accepted');
  assert.equal(fs.readFileSync(path.join(e.root, 'docs/sample.md'), 'utf8'), 'after\n');
  assert.equal(start(e).status, 'accepted');
  assert.throws(() => accept(e), /not reviewable/);
});
test('no plan, modified plan, missing dependency and cyclic plan are rejected', t => {
  const e = fixture(t);
  assert.throws(() => start(e));
  assert.throws(() => approve(e, [task({ dependencies: ['missing'] })]), /Unknown/);
  assert.throws(() => approve(e, [task({ dependencies: ['second'] }), task({ id: 'second', dependencies: ['task'] })]), /cycle/);
  approve(e);
  const file = path.join(e.home, 'plans/plan.json'), data = json(file);
  data.tasks[0].goal = 'unapproved'; save(file, data);
  assert.throws(() => start(e), /modified/);
});
test('out-of-scope proposal never writes original or stages attacker path', async t => {
  const e = fixture(t, async () => ({ output: { summary: 'bad', observations: [], blockers: [], files: [
    { path: 'src/Other.java', content: 'bad', delete: false }
  ] } }));
  approve(e); start(e); await e.wait();
  assert.equal(e.state('task').error, 'out_of_scope_proposal');
  assert.equal(fs.existsSync(path.join(e.root, 'src/Other.java')), false);
});
test('two failed repair rounds lead to Codex takeover, not endless dispatch', async t => {
  const e = fixture(t); approve(e);
  for (let round = 0; round < 3; round++) {
    start(e); await e.wait();
    e.review({ taskId: 'task', decision: 'retry', evidence: 'Business acceptance still wrong, concrete reproduction' });
  }
  assert.equal(e.state('task').status, 'takeover');
  assert.equal(e.state('task').attempt, 3);
  assert.equal(start(e).status, 'takeover');
});
test('concurrent edit after worker finishes blocks apply and preserves user content', async t => {
  const e = fixture(t); approve(e); start(e); await e.wait();
  text(path.join(e.root, 'docs/sample.md'), 'USER EDIT');
  assert.throws(() => accept(e), /Source changed/);
  assert.equal(e.state('task').status, 'conflict');
  assert.equal(fs.readFileSync(path.join(e.root, 'docs/sample.md'), 'utf8'), 'USER EDIT');
});
test('external edits during run are detected even on an unrelated file', async t => {
  let finish;
  const e = fixture(t, () => new Promise(resolve => { finish = resolve; }));
  approve(e); start(e);
  text(path.join(e.root, 'README.md'), 'external edit');
  finish({ output: { summary: 'none', observations: [], blockers: [], files: [] } }); await e.wait();
  assert.equal(e.state('task').status, 'conflict');
  assert.deepEqual(e.state('task').sourceChanges, ['README.md']);
});
test('only one worker, cancellation persists and does not integrate proposals', async t => {
  const e = fixture(t, ({ signal }) => new Promise((resolve, reject) => signal.addEventListener('abort', () => reject(new Error('cancelled')))));
  approve(e); start(e);
  assert.throws(() => start(e), /One worker/);
  await e.cancel('task');
  assert.equal(e.state('task').status, 'cancelled');
  assert.equal(fs.readFileSync(path.join(e.root, 'docs/sample.md'), 'utf8'), 'before\n');
  assert.equal(start(e).status, 'cancelled');
});
test('quota failure invalidates readiness and preserves task; no automatic retry or API fallback', async t => {
  const e = fixture(t, async () => { throw new Error('quota_exhausted'); });
  approve(e); start(e); await e.wait();
  assert.equal(e.state('task').status, 'blocked');
  assert.equal(json(path.join(e.home, 'preflight.json')).ready, false);
  assert.equal(start(e).attempt, 1);
  assert.throws(() => start(e, { resume: true }), /doctor/);
});
test('restart reports interrupted worker, never replays without explicit resume', t => {
  const e = fixture(t); approve(e);
  save(path.join(e.taskDir('task'), 'state.json'), { id: 'task', planId: 'plan', planDigest: json(path.join(e.home, 'plans/plan.json')).digest,
    status: 'running', ownerPid: 2147483647, attempt: 1, repairs: 0 });
  assert.equal(e.status('task')[0].status, 'interrupted');
  assert.equal(start(e).attempt, 1);
});
test('tampered result cannot bypass verified proposal', async t => {
  const e = fixture(t); approve(e); start(e); await e.wait();
  const file = path.join(e.taskDir('task'), 'attempt-1/result.json'), data = json(file);
  data.output.files[0].content = 'tampered'; save(file, data);
  assert.throws(() => accept(e), /modified/);
});
test('failed check cannot be accepted; manual checks remain explicitly unverified', async t => {
  const e = fixture(t); approve(e, [task({ checks: [{ kind: 'contains', path: 'docs/sample.md', text: 'absent' }, { kind: 'manual', description: 'UI actual test' }] })]);
  start(e); await e.wait();
  assert.equal(e.state('task').checks[0].passed, false);
  assert.equal(e.state('task').checks[1].passed, null);
  assert.throws(() => accept(e), /checks failed/);
});
test('path traversal, Windows aliases, protected edits and SQL without approval are rejected', t => {
  const e = fixture(t);
  for (const file of ['../escape', 'C:/escape', 'docs/../escape', 'docs/NUL.md', 'docs/a.', 'docs/x:stream', 'docs\\x']) assert.throws(() => safePath(e.root, file));
  for (const file of ['config/database.properties', '.git/config', '.codex/config.toml', 'src/utils/password.java']) assert.throws(() => approve(e, [task({ allowedFiles: [file] })]));
  assert.throws(() => approve(e, [task({ allowedFiles: ['sql/change.sql'] })]), /sqlApproved/);
});
test('production dispatch blocked until live validation and smoke bypass cannot edit source', t => {
  const e = fixture(t);
  const file = path.join(e.home, 'preflight.json'), p = json(file); p.liveValidated = false; save(file, p);
  approve(e);
  assert.throws(() => start(e), /Live probe/);
  const e2 = fixture(t); const p2 = json(path.join(e2.home, 'preflight.json')); p2.liveValidated = false; save(path.join(e2.home, 'preflight.json'), p2);
  approve(e2, [task({ setupSmoke: true })]);
  assert.throws(() => start(e2), /setupSmoke/);
});
test('context is never silently truncated', t => {
  const e = fixture(t); const config = e.config(); config.maxContextBytes = 5; save(e.configPath, config);
  approve(e); assert.throws(() => start(e), /budget exceeded/);
  assert.equal(e.all().length, 0);
});
test('doctor does not invent Flash availability, model choice must match account listing', async t => {
  const e = fixture(t);
  await assert.rejects(() => e.setModel('gemini-fiction-flash'), /not confirmed/);
  await assert.rejects(() => e.setModel('another-provider'), /exact Flash/);
  assert.equal((await e.doctor()).ready, true);
});
test('sensitive values redacted and paid API env keys removed from worker', t => {
  assert.equal(redact('password="abc"\napi_key=xyz'), 'password="[REDACTED]"\napi_key=[REDACTED]');
  const original = process.env.GEMINI_API_KEY; process.env.GEMINI_API_KEY = 'fixture-secret';
  t.after(() => { if (original === undefined) delete process.env.GEMINI_API_KEY; else process.env.GEMINI_API_KEY = original; });
  assert.equal(accountEnv().GEMINI_API_KEY, undefined);
});
