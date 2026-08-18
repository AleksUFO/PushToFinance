# Security Policy

## Reporting a vulnerability

Please **do not** open a public issue for security vulnerabilities. Instead, report them privately by opening a [security advisory](https://github.com/<your-user>/PushToFinance/security/advisories/new) or contacting the maintainers directly.

We ask that you:

- Include a description of the vulnerability and the affected versions.
- Provide a minimal proof-of-concept where possible.
- Allow time for a fix to be released before public disclosure.

## What this project does with your data

- All data (transactions, categories, budgets, settings) is stored **locally** on your device.
- Your AI API key is stored locally and sent **only** to the AI provider you selected, as part of the category/store detection request.
- The only network calls made are: the AI provider API and the exchange-rate API (`open.er-api.com`).
- Nothing is ever uploaded to a server owned by this project.

## Security-relevant components

- `data/SettingsManager.kt` — local storage of the API key via DataStore
- `ai/AiClient.kt` — outbound calls to AI providers (key transmitted via header/URL)
- `notification/ListenerService.kt` — reads notification content to detect payments

## Supported versions

| Version | Supported |
|---------|-----------|
| 1.0.x   | ✅ |