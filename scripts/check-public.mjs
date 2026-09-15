import { execFileSync } from 'node:child_process'
import fs from 'node:fs'

// Scan publishable working-tree files, never print matching secret values.
const files = [...new Set(execFileSync('git', ['ls-files', '-co', '--exclude-standard', '-z'], { encoding: 'utf8' }).split('\0').filter(Boolean))]
const rules = [
  ['private key', /-----BEGIN (?:OPENSSH |RSA |EC )?PRIVATE KEY-----/],
  ['Google OAuth secret', /GOCSPX-[A-Za-z0-9_-]+/],
  ['private spreadsheet URL', /docs\.google\.com\/spreadsheets\/d\/[A-Za-z0-9_-]{20,}/],
  ['private network address', /\b192\.168\.\d{1,3}\.\d{1,3}\b/],
  ['service account key', /"private_key"\s*:\s*"[^"\s]+/],
]
const problems = []
for (const file of files) {
  if (!fs.existsSync(file)) continue
  if (fs.lstatSync(file).isSymbolicLink()) { problems.push(`${file}: symbolic links are not allowed in the public snapshot`); continue }
  if (/(^|\/)(credentials[^/]*\.json|y|y\.pub|\.env(?!\.example)[^/]*)$|\.(pem|p12)$/.test(file)) problems.push(`${file}: credential file`)
  const buffer = fs.readFileSync(file)
  if (buffer.includes(0)) continue
  const text = buffer.toString('utf8')
  for (const [label, pattern] of rules) if (pattern.test(text)) problems.push(`${file}: ${label}`)
}
if (problems.length) {
  console.error(problems.join('\n'))
  process.exit(1)
}
console.log('Public working-tree checks passed. This targeted check does not audit Git history or image contents.')
