# Publishing the public edition

## What was sanitized

- Removed the bundled Google service-account credential and the frontend environment file.
- Removed hardcoded OAuth/JWT secrets, the fixed private administrator identity, and password logging.
- Replaced private spreadsheet IDs and deployment details with external configuration.
- Replaced operational export text with synthetic templates and replaced original images with screenshots of fictional fixtures.
- Kept local originals under ignored `.local/private/` where applicable. Untracked SSH key files are ignored and must never be added to a public repository.

## Original Git history still needs care

The original repository contains credential-bearing historical commits. This working-tree change does not rewrite that history and cannot revoke credentials at Google or on existing deployments.

Before publishing, revoke/rotate any credential that was exposed: the Google service-account key, Google OAuth client secret, deployed JWT signing key, and any still-used bootstrap administrator password. Rotate SSH credentials too if those keys were ever shared or published. Check existing logs for the previously recorded administrator password.

GitHub recommends revoking/rotating exposed credentials before history cleanup: [Removing sensitive data from a repository](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository).

## Recommended publication path: a new public snapshot

After reviewing changes and running the checks:

```sh
node scripts/check-public.mjs
node scripts/export-public.mjs
```

The output is `.local/public-release/WorkReportPlus`. It contains the reviewed current source, tests, documentation, and CI; it excludes `.git`, ignored secrets, local caches, build output, and original images. Copy **that directory** to a separate location and initialize a new repository there. Do not copy the original `.git` directory or push the original branches/tags.

If preserving historical commits is essential, perform a coordinated full-history sanitization and audit separately. A revert or a new deletion commit leaves previous secrets accessible. Existing clones and forks may retain the old objects.

## Review boundary

`check-public.mjs` is a targeted guard for key files, private-key headers, Google OAuth secrets, private spreadsheet URLs, and local network addresses. It does not prove that arbitrary prose, binaries, or all possible secret formats are safe. Review the snapshot and its screenshots before publishing. Public demo credentials are intentionally documented and are for loopback-only use.

Choose a license only after confirming the rights to distribute the code and included assets. No license or authorship claim has been added automatically.
