# Playwright Heroku Framework

## 🎯 Purpose

This is a **framework demonstration project** built on Java 17 + Playwright + TestNG + Maven.
The main goal is **not** to cover all pages of [The Internet Herokuapp](https://the-internet.herokuapp.com), 
but to build a clean, scalable, and production-ready web test automation framework with:

- Solid architecture (Page Object Model, separation of concerns)
- Thread-safe browser management (`ThreadLocal`)
- Reusable element wrappers and components
- CI/CD integration (GitHub Actions)
- Reporting (Allure)
- Logging (SLF4J + Logback)
- Flexible configuration
- Parallel test execution support

Only a couple of sample tests are included to demonstrate the framework's capabilities.

---

## 🛠 Tech Stack

| Layer              | Technology                          |
|--------------------|-------------------------------------|
| Language           | Java 17                             |
| Automation         | Playwright (Microsoft)              |
| Test Runner        | TestNG                              |
| Build Tool         | Maven                               |
| Reporting          | Allure                              |
| Logging            | SLF4J + Logback                     |
| CI/CD              | GitHub Actions *(planned)*          |

---

## 📁 Project Structure

```
playwright_heroku_framework/
├── src/
│   ├── main/java/
│   │   └── com/framework/
│   │       ├── config/              # Configuration reader (Singleton)
│   │       │
│   │       ├── dto/                 # Data transfer objects (UserCredentials)
│   │       │
│   │       ├── core/                # Core infrastructure
│   │       │   ├── manager/         # BrowserManager (ThreadLocal for parallel execution)
│   │       │   ├── factory/         # PageFactory (creates and caches page instances per thread)
│   │       │   └── wrappers/        # ElementWrapper (wraps Locator with logging and waits)
│   │       │
│   │       ├── pages/               # Page Objects
│   │       │   ├── base/            # BasePage (abstract class with common methods)
│   │       │   └── [page_name]/     # Concrete page classes
│   │       │
│   │       ├── components/          # Reusable UI components (header, footer, menu)
│   │       │
│   │       ├── utils/               # Utilities (waiters, assertions, screenshots)
│   │       │
│   │       └── listeners/           # Test listeners (Allure, logging, screenshot on failure)
│   │
│   ├── test/java/
│   │   └── com/tests/
│   │       ├── base/                # BaseTest (lifecycle hooks)
│   │       ├── smoke/               # Smoke test suite
│   │       └── regression/          # Regression test suite
│   │
│   └── test/resources/
│       ├── config.properties        # Runtime configuration
│       ├── logback.xml              # Logger configuration
│       └── META-INF/services/       # TestNG listener auto-registration
│           └── org.testng.ITestNGListener
│
├── pom.xml
├── .gitignore
└── README.md
```

### Key Components Explained:

**1. Core Layer** (`com/framework/core/`)
Infrastructure that powers the framework:
- **`manager/`** — `BrowserManager` manages Playwright instance and browser lifecycle using ThreadLocal
- **`factory/`** — `PageFactory` creates page instances and caches them per thread
- **`wrappers/`** — `ElementWrapper` wraps Playwright `Locator` with enhanced methods (click, type, wait) 
- and human-readable logging

**2. Pages Layer** (`com/framework/pages/`)
Page Object Model implementation:
- **`base/`** — `BasePage` abstract class with common page methods (navigation, waits, assertions)
- **`[page_name]/`** — Concrete page classes extending `BasePage`

**3. Components Layer** (`com/framework/components/`)
Reusable UI components that appear on multiple pages:
- Header, Footer, Navigation Menu
- Each component has its own class with element locators and actions

**4. Utils Layer** (`com/framework/utils/`)
Helper utilities:
- `WaitUtils` — custom wait conditions
- `AssertUtils` — assertions with logging
- `ScreenshotUtils` — screenshot capture and Allure attachments

**5. Listeners Layer** (`com/framework/listeners/`)
Test execution listeners:
- Allure integration
- Logging
- Screenshot on test failure

**6. Credentials Layer** (`com/framework/config/` and `com/framework/dto/`)
Manages sensitive test data separately from regular configuration:
- `CredentialsConfig` — reads `secrets.properties` and provides user credentials
- `UserCredentials` — DTO that holds arbitrary credential fields per user
- Sensitive values are never logged
- The active user is selected via the `current.user` property

---

## ⚙️ Configuration

All runtime settings are stored in `src/test/resources/config.properties`:

```properties
# Base URL for testing
baseUrl=https://the-internet.herokuapp.com

# Browser type: chromium, firefox, webkit
browser=chromium

# Run browser in headless mode (true/false)
headless=false
```

### Override via System Properties

Any property can be overridden at runtime via `-D` flag. Priority order:

1. System property (`-Dbrowser=firefox`)
2. `config.properties` file
Default values are defined in `config.properties`. They are used when no system property is provided.
The `browser` property is validated against allowed values: `chromium`, `firefox`, `webkit`. 
An invalid value causes the test run to fail immediately.


Example:
```bash
mvn clean test -Dbrowser=firefox -Dheadless=true
```

### Sensitive Data

Credentials and other sensitive test data are stored separately in `src/test/resources/secrets.properties`. This file should be added to `.gitignore` and never committed.

Example `secrets.properties`:
```properties
user.vasya.email=vasya@example.com
user.vasya.password=SuperSecretPassword
user.vasya.id=12345

user.petya.email=petya@example.com
user.petya.password=AnotherSecret
user.petya.id=67890
---

## 🚀 Running Tests

### Local run (default settings)
```bash
mvn clean test
```

### With custom test suite name (affects log file name)
```bash
mvn clean test -DtestSuite=smoke
mvn clean test -DtestSuite=regression
```

### Override configuration at runtime
```bash
mvn clean test -Dbrowser=firefox -Dheadless=true -DtestSuite=smoke
```

### Run only smoke tests
```bash
mvn clean test -Dtest=com.tests.smoke.*
```

Log files will be generated in `target/test-logs/` with names like:
- `smoke-2024-01-15_14-30-22.log`
- `regression-2024-01-15_14-30-22.log`
- `default-2024-01-15_14-30-22.log` *(if `-DtestSuite` is not provided)*

---

## 📝 Logging

- **Console output** — formatted with timestamp, thread, level, and logger name
- **File output** — unique log file per run (timestamp + test suite name)
- Log files location: `target/test-logs/`
- Configuration: `src/test/resources/logback.xml`

---

## 📊 Reporting

Allure reporting is configured:
- Screenshots on failure
- Test steps with attachments
- Environment info
- GitHub Actions artifact upload *(planned)*

### Generate and view report locally

1. Run tests to produce Allure results:
   ```bash
   mvn clean test
   ```

2. Generate report and open it in browser:
   ```bash
   mvn allure:serve
   ```

### Generate static report (for CI/CD)

```bash
mvn allure:report
```

The static HTML report will be generated in `target/site/allure-maven-plugin/`.
```

---

## 🔄 CI/CD (Planned)

GitHub Actions pipeline will include:
- Headless mode by default
- Allure report generation and publishing
- Log file artifacts

---

## 📋 Requirements

- Java 17+
- Maven 3.6+
- Playwright browsers (installed via):
  ```bash
  mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
  ```

---

## 👤 Author

Built as a framework demo project.
