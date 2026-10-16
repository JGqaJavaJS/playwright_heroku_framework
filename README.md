# Playwright Heroku Framework

## 🎯 Purpose

This is a **framework demonstration project** built on Java 17 + Playwright + JUnit 5.
The main goal is **not** to cover all pages of [The Internet Herokuapp](https://the-internet.herokuapp.com), but to build a clean, scalable, and production-ready test automation framework with:

- Solid architecture (Page Object Model, separation of concerns)
- CI/CD integration (GitHub Actions)
- Reporting (Allure)
- Logging (SLF4J + Logback)
- Flexible configuration
- Parallel test execution support (ThreadLocal)

Only a couple of sample tests are included to demonstrate the framework's capabilities.

---

## 🛠 Tech Stack

| Layer              | Technology                          |
|--------------------|-------------------------------------|
| Language           | Java 17                             |
| Automation         | Playwright (Microsoft)              |
| Test Runner        | JUnit 5                             |
| Build Tool         | Maven                               |
| Reporting          | Allure                              |
| Logging            | SLF4J + Logback                     |
| Utilities          | Lombok                              |
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
│   │       ├── core/                # Core infrastructure
│   │       │   ├── manager/         # BrowserManager (ThreadLocal for parallel execution)
│   │       │   ├── factory/         # PageFactory (creates and caches page instances)
│   │       │   └── wrappers/        # Element/Action wrappers with built-in waits
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
│   │       ├── smoke/               # Smoke test suite
│   │       └── regression/          # Regression test suite
│   │
│   └── test/resources/
│       ├── config.properties        # Runtime configuration
│       └── logback.xml              # Logger configuration
│
├── pom.xml
├── .gitignore
└── README.md
```

### Key Components Explained:

**1. Core Layer** (`com/framework/core/`)  
Infrastructure that powers the framework:
- **`manager/`** — `BrowserManager` manages Playwright instance and browser lifecycle using ThreadLocal
- **`factory/`** — `PageFactory` creates page instances and caches them per thread (Singleton pattern)
- **`wrappers/`** — Wraps Playwright Locator with enhanced methods (click, type, wait) with built-in retry logic

**2. Pages Layer** (`com/framework/pages/`)  
Page Object Model implementation:
- **`base/`** — `BasePage` abstract class with common page methods (navigation, waits, assertions)
- **`[page_name]/`** — Concrete page classes extending `BasePage`

**3. Components Layer** (`com/framework/components/`)  
Reusable UI components that appear on multiple pages:
- Header, Footer, Navigation Menu, Search Bar
- Each component has its own class with element locators and actions

**4. Utils Layer** (`com/framework/utils/`)  
Helper utilities:
- `WaitUtils` — custom wait conditions
- `AssertUtils` — soft assertions, custom assertions
- `ScreenshotUtils` — screenshot capture on failure

**5. Listeners Layer** (`com/framework/listeners/`)  
Test execution listeners:
- Allure integration
- Logging
- Screenshot on test failure

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
3. *(no default — fail fast if required property is missing)*

Example:
```bash
mvn test -Dbrowser=firefox -Dheadless=true
```

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

Log files will be generated in `target/test-logs/` with names like:
- `smoke-20240115T143022.log`
- `regression-20240115T143022.log`
- `default-20240115T143022.log` *(if `-DtestSuite` is not provided)*

---

## 📝 Logging

- **Console output** — formatted with timestamp, thread, level, and logger name
- **File output** — unique log file per run (timestamp + test suite name)
- Log files location: `target/test-logs/`
- Configuration: `src/test/resources/logback.xml`

---

## 📊 Reporting (Planned)

Allure reporting will be integrated in the next steps:
- Screenshots on failure
- Test steps with attachments
- Environment info
- GitHub Actions artifact upload

---

## 🔄 CI/CD (Planned)

GitHub Actions pipeline will include:
- Run tests on push / pull request
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