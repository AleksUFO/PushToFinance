<div align="center">

# PushToFinance

**Turn banking payment notifications into organized personal finance records — automatically.**

Captures payment push notifications from your banking apps, parses amount / currency / card / store, and lets you save them as transactions with AI-assisted category and store detection.

</div>

---

## ✨ Features

- 📲 **Notification capture** — listens to your banking app notifications via Android NotificationListenerService (no root required).
- 🔢 **Smart parsing** — detects amounts (`25.50 zł`, `12.99 EUR`, `$4.99`), currency, card (Visa / Mastercard / Google Pay / Blik) and store/merchant name.
- 🤖 **Multi-provider AI** — automatic category & store detection powered by any of these providers:
  - **Google Gemini**
  - **OpenAI (GPT)**
  - **DeepSeek**
  - **Kimi (Moonshot AI)**
  - **Anthropic Claude**
  - **Custom OpenAI-compatible endpoint** (Ollama, vLLM, LM Studio, etc.)
- 💳 **Payment methods** — cards (auto-detected from pushes or manual) and cash, each with its own balance.
- 🗂️ **Categories** — planned / spontaneous / other, with nested sub-categories and colors.
- 💰 **Budgets** — monthly, weekly, yearly or total, per category or global.
- 🪙 **Pockets** — temporary category restrictions for holidays, festivities, etc.
- 💱 **Multi-currency** — PLN / EUR / USD with automatic conversion via live exchange rates (with offline fallback).
- 📊 **Dashboard** — total balance, monthly spent/income, budget progress and category breakdown.
- 🔔 **Heads-up notifications** — save or discard a payment straight from the notification.
- 🧪 **Test push simulator** — send a synthetic push to verify the full pipeline.

## 🧱 Tech stack

| Layer      | Technology                                        |
|------------|---------------------------------------------------|
| UI         | Jetpack Compose + Material 3                      |
| Language   | Kotlin (2.1)                                       |
| Architecture | MVVM + Repository                               |
| Database   | Room (SQLite)                                      |
| Settings   | DataStore Preferences                              |
| Networking | OkHttp + kotlinx.serialization                    |
| Background | WorkManager (resurface pending captures)          |
| Min / target SDK | 26 / 36                                     |

## 🚀 Quick start

```bash
# 1. Clone
git clone https://github.com/<your-user>/PushToFinance.git
cd PushToFinance

# 2. Make sure the Android SDK is installed and set ANDROID_HOME
# 3. Build the debug APK
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

> On Windows use `gradlew.bat assembleDebug` instead of `./gradlew`.

## 🔐 API keys

AI keys are **never** bundled into the APK. You enter your key in the app (Settings → AI provider → API key) or during onboarding. Keys are stored locally in Android DataStore.

Each provider has a default model, but you can override the model and, for custom endpoints, the base URL:

| Provider | Default model |
|----------|---------------|
| Google Gemini | `gemini-2.0-flash` |
| OpenAI (GPT) | `gpt-4o-mini` |
| DeepSeek | `deepseek-chat` |
| Kimi (Moonshot) | `moonshot-v1-8k` |
| Anthropic Claude | `claude-3-5-haiku-latest` |
| Custom | *(you provide model + base URL)* |

## 📦 Building & releasing

Full build instructions for **Linux**, **Windows** and **macOS** (including how to create a signing keystore and a signed release APK) are in:

- [`docs/BUILDING.md`](docs/BUILDING.md) — step-by-step setup per OS
- [`.github/workflows/build.yml`](.github/workflows/build.yml) — CI builds the APK on all three OS automatically

## 🗂️ Project structure

```
app/src/main/java/com/pushtofinance/infinapp/
├── ai/             # Multi-provider AI client (Gemini, GPT, DeepSeek, Kimi, Claude, custom)
├── currency/       # Exchange-rate client + fallback rates
├── data/           # Room entities, DAOs, repository, DataStore settings
├── notification/   # Notification listener, push parser/processor, helpers
├── ui/             # Compose screens, view models, navigation, components
│   └── screens/    # Dashboard, Finance (methods/transactions/budgets/categories/pushes)
└── util/           # Formatting & time helpers
```

## 🧪 Testing

Send a simulated push from *Settings → Testing → Test push* to exercise the whole capture → parse → save pipeline without touching a real bank app.

## 🤝 Contributing

Contributions are welcome! Please read [`CONTRIBUTING.md`](CONTRIBUTING.md) first.

## 📄 License

This project is licensed under the **MIT License** — see [`LICENSE`](LICENSE).

---

<div align="center">
<sub>Made with ❤️ for privacy-first personal finance.</sub>
</div>