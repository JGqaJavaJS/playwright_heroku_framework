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
    │           │       └── BrowserManager.java # Singleton managing Playwright, Browser, Context, Pages
    │           │                               # Uses ThreadLocal for test isolation
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
    │       ├── testng.xml                     # TestNG suite configuration
    │       └── logback.xml                    # Logging configuration
    │
    └── .github/
        └── workflows/
            └── playwright-tests.yml           # GitHub Actions CI/CD

### Key Features

#### 1. Thread-Safe Parallel Execution
- **Shared Browser**: Single browser instance for all tests (saves RAM and startup time)
- **Isolated Contexts**: Each test gets its own BrowserContext (like incognito mode) via ThreadLocal
- **Page Stack**: Supports multiple tabs per test using Deque<Page> in ThreadLocal
- **Auto-tracking**: New tabs/popups automatically added to stack via page.onPopup()

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

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+

### Installation

    git clone <repository-url>
    cd playwright-heroku-framework
    mvn clean install

### Run Tests

    # Run all tests (4 threads, headless)
    mvn test -Dthreads=4 -Dheadless=true

    # Run specific test class
    mvn test -Dtest=WindowsTest

    # Override browser
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

1. **@BeforeSuite**: Initialize shared Playwright and Browser (once)
2. **@BeforeClass**: Create isolated BrowserContext and first Page for test class
3. **@Test**: Execute test (can open multiple tabs, all tracked in stack)
4. **@AfterClass**: Close all pages, context, clear ThreadLocal
5. **@AfterSuite**: Close shared browser and Playwright engine

## 🎯 Planned Enhancements

- **Components**: Reusable UI components (header, footer, menu) - architecture placeholder
- **Utils**: Additional utilities (advanced waiters, custom assertions) - architecture placeholder
- **Allure Reports**: Rich test reporting with screenshots on failure
- **API Testing**: Extend framework to support REST API tests alongside UI tests

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