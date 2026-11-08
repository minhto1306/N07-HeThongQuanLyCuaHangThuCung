import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

export const hash = value => crypto.createHash('sha256').update(value).digest('hex');
export const json = file => JSON.parse(fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''));
export function save(file, data) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  const temporary = `${file}.${crypto.randomUUID()}.tmp`;
  fs.writeFileSync(temporary, JSON.stringify(data, null, 2) + '\n', { mode: 0o600 });
  fs.renameSync(temporary, file);
}
export function text(file, value) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, value, { mode: 0o600 });
}
export function relativeName(name) {
  if (typeof name !== 'string' || !name || name.includes('\\') || name.includes(':') ||
      name.startsWith('/') || /[\x00-\x1f]/.test(name) ||
      name.split('/').some(x => !x || x === '.' || x === '..' || /[. ]$/.test(x) ||
        /^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\.|$)/i.test(x))) {
    throw new Error('Invalid relative file path: ' + name);
  }
  return name;
}
export function safePath(root, name) {
  relativeName(name);
  const base = fs.realpathSync(root);
  let current = base;
  for (const part of name.split('/')) {
    current = path.join(current, part);
    if (fs.existsSync(current) && fs.lstatSync(current).isSymbolicLink()) throw new Error('Symlink/reparse path rejected: ' + name);
  }
  return current;
}
export function digest(root, name) {
  const file = safePath(root, name);
  return fs.existsSync(file) ? hash(fs.readFileSync(file)) : null;
}
export function permitted(name) {
  relativeName(name);
  if (!/^(src\/.*\.java|docs\/.*\.(md|json|txt)|resources\/.*\.(json|txt|xml|properties)|sql\/.*\.sql)$/.test(name)) {
    throw new Error('File outside editable source/document allowlist: ' + name);
  }
  if (/(^|\/)(\.git|\.codex|\.ai-work|\.agent|\.agents|config)(\/|$)/i.test(name) ||
      /(?:secret|credential|password|token|\.env)/i.test(name)) throw new Error('Protected file: ' + name);
}
export function redact(value) {
  return String(value)
    .replace(/("?(?:password|passwd|secret|api[_-]?key|access[_-]?token|refresh[_-]?token|thinkingSignature)"?\s*[:=]\s*)("[^"\r\n]*"|'[^'\r\n]*'|[^\s;,}\r\n]+)/gi,
      (_, prefix, value) => prefix + (value.startsWith('"') ? '"[REDACTED]"' : value.startsWith("'") ? "'[REDACTED]'" : '[REDACTED]'))
    .replace(/\b(?:AIza[\w-]{20,}|ya29\.[\w.-]+|sk-[\w-]{16,})\b/g, '[REDACTED]')
    .replace(/(Authorization\s*:\s*Bearer\s+)\S+/gi, '$1[REDACTED]');
}
export function readText(root, name, max = 400000) {
  const file = safePath(root, name);
  const data = fs.readFileSync(file);
  if (data.length > max || data.includes(0)) throw new Error('Context file too large or binary; split explicitly: ' + name);
  return data.toString('utf8');
}
export function inventory(root, prefix = '') {
  const found = {};
  for (const entry of fs.readdirSync(path.join(root, prefix), { withFileTypes: true })) {
    if (['.git', '.ai-work', 'node_modules', 'bin', 'target', 'build', '.metadata'].includes(entry.name)) continue;
    const name = prefix ? `${prefix}/${entry.name}` : entry.name;
    if (entry.isSymbolicLink()) continue;
    if (entry.isDirectory()) Object.assign(found, inventory(root, name));
    else if (entry.isFile()) found[name] = digest(root, name);
  }
  return found;
}
export function changed(a, b) {
  return [...new Set([...Object.keys(a), ...Object.keys(b)])].filter(k => a[k] !== b[k]);
}
// Valid unified replacement hunks, generated directly from bytes rather than Git.
export function diff(name, before, after) {
  if (before === after) return '';
  const lines = value => value === null || value === '' ? [] : value.replace(/\r\n/g, '\n').split('\n');
  const a = lines(before), b = lines(after);
  return `--- ${before === null ? '/dev/null' : 'a/' + name}\n+++ ${after === null ? '/dev/null' : 'b/' + name}\n@@ -${a.length ? 1 : 0},${a.length} +${b.length ? 1 : 0},${b.length} @@\n` +
    a.map(x => '-' + x + '\n').join('') + b.map(x => '+' + x + '\n').join('');
}
