# Playwright Java Test Framework

Production-ready test automation framework built with Playwright, TestNG, and Maven.
Features parallel test execution, thread-safe browser isolation, and comprehensive logging.

## 🏗️ Architecture

### Project Structure

    src/
    ├── main/
    │   └── java/
    │       └── com/framework/
    │           ├── config/                    # Configuration management
    │           │   ├── Config.java            # Main configuration (baseUrl, browser, headless)
    │           │   ├── CredentialsConfig.java # User credentials from secrets.properties
    │           │   └── UserCredentials.java   # User data object (DTO)
    │           │
    │           ├── core/
    │           │   └── manager/
    │           │       └── BrowserManager.java # Manages thread-isolated Playwright, Browser, Context, and Pages
    │           │                               # Ensures 100% parallel stability by avoiding shared browser race conditions
    │           │                               # Supports multiple tabs via page stack (Deque)
    │           │
    │           └── pages/
    │               └── base/
    │                   └── BasePage.java       # Abstract base class for all Page Objects
    │                                           # Wraps Playwright Locator with logging and auto-waits
    │                                           # Provides common methods: click, fill, isVisible, navigate, etc.
    │
    ├── test/
    │   ├── java/
    │   │   └── com/framework/
    │   │       ├── tests/                     # Test classes (extend BaseTest)
    │   │       │   ├── BaseTest.java          # Base class with @BeforeClass/@AfterClass hooks
    │   │       │   ├── WindowsTest.java       # Tests new tab/popup handling
    │   │       │   └── ...                    # Other test classes
    │   │       │
    │   │       └── pages/                     # Page Objects (extend BasePage)
    │   │           ├── LoginPage.java
    │   │           ├── WindowsPage.java
    │   │           └── ...
    │   │
    │   └── resources/
    │       ├── config.properties              # Main configuration (baseUrl, browser, headless)
    │       ├── secrets.properties             # User credentials (gitignored)
    │       ├── allure.properties              # Allure report configuration (results directory)
    │       ├── testng.xml                     # TestNG suite configuration
    │       └── logback.xml                    # Logging configuration
    │
    └── .github/
        └── workflows/
            └── playwright-tests.yml           # GitHub Actions CI/CD

### Key Features

#### 1. Thread-Safe Parallel Execution
- **Thread-Isolated Engines**: Each parallel thread gets its own isolated Playwright and Browser instance. 
- This completely prevents Node.js driver WebSocket race conditions and `Object doesn't exist` errors during parallel execution.
- **Isolated Contexts**: Each test class gets its own BrowserContext (like incognito mode) via ThreadLocal.
- **Page Stack**: Supports multiple tabs per test using `Deque<Page>` in ThreadLocal.
- **Auto-tracking**: New tabs/popups are automatically added to the stack via `page.onPopup()`.

#### 2. Configuration Management
- **config.properties**: Non-sensitive settings (baseUrl, browser type, headless mode)
- **secrets.properties**: Credentials (gitignored, never committed)
- **System properties override**: Pass -Dbrowser=firefox -Dheadless=true to override config
- **Fail-fast validation**: Invalid browser value throws exception immediately

#### 3. Page Object Pattern
- **BasePage**: Wraps Playwright Locator with logging and built-in waits
- **Auto-waits**: click() and fill() automatically wait for element to be ready
- **No manual sleeps**: Leverages Playwright's native auto-waiting (no Thread.sleep)
- **Flexible constructors**: Default constructor uses current thread's page, parameterized constructor for specific pages

#### 4. Logging & Reporting
- **SLF4J + Logback**: Structured logging with dynamic log file names per test suite
- **Action logging**: Every click, fill, navigation logged automatically
- **Thread-aware logs**: Each thread's actions clearly marked in logs

#### 5. Allure Reporting & Auto-Screenshots
- **Rich HTML Reports**: Generated via `mvn allure:report` or `mvn allure:serve`.
- **Smart Failure Screenshots**: Automatically captures and attaches screenshots on test failure.
- **Descriptive Naming**: Screenshots are named dynamically (`ClassName_MethodName_YYYY-MM-DD_HH-mm-ss`) to prevent overwrites during parallel runs.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+

### Installation

    git clone <repository-url>
    cd playwright-heroku-framework
    mvn clean install

### Run Tests

    # Run all tests (parallel execution as configured in testng.xml)
    mvn clean test

    # Run all tests, ignore failures, and generate Allure HTML report
    mvn clean test -Dmaven.test.failure.ignore=true allure:report

    # Open the generated Allure report in the default browser
    open target/site/allure-maven-plugin/index.html

    # Run a specific test class
    mvn test -Dtest=WindowsTest

    # Override browser and headless mode via system properties
    mvn test -Dbrowser=firefox -Dheadless=false

## 📋 Configuration

### config.properties

    baseUrl=https://the-internet.herokuapp.com
    browser=chromium
    headless=true

### secrets.properties (gitignored)

    user.vasya.email=vasya@example.com
    user.vasya.password=secret123

## 🔄 Test Lifecycle

1. **@BeforeSuite**: Initialize suite-level hooks (actual browser initialization is deferred to thread level).
2. **@BeforeClass**: Create an isolated Playwright, Browser, BrowserContext, and first Page specifically for the current thread.
3. **@Test**: Execute test (can open multiple tabs, all tracked in the thread's page stack).
4. **@AfterMethod**: (If failed) Automatically capture and attach a timestamped screenshot to Allure.
5. **@AfterClass**: Close all pages, context, browser, and Playwright instances for the current thread, then clear ThreadLocal storage to prevent memory leaks.

## 📦 Dependencies

- **Playwright**: Browser automation
- **TestNG**: Test runner with parallel execution
- **SLF4J + Logback**: Logging
- **Maven Surefire**: Test execution plugin

## 🔧 CI/CD

GitHub Actions workflow runs tests on every push:
- Installs Java 17
- Caches Maven dependencies
- Installs Playwright browsers
- Runs tests in headless mode with 4 threads