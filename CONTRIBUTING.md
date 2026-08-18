# Contributing to PushToFinance

Thanks for taking the time to contribute! 🎉

## Code of conduct

Be respectful and inclusive. Harassment, trolling and offensive behavior are not tolerated.

## How to contribute

1. **Fork** the repository and create a branch from `main`:
   ```bash
   git checkout -b feature/my-feature
   ```
2. Make your changes. Keep them focused and follow the existing code style.
3. Make sure the project builds:
   ```bash
   ./gradlew assembleDebug        # Linux / macOS
   gradlew.bat assembleDebug      # Windows
   ```
4. Commit with a clear message and push to your fork.
5. Open a pull request describing **what** changed and **why**, and how it was tested.

## What we're looking for

- Bug fixes and crash reports
- New AI providers (they follow the pattern in `app/src/main/java/com/pushtofinance/infinapp/ai/AiClient.kt`)
- Better push parsing for more bank apps / languages (see `notification/PushParser.kt`)
- UI/UX improvements, tests, docs

## Guidelines

- **Do not** commit API keys, keystores or any secrets. They are gitignored for a reason.
- Keep strings in the UI in English (this is an international open-source project).
- Prefer small, reviewable pull requests over large ones.
- If you change user-facing behavior, mention it in the PR description.

## Reporting issues

- Use the GitHub issue tracker.
- For crash reports, include the stack trace, Android version and device model.
- For parsing bugs, include the **exact** notification text (redact personal data).

## Questions?

Open a discussion or an issue — we're happy to help.