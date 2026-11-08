import fs from 'node:fs';
import path from 'node:path';
import { runProcess } from './agy.mjs';
import { readText, safePath, text, redact } from './files.mjs';

function javaFiles(root, prefix = 'src') {
  const base = safePath(root, prefix);
  if (!fs.existsSync(base)) return [];
  return fs.readdirSync(base, { withFileTypes: true }).flatMap(entry => {
    const name = `${prefix}/${entry.name}`;
    if (entry.isSymbolicLink()) throw new Error('Symlink in Java source');
    return entry.isDirectory() ? javaFiles(root, name) : name.endsWith('.java') ? [safePath(root, name)] : [];
  });
}
export async function compileJava(root, directory, overlay = null) {
  let files = javaFiles(root);
  if (overlay) {
    const originals = javaFiles(overlay.originalRoot).map(file => path.relative(overlay.originalRoot, file).replaceAll('\\', '/'));
    const proposed = files.map(file => path.relative(root, file).replaceAll('\\', '/'));
    files = [...new Set([...originals, ...proposed])].filter(name => !overlay.deleted.includes(name))
      .map(name => fs.existsSync(safePath(root, name)) ? safePath(root, name) : safePath(overlay.originalRoot, name));
  }
  if (!files.length) return { kind: 'java-compile', passed: false, message: 'No Java files' };
  fs.mkdirSync(directory, { recursive: true });
  const argfile = path.join(directory, 'javac.args');
  const classes = path.join(directory, 'classes');
  fs.mkdirSync(classes, { recursive: true });
  const quote = value => '"' + value.replaceAll('\\', '/') + '"';
  const libraryRoot = path.join(overlay?.originalRoot || root, 'lib');
  const classpath = fs.existsSync(libraryRoot) ? fs.readdirSync(libraryRoot).filter(name => name.endsWith('.jar'))
    .map(name => path.join(libraryRoot, name)).join(path.delimiter) : '';
  text(argfile, ['--release', '17', '-encoding', 'UTF-8', '-cp', quote(classpath || '.'),
    '-d', quote(classes), ...files.map(quote)].join('\n'));
  const result = await runProcess('javac', ['@' + argfile], { cwd: root, timeout: 120000 });
  text(path.join(directory, 'javac.log'), redact(result.stdout + result.stderr));
  return { kind: 'java-compile', passed: result.code === 0 && !result.stopped,
    fileCount: files.length, code: result.code, stopped: result.stopped,
    log: path.join(directory, 'javac.log') };
}
export function validateChecks(checks) {
  if (!Array.isArray(checks) || !checks.length) throw new Error('At least one acceptance check is required');
  for (const check of checks) {
    if (!check || !['java-compile', 'contains', 'json-parse', 'manual'].includes(check.kind)) throw new Error('Unsupported check kind');
    if (['contains', 'json-parse'].includes(check.kind) && typeof check.path !== 'string') throw new Error('Check needs a relative path');
    if (check.kind === 'contains' && (typeof check.text !== 'string' || !check.text)) throw new Error('Contains check needs text');
    if (check.kind === 'manual' && (typeof check.description !== 'string' || !check.description)) throw new Error('Manual check needs description');
  }
}
export async function verify(root, checks, directory, overlay = null) {
  const results = [];
  for (const check of checks) {
    try {
      const sourceRoot = check.path && overlay && !fs.existsSync(safePath(root, check.path)) && !overlay.deleted.includes(check.path) ? overlay.originalRoot : root;
      if (check.kind === 'java-compile') results.push(await compileJava(root, directory, overlay));
      if (check.kind === 'contains') results.push({ ...check, passed: readText(sourceRoot, check.path).includes(check.text) });
      if (check.kind === 'json-parse') { JSON.parse(readText(sourceRoot, check.path)); results.push({ ...check, passed: true }); }
      if (check.kind === 'manual') results.push({ ...check, passed: null, message: 'Codex must supply actual review evidence' });
    } catch (error) { results.push({ ...check, passed: false, message: redact(error.message) }); }
  }
  return results;
}
