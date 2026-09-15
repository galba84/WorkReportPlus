import { execFileSync } from 'node:child_process'
import fs from 'node:fs'
import path from 'node:path'

// Export reviewed working-tree files, excluding original Git history and ignored local secrets.
execFileSync(process.execPath, ['scripts/check-public.mjs'], { stdio: 'inherit' })
const destination = path.resolve('.local/public-release/WorkReportPlus')
if (fs.existsSync(destination)) throw new Error('Export directory already exists; archive or rename it before exporting again.')
const files = [...new Set(execFileSync('git', ['ls-files', '-co', '--exclude-standard', '-z'], { encoding: 'utf8' }).split('\0').filter(Boolean))]
for (const file of files) {
  if (!fs.existsSync(file)) continue
  const target = path.resolve(destination, file)
  if (!target.startsWith(destination + path.sep)) throw new Error('File escapes export directory')
  fs.mkdirSync(path.dirname(target), { recursive: true })
  fs.copyFileSync(file, target)
}
console.log(`Public snapshot created at ${destination}. It contains no .git directory.`)
