import fs from 'node:fs';
import crypto from 'node:crypto';
const sha=s=>crypto.createHash('sha256').update(s).digest('hex');
const task=process.argv[2], mode=process.argv[3]||'check';
if(!task || !['check','apply'].includes(mode)) throw Error('task and check|apply required');
const plan=JSON.parse(fs.readFileSync('.ai-work/agy-compact-plan.json','utf8'));
const def=plan.tasks.find(t=>t.id===task)||JSON.parse(fs.readFileSync('.ai-work/agy-batch-plan.json','utf8')).tasks.find(t=>t.id===task);if(!def)throw Error('Unknown task');
const artifact=def.allowedFiles[0];
const proposed=`.ai-work/tasks/${task}/attempt-1/proposed/${artifact}`;
const p=JSON.parse(fs.readFileSync(fs.existsSync(artifact)?artifact:proposed,'utf8'));
if(p.version!==1||!Array.isArray(p.changes)||!p.changes.length)throw Error('Bad artifact');
const originals=JSON.parse(fs.readFileSync('.ai-work/agy-core-plan.json','utf8')).tasks.concat(JSON.parse(fs.readFileSync('.ai-work/agy-simple-plan-v2.json','utf8')).tasks);
const original=task==='agy-patch-simple-batch-20261108'?{allowedFiles:originals.filter(t=>/reports|warehouse-validation|id-reservation|profile-points/.test(t.id)).flatMap(t=>t.allowedFiles)}:originals.find(t=>t.id===task.replace('agy-patch-','agy-fix-'));
const staged=[];
for(const c of p.changes){
 if(!original.allowedFiles.includes(c.path)||staged.some(x=>x.path===c.path))throw Error('Unexpected or duplicate path '+c.path);
 let next;
 if(c.baseSha256===null){if(fs.existsSync(c.path)||typeof c.content!=='string')throw Error('New file conflict');next=c.content;}
 else {
  const raw=fs.readFileSync(c.path);if(sha(raw)!==c.baseSha256)throw Error('Stale SHA '+c.path);
  const crlf=raw.toString('utf8').includes('\r\n');next=raw.toString('utf8').replaceAll('\r\n','\n');
  if(!Array.isArray(c.edits)||!c.edits.length)throw Error('No edits');
  for(const e of c.edits){
   if(!e.before||typeof e.after!=='string'||e.expectedCount!==1)throw Error('Invalid edit');
   const before=e.before.replaceAll('\r\n','\n'),after=e.after.replaceAll('\r\n','\n');
   if(next.split(before).length-1!==1)throw Error('Nonunique/missing match '+c.path+' '+before.slice(0,90));
   next=next.replace(before,()=>after);
  }
  if(crlf)next=next.replaceAll('\n','\r\n');
 }
 staged.push({path:c.path,next});
 console.log(c.path+' '+(c.edits?.length||0)+' exact edits; '+next.length+' resulting chars');
}
if(mode==='apply'){
 const state=JSON.parse(fs.readFileSync(`.ai-work/tasks/${task}/state.json`,'utf8'));
 if(state.status!=='accepted')throw Error('Artifact must be accepted before applying source changes');
 for(const c of staged){fs.mkdirSync(c.path.slice(0,c.path.lastIndexOf('/')),{recursive:true});fs.writeFileSync(c.path,c.next);}
 console.log('Applied reviewed source edits; compilation and regression still required.');
}
