import { test, expect } from '@playwright/test'
import fs from 'node:fs/promises'

test('sign in, find a synthetic report, read personnel, and download an export', async ({ page }) => {
  const failures = []
  page.on('pageerror', error => failures.push(error.message))
  await page.goto('/login')
  await page.getByPlaceholder('Email', { exact: true }).fill('admin@example.test')
  await page.getByPlaceholder('Password', { exact: true }).fill('demo-admin-password')
  await page.getByRole('button', { name: 'Login', exact: true }).click()
  await expect(page).toHaveURL('/')
  await page.goto('/search-report')
  await expect(page.locator('table')).toContainText('Demo North')
  if (process.env.CAPTURE_SCREENSHOTS === 'true') {
    await fs.mkdir('../docs/screenshots', { recursive: true })
    await page.screenshot({ path: '../docs/screenshots/report-search.png', fullPage: true })
  }
  await page.locator('tr').filter({ hasText: 'Demo North' }).getByRole('link', { name: 'Open' }).click()
  await expect(page.getByRole('heading', { name: 'Daily Regional Report' })).toBeVisible()
  await expect(page.locator('.report-details')).toContainText('Demo Alpha')
  await expect(page.locator('.report-details')).toContainText('Example')
  if (process.env.CAPTURE_SCREENSHOTS === 'true') {
    await page.setViewportSize({ width: 1440, height: 1700 })
    await page.screenshot({ path: '../docs/screenshots/daily-report.png', fullPage: true })
  }
  await page.locator('.export-button').click()
  await expect(page.locator('.download-link a')).toBeVisible()
  const [download] = await Promise.all([
    page.waitForEvent('download'),
    page.locator('.download-link a').click(),
  ])
  const document = await fs.readFile(await download.path(), 'utf8')
  expect(document).toMatch(/^\{\\rtf/)
  expect(document).toContain('Demo North')
  expect(failures).toEqual([])
})
