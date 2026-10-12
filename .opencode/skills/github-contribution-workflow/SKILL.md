---
name: github-contribution-workflow
description: GitHub issues, gh CLI, pull requests, PR titles, Closes/Refs references, and signed releases for this repository; use before repository edits and when creating, reviewing, or publishing GitHub work.
---

# GitHub contribution workflow

- Before editing repository files, search existing GitHub issues for the work. Use a relevant open issue or create one first; closed issues are history, not active work items. Include the issue number in the branch name where practical.
- Use `gh issue list --state all --search "keywords"` to look for existing work. Follow the issue templates and keep issue/PR content in English.
- When using `gh` to create or edit an issue or PR, pass real newlines using a safely quoted multiline body or a temporary body file. Never use literal `\n` escapes as line breaks, and verify the posted body with `gh issue view NUMBER` or `gh pr view NUMBER`.
- PR titles must start with exactly one of `[FEATURE]`, `[BUGFIX]`, `[DOCS]`, or `[CHORE]`. Reference the earlier issue using `Closes #123` or `Refs #123`; `.github/workflows/pr-policy.yml` checks the category and that the issue predates the PR.
- Before opening a PR, run checks relevant to the change, review the final diff, and complete `.github/PULL_REQUEST_TEMPLATE.md`.
- Publish only through `.github/workflows/release.yml` with a new `vMAJOR.MINOR.PATCH` tag. Do not manually create releases or bypass signing, signature, or checksum verification. Confirm the workflow succeeded and both APK and checksum assets are present before reporting publication; report missing secrets or permissions as blockers.
