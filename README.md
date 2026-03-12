# portless-spring-boot-starter

[![](https://jitpack.io/v/leesum-in/portless-spring-boot-starter.svg)](https://jitpack.io/#leesum-in/portless-spring-boot-starter)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring%20Boot-3.x-green)](https://spring.io/projects/spring-boot)
[![Coverage 92%](https://img.shields.io/badge/coverage-92%25-brightgreen)](build/reports/jacoco/test/html/index.html)

Spring Boot Starter for [Portless](https://github.com/vercel-labs/portless). Gives every Spring Boot app a stable `.localhost` URL — no port conflicts, no memorizing port numbers.

```
http://myapp.localhost:1355    # instead of http://localhost:8080
http://api.localhost:1355      # run multiple apps without port collisions
https://myapp.test             # custom TLD with HTTPS (portless 0.6+)
```

Works with IntelliJ debug mode. No wrapper script needed.

## The Problem

When running multiple Spring Boot apps locally, you deal with:

- **Port conflicts** — which app was `:8080` again?
- **Port memorization** — `api` on `:8081`, `admin` on `:8082`, `gateway` on `:8083`...
- **Cookie/storage clashes** — everything is `localhost`, just different ports

[Portless](https://github.com/vercel-labs/portless) solves this with named `.localhost` URLs, but only works as a CLI wrapper (`portless run gradle bootRun`). That means no IntelliJ debugger, no Spring Boot DevTools reload, no run configurations.

**This starter fixes that.** Drop it in as a dependency and your app automatically registers with the Portless proxy on startup — from any launch method.

## Quick Start

### 1. Install Portless CLI

```bash
npm install -g portless@latest
```

### 2. Add the dependency

Add the JitPack repository and the dependency.

**Gradle (Kotlin DSL)**

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.leesum-in:portless-spring-boot-starter:0.1.0")
}
```

**Gradle (Groovy)**

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.leesum-in:portless-spring-boot-starter:0.1.0'
}
```

**Maven**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.leesum-in</groupId>
    <artifactId>portless-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 3. Set the app name

```yaml
# application.yml
spring:
  application:
    name: myapp
```

Or use a dedicated property:

```yaml
portless:
  name: myapp
```

### 4. Run your app

```bash
./gradlew bootRun
```

```
Portless: assigned port 8237 for http://myapp.localhost
```

Open [http://myapp.localhost:1355](http://myapp.localhost:1355) — done.

## How It Works

```
App starts
  │
  ├─ EnvironmentPostProcessor
  │    ├─ Detect portless proxy (or start it automatically)
  │    ├─ Pick a random port (8000–8999)
  │    └─ Inject server.port + server.address
  │
  ├─ Server starts on the assigned port
  │
  ├─ WebServerInitializedEvent
  │    └─ Run: portless alias <name> <port>
  │
  └─ Shutdown
       └─ Run: portless alias --remove <name>
```

The proxy auto-starts if it's not already running. No manual `portless proxy start` needed.

## IntelliJ IDEA

Just run or debug normally. The starter handles everything — no need for `portless run`, no special run configurations.

1. Add the dependency
2. Set `spring.application.name` (or `portless.name`)
3. Hit Run/Debug
4. Open `http://{name}.localhost:1355`

Breakpoints, hot reload, DevTools — everything works as expected.

## Custom TLD

> Requires portless 0.6+

Use `.test`, `.dev`, or any custom TLD instead of `.localhost`:

```yaml
portless:
  name: myapp
  tld: test
```

The proxy starts with `--https --tld test`. Your app is accessible at `https://myapp.test`.

> **Note:** Custom TLDs require DNS configuration. Run `sudo portless hosts sync` to add entries to `/etc/hosts`, or configure a local DNS resolver.

## Configuration Reference

All properties are optional. The starter activates automatically when the `portless` CLI is installed.

```yaml
portless:
  enabled: true        # Enable/disable the starter (default: true)
  name: myapp          # Route name (default: spring.application.name)
  tld: test            # Custom TLD, starts proxy with --https --tld (default: none → localhost)
  force: false         # Overwrite existing route for same name (default: false)
  min-port: 8000       # Min port for random allocation (default: 8000)
  max-port: 8999       # Max port for random allocation (default: 8999)
  state-dir: ~/.portless  # Portless state directory (default: auto-detected)
```

### Disabling

Set `portless.enabled=false` or remove the dependency. The starter is a no-op when:

- `portless.enabled` is `false`
- The `portless` CLI is not installed
- No `portless.name` or `spring.application.name` is set

## Requirements

- Java 17+
- Spring Boot 3.x
- [`portless`](https://github.com/vercel-labs/portless) CLI (`npm install -g portless@latest`)

## Building from Source

```bash
git clone https://github.com/leesum-in/portless-spring-boot-starter.git
cd portless-spring-boot-starter
./gradlew build
```

## License

[MIT](LICENSE)
