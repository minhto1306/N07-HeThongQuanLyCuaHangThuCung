import fs from 'node:fs';

const resolutions = [
  { file: 'src/dao/HoaDonDAO.java', choices: ['upstream', 'stash'] },
  { file: 'src/gui/DoiTraHangPanel.java', choices: ['upstream'] },
];

for (const { file, choices } of resolutions) {
  const original = fs.readFileSync(file, 'utf8');
  let index = 0;
  const resolved = original.replace(
    /^<<<<<<< Updated upstream\r?\n([\s\S]*?)^=======\r?\n([\s\S]*?)^>>>>>>> Stashed changes\r?\n/gm,
    (_match, upstream, stash) => {
      const choice = choices[index++];
      if (!choice) throw new Error(`Unexpected conflict in ${file}`);
      return choice === 'upstream' ? upstream : stash;
    },
  );
  if (index !== choices.length || /^<<<<<<<|^=======|^>>>>>>>/m.test(resolved)) {
    throw new Error(`Unexpected conflict count or markers in ${file}: ${index}`);
  }
  fs.writeFileSync(file, resolved, 'utf8');
  console.log(`${file}: resolved ${index} conflict(s)`);
}
