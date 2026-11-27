# BigCash

BigCash turns bank notifications into a clean, searchable local ledger. It captures every notification
from the bank apps you choose, stores the raw text verbatim, and runs a fully configurable cascade of
parsing rules to derive amount, direction, payment method, card digits and counterparty.

Everything stays on the device: no network permission, no analytics, no cloud.

## Highlights

- **Notification listener** for C6 Bank, Nubank and Mercado Pago, with a configurable package list.
- **Raw history first**: every captured notification is persisted before parsing, even when parsing fails.
- **Data-driven parsing engine**: regex, mask and keyword rules ordered by priority, per bank, with
  capture-group mapping for amount, card digits and counterparty.
- **Calibration panel**: raw history, full rule CRUD (create, edit, duplicate, reorder, enable, delete),
  a playground that shows what every rule extracted, a failure list, and a "reprocess history" action.
- **Dashboard** with combinable filters (bank, direction, method, period, amount range, parse status,
  tags, duplicates), full-text search, income/expense/balance totals and manual corrections.
- **Monthly summary** built from numbers and ranked lists (no charts).
- **App notifications** with user-editable templates for successful and failed parses.
- **CSV/JSON export**, **JSON import** and share-sheet delivery.
- **Liquid Glass UI**: translucent layered surfaces, drifting light blobs, hairline highlight borders,
  an intentional dark mode and haptic feedback on every meaningful action.

## Modules

```
:app               Compose UI, navigation, ViewModels, notification listener, repositories, export/import
:core:model        Pure Kotlin domain models, rule definitions, parsing configuration
:core:parsing      Pure Kotlin, KSP-free parsing engine + JUnit tests
:core:database     Room 3 entities, DAOs, converters and the Hilt database module
build-logic        Convention plugins copied from the claude-android-ninja skill
```

Dependency direction is one way: `:app` → `:core:*`. The parsing engine has no Android dependency, so the
rule cascade is unit tested on the JVM with real notification samples.

## Tech stack

| Concern        | Choice                                                        |
|----------------|---------------------------------------------------------------|
| Language       | Kotlin 2.3.21                                                 |
| UI             | Jetpack Compose + Material 3 (2026.06.01 BOM)                 |
| DI             | Hilt 2.60.1 (KSP)                                             |
| Persistence    | Room 3.0.3 with `BundledSQLiteDriver`                          |
| Async          | Coroutines + Flow                                             |
| Build          | AGP 9.3.1, Gradle 9.5.1 wrapper, version catalog, convention plugins |
| SDK            | `minSdk 26`, `compileSdk 37`, `targetSdk 37`                  |
| Tests          | JUnit 4 + Truth on `:core:parsing`                            |

## Build

```powershell
# debug build
.\gradlew.bat :app:assembleDebug

# unit tests for the parsing engine
.\gradlew.bat :core:parsing:test

# signed release build (creates app\build\outputs\apk\release\app-release.apk)
.\gradlew.bat :app:assembleRelease
```

`local.properties` must point at your SDK (`sdk.dir=...`). Release signing reads
`keystore.properties` at the repository root; when that file is absent the release build falls back to the
debug signing config so `assembleRelease` still produces an installable artifact.

## Install on a Samsung S25

1. Copy `app\build\outputs\apk\release\app-release.apk` to the phone (USB, Drive, or `adb install`).
2. Allow installing from that source if Android asks.
3. Open **BigCash**, follow onboarding and tap **Open notification access settings**.
4. Enable **BigCash notification capture**, then return to the app.
5. The dashboard header shows **Listener active** once access is granted.

```powershell
adb install -r app\build\outputs\apk\release\app-release.apk
```

## Keystore

The release keystore lives **outside version control**:

- `keystore\bigcash-release.jks` (generated locally with `keytool`, RSA 2048, 10 000 days)
- `keystore.properties` (store/key passwords, alias)

Both paths are ignored by git. To use your own key, replace the file and the four properties
(`storeFile`, `storePassword`, `keyAlias`, `keyPassword`). Never commit them.

## How the parsing pipeline works

1. `CashNotificationListenerService` receives a notification from a package that is in
   `monitored_packages` and enabled.
2. The raw title/text/big text/time/package/hash row is inserted into `raw_notifications`.
3. A SHA-256 hash of the content plus a configurable time window (`dedupe_window_millis`) drops
   re-posted notifications.
4. `ParsingEngine` sorts the active rules of that bank (plus `*` cross-bank rules) by priority and applies
   them in cascade:
   - `REGEX` — compiled as-is, capture groups are mapped through `amountGroup`, `cardGroup`,
      `counterpartyGroup`.
   - `MASK` — `#` matches a digit, `*` matches any run of characters, everything else is literal.
   - `KEYWORD` — comma/semicolon separated keywords, first hit wins.
