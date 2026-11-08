import fs from 'node:fs';
import crypto from 'node:crypto';
const sha=s=>crypto.createHash('sha256').update(s).digest('hex');
const simple=JSON.parse(fs.readFileSync('.ai-work/agy-simple-plan-v2.json','utf8'));
const core=JSON.parse(fs.readFileSync('.ai-work/agy-core-plan.json','utf8'));
const old=[...simple.tasks.slice(1),...core.tasks];
const ids=new Map(old.map(t=>[t.id,t.id.replace('agy-fix-','agy-patch-')]));
const tasks=old.map(t=>{
 const id=ids.get(t.id), artifact=`docs/ai-context/patches/${id}.json`;
 const sources=[...new Set([...t.allowedFiles.filter(p=>fs.existsSync(p)),...t.contextFiles])];
 const hashes=t.allowedFiles.map(p=>`${p}: ${fs.existsSync(p)?sha(fs.readFileSync(p)): 'NEW FILE'}`).join('\n');
 return {...t,id,dependencies:(t.dependencies||[]).map(v=>ids.get(v)||v),allowedFiles:[artifact],contextFiles:sources,
  design:t.design+'\nOUTPUT EFFICIENCY: Do not return replacement source files. Return ONE allowed JSON artifact containing exact compact edit proposals. Codex will review, accept the artifact, then separately apply edits and compile/test. Artifact format {"version":1,"changes":[{"path":"src/...","baseSha256":"...","edits":[{"before":"EXACT UNIQUE existing text","after":"replacement","expectedCount":1}]}]}. New files use {"path":"sql/...","baseSha256":null,"content":"complete new file"}. Existing file matching is normalized LF, exact original substrings; include enough context for uniqueness. All edits in a file are applied in order. Only target the original allowed source paths below. No commands or executable patch tools. No claims of compilation or testing. Keep prose short.\nOriginal target hashes (raw bytes):\n'+hashes,
  steps:['Read provided source, design precise minimal edits','Return only the compact JSON artifact at '+artifact+' using the required outer worker files format'],
  checks:[{kind:'json-parse',path:artifact},{kind:'manual',description:'Codex reviews exact edits and source/hash guards before artifact acceptance. Actual compilation and DB regression follow separate application.'}]
 };
});
fs.writeFileSync('.ai-work/agy-compact-plan.json',JSON.stringify({id:'repair-20261108-compact-v1',summary:'Real AGY produces compact reviewed patch artifacts for authorized audit repairs; Codex applies and verifies each independently. Reduces repeated full-file generation.',approval:simple.approval,tasks},null,2));
for(const t of tasks) fs.writeFileSync(`.ai-work/dispatch-${t.id}.json`,JSON.stringify({planId:'repair-20261108-compact-v1',taskId:t.id}));
console.log(tasks.map(t=>t.id).join('\n'));
