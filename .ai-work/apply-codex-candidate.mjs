import fs from 'node:fs';
import crypto from 'node:crypto';
const sha=b=>crypto.createHash('sha256').update(b).digest('hex');
const manifest=JSON.parse(fs.readFileSync('.ai-work/candidate/manifest.json','utf8'));
for(const c of manifest){
 if(c.sha256===null){if(fs.existsSync(c.path))throw Error('New file conflict '+c.path);}
 else if(sha(fs.readFileSync(c.path))!==c.sha256)throw Error('Source changed '+c.path);
 if(!fs.existsSync('.ai-work/candidate/'+c.path))throw Error('Missing candidate');
}
if(process.argv[2]!=='apply')throw Error('Explicit apply required');
for(const c of manifest){fs.mkdirSync(c.path.slice(0,c.path.lastIndexOf('/')),{recursive:true});fs.copyFileSync('.ai-work/candidate/'+c.path,c.path);console.log(c.path);}
for(const name of ['20261108_DoiTraCoSo.sql','20261108_HotelSpa.sql']){
 const p='sql/'+name;if(fs.existsSync(p))throw Error('New migration conflict');fs.copyFileSync('.ai-work/candidate/'+p,p);
}