5. The first rule that yields an amount wins and produces a `SUCCESS` transaction. A rule that matched but
   produced no amount becomes `PARTIAL`; no match at all is `FAILED` and lands in the failure list.
6. Direction and method come from the rule override, otherwise from the configurable keyword maps.
7. Every rule evaluation is recorded as a `RuleTrace`, which powers the playground and the audit view.

## Calibration panel

| Tab         | What it does                                                                                   |
|-------------|------------------------------------------------------------------------------------------------|
| Raw         | Every captured notification with package/status/search filters. Tap a card to load it into the playground. |
| Rules       | Create, edit, duplicate, enable/disable, raise/lower priority and delete rules. Every engine field is editable. |
| Playground  | Paste or load a real notification, pick a bank, run the cascade and inspect the winner plus the full rule trace. |
| Failures    | Only the notifications that matched no rule, ready to become new rules.                        |

The **Reprocess history** button replays every raw notification through the current rules, updates the raw
rows and inserts/updates the derived transactions. Settings → Parsing engine exposes every value the engine
reads (currency symbols, separators, fallback pattern, keyword maps, min/max amounts, dedupe window).

## Seeded data (all editable, nothing hardcoded)

Package names were verified against the official Google Play listings before being seeded:

| Bank          | Package                     | Source                                        |
|---------------|-----------------------------|-----------------------------------------------|
| Nubank        | `com.nu.production`         | Play Store listing for "Nu: Everyday mobile banking" |
| C6 Bank       | `com.c6bank.app`            | Play Store listing for "C6 Bank: Cartão, Conta e Mais!" |
| Mercado Pago  | `com.mercadopago.wallet`    | Play Store listing for "Mercado Pago: Conta Digital" |

Seed rules cover incoming/outgoing Pix, credit and debit card purchases (including card last digits),
boletos and a per-bank fallback plus a cross-bank wildcard. They are plain rows in `parsing_rules`: edit or
delete them from the Rules tab.

## Prerequisites and what was installed

Already present on the machine:

- JDK 17 (`C:\Program Files\Microsoft\jdk-17.0.21.102-hotspot`), `JAVA_HOME` already set
- Android SDK at `C:\Users\User\AppData\Local\Android\Sdk` with `platform-tools` and `platform 34`
- Git 2.55, GitHub CLI authenticated as `georg342`

Installed or configured during this build:

- Android SDK `platforms;android-37.0` and `build-tools;37.0.0` (via `sdkmanager`)
- Gradle 9.5.1 distribution used to generate the project wrapper (`gradlew`, `gradle-wrapper.jar`)
- Local release keystore `keystore\bigcash-release.jks` + `keystore.properties`
- `local.properties` pointing at the SDK, `keystore.properties`, `.tools\` and generated APKs are git-ignored

No interactive step was required. Everything else (AGP, Kotlin, Compose, Hilt, Room) is resolved from
Maven by the Gradle wrapper.

## Tests

```powershell
.\gradlew.bat :core:parsing:test
```

`ParsingEngineTest`, `RuleTypesTest` and `AmountParserTest` cover real wording from the three banks
(incoming Pix, credit/debit purchases, boleto keywords, wildcard fallback), rule precedence, disabled
rules, cross-bank isolation, pattern validation and Brazilian amount grouping.

## Poetic license (small additions beyond the brief)

- **Rule traces everywhere**: every parse stores the list of evaluated rules, so the playground shows what
  each regex/mask/keyword did instead of only the final result.
- **Visible listener state**: the dashboard header shows `Listener active`/`Listener disabled` and the
  settings screen turns the notification-access row into a shortcut.
- **Manual transactions safety net**: clearing history keeps rules and settings; exported files land in the
  cache and open the share sheet immediately.
- **Fallback signing**: a fresh clone without `keystore.properties` still builds an installable release APK
  using the debug key, instead of failing at `assembleRelease`.
- **Cross-bank rules**: a rule with bank key `*` applies to every monitored package, useful for shared
  formats such as "R$ 12,34" style amounts.
- **Duplicate hints**: transactions that share an amount within a three-window radius are flagged
  `possibleDuplicate` and can be filtered from the dashboard.

## Known limitations

- Notification text formats change often. The seeded rules are a starting point: use the playground and the
  failure list to adapt them. Nothing is immutable.
- Only notification access is used; there is no bank API or Open Finance integration.
- Import expects the JSON backup produced by this app (CSV is export only).
- The launcher icon is a simple vector placeholder; replace `res/drawable/ic_launcher_foreground.xml` for a
  branded asset.

## Version

`1.0.0` (versionCode 1). See `CHANGELOG.md`.
