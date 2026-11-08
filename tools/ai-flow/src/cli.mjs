import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { Engine } from './engine.mjs';
import { json, save } from './files.mjs';
import { compileJava } from './verify.mjs';
import { serve } from './server.mjs';
import { runHost, hostCall } from './host.mjs';

const argumentsList = process.argv.slice(2);
const command = argumentsList.shift() || 'help';
let project = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..');
const index = argumentsList.indexOf('--project');
if (index !== -1) { project = path.resolve(argumentsList[index + 1]); argumentsList.splice(index, 2); }
const print = value => process.stdout.write(JSON.stringify(value, null, 2) + '\n');
try {
  if (command === 'serve') await serve(project);
  else if (command === 'host') await runHost(project);
  else if (command === 'host-call') print(await hostCall(project, argumentsList[0], argumentsList[1] ? json(path.resolve(argumentsList[1])) : {}));
  else if (command === 'help') print({ commands: {
    doctor: 'Check account and exact model availability', 'set-model ID': 'Choose exact Flash ID shown by doctor',
    'approve PLAN.json': 'Record explicit user approval and immutable task definitions',
    'dispatch PLAN_ID TASK_ID': 'Run a task until worker completion (MCP dispatch is asynchronous)',
    'resume PLAN_ID TASK_ID': 'Explicitly retry interrupted/blocked/cancelled infrastructure run',
    'status [TASK_ID]': 'Inspect state without replay', 'result TASK_ID': 'Read proposal and diff',
    'review REVIEW.json': 'Accept/retry/takeover with evidence and proposalDigest',
    'finish-smoke PROBE_ID SMOKE_ID': 'Mark live probes accepted and enable production dispatch',
    compile: 'Compile original Java to .ai-work/baseline-compile without running app or database',
    'serve': 'Serve MCP over newline-delimited stdio JSON-RPC',
    'host': 'Foreground account worker host; keep CMD open, Ctrl+C to stop',
    'host-call TOOL [ARGS.json]': 'Send one allowed operation to the foreground host'
  }, note: 'Run from installed tools/ai-flow, or supply --project ABSOLUTE_PATH. Never paste credentials into task files.' });
  else {
    const engine = new Engine(project);
    if (command === 'doctor') print(await engine.doctor());
    else if (command === 'set-model') print(await engine.setModel(argumentsList[0]));
    else if (command === 'approve') print(engine.approvePlan(json(path.resolve(argumentsList[0]))));
    else if (command === 'status') print(engine.status(argumentsList[0]));
    else if (command === 'result') print(engine.result(argumentsList[0]));
    else if (command === 'review') print(engine.review(json(path.resolve(argumentsList[0]))));
    else if (command === 'dispatch' || command === 'resume') {
      const state = engine.dispatch({ planId: argumentsList[0], taskId: argumentsList[1], resume: command === 'resume' });
      await engine.wait(); print(engine.state(state.id));
    } else if (command === 'finish-smoke') print(engine.markLive(argumentsList[0], argumentsList[1]));
    else if (command === 'compile') {
      const result = await compileJava(engine.root, path.join(engine.home, 'baseline-compile'));
      save(path.join(engine.home, 'baseline-compile', 'result.json'), result); print(result);
      if (!result.passed) process.exitCode = 1;
    } else throw new Error('Unknown command; use help');
  }
} catch (error) { process.stderr.write(error.message + '\n'); process.exitCode = 1; }
