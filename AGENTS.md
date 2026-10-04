# Repository instructions for coding agents

## Required issue, pull request, and release order

1. Before changing project files, inspect the repository guidance and search GitHub issues for an existing issue that covers the work. If none exists, create an issue first and use its number in the branch and pull request. Do not create a pull request first and add an issue afterward.
2. Keep each change focused on its issue. Use a branch name that includes the issue number where practical.
3. Before opening a pull request, run the relevant checks and review the final diff. Open the PR only after the issue exists. Start every PR title with exactly one category prefix: `[FEATURE]`, `[BUGFIX]`, `[DOCS]`, or `[CHORE]`. The PR body must reference the issue as `Closes #123` or `Refs #123` and summarize changes and verification.
4. Do not publish a release before the PR is merged and its required checks pass. Use a new `vMAJOR.MINOR.PATCH` tag only; do not manually create a GitHub release or bypass `.github/workflows/release.yml`. The tag workflow builds and signs the APK, verifies its signature and checksum, and attaches both files to the GitHub Release. Verify the workflow succeeded and the release assets are present before reporting completion.
5. Never claim that a release is published if only a tag or a draft/empty release exists. If required signing secrets or permissions are unavailable, report the blocker instead of bypassing the workflow.

## GitHub issue and pull request descriptions

- Write issue and PR titles, bodies, and comments as normal Markdown with actual line breaks. Never submit visible `\\n` escape sequences in place of line breaks.
- For multi-paragraph bodies, prefer writing the complete text to a temporary file and pass it with `gh issue create --body-file` or `gh pr create --body-file`. If using `--body`, supply a true multiline shell string and quote it safely; do not embed text in a way that lets the shell interpret apostrophes, backticks, dollar signs, or Markdown as shell syntax.
- After creating or editing an issue or PR, fetch its body back with `gh issue view` or `gh pr view` and confirm headings, paragraphs, lists, and code spans contain real line breaks and render as intended. Correct the body before reporting completion if formatting is wrong.

## Project-specific development checks

- Use the Gradle wrapper (`./gradlew`).
- Run `./gradlew lintDebug testDebugUnitTest assembleDebug assembleDebugAndroidTest` before opening a PR when applicable.
- Accessibility behavior changes should be tested against the relevant Android/Instagram flows when a device is available. Clearly state when device-level testing could not be done.
- Follow `CONTRIBUTING.md`, the issue templates, and `.github/PULL_REQUEST_TEMPLATE.md`. Keep their issue-first and PR-title requirements consistent with these instructions.
- Do not claim that an AccessibilityService change will prevent Android or Play Protect from warning about restricted settings; explain the capabilities it requests accurately.
