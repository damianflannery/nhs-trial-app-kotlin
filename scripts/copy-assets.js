const fs = require("fs");
const path = require("path");

const src = path.join(__dirname, "..", "node_modules", "nhsuk-frontend", "dist");
const dst = path.join(
  __dirname,
  "..",
  "src",
  "main",
  "resources",
  "static",
  "nhsuk"
);

function copyDir(from, to) {
  fs.mkdirSync(to, { recursive: true });
  for (const entry of fs.readdirSync(from, { withFileTypes: true })) {
    const s = path.join(from, entry.name);
    const d = path.join(to, entry.name);
    if (entry.isDirectory()) {
      copyDir(s, d);
    } else {
      fs.copyFileSync(s, d);
    }
  }
}

copyDir(src, dst);
console.log("NHS Design System assets copied to", dst);
