# Spring ApplicationContext — Complete Guide

## 1. What Is ApplicationContext?

`ApplicationContext` is **the Spring IoC container itself** — it's a runtime object that holds all your beans (objects) and their relationships. Think of it as a **smart factory + registry** for your entire application's objects.

---

## 2. The Interface Hierarchy

```
BeanFactory                    ← Basic container (lazy, minimal)
  └── ApplicationContext       ← Advanced container (eager, full-featured)
       └── ConfigurableApplicationContext  ← Adds lifecycle management (close, refresh)
            └── Implementations:
                 - AnnotationConfigApplicationContext (Java config)
                 - ClassPathXmlApplicationContext (XML config)
                 - GenericWebApplicationContext (web apps)
```

`BeanFactory` is the raw IoC container. `ApplicationContext` **extends** it and adds:
- Event publishing (`ApplicationEvent`)
- Internationalization (i18n via `MessageSource`)
- Resource loading (files, URLs)
- Environment/profiles support
- AOP integration
- Eager initialization of singletons (vs. BeanFactory's lazy approach)

---

## 3. How It Relates to IoC (Inversion of Control)

**Without IoC (you manage objects):**
```java
UserRepository repo = new UserRepository();
UserService service = new UserService(repo);  // YOU wire dependencies
```

**With IoC (container manages objects):**
```java
@Service
public class UserService {
    @Autowired
    private UserRepository repo;  // CONTAINER injects this
}
```

The `ApplicationContext` **is** the thing doing that injection. It:
1. Scans your classpath for `@Component`, `@Service`, `@Repository`, `@Controller`
2. Creates instances (beans)
3. Resolves dependencies between them
4. Injects them via constructor, setter, or field injection
5. Manages their lifecycle (init → use → destroy)

---

## 4. How Spring Boot Creates It

```java
SpringApplication.run(EhrBridgeMcpApplication.class, args);
```

This single line does:
1. Creates a `SpringApplication` instance
2. Determines the application type (web/reactive/none)
3. Creates the appropriate `ApplicationContext` implementation
4. Loads all configuration (`@SpringBootApplication` triggers component scanning)
5. Refreshes the context (instantiates all singleton beans)
6. Returns the live context

---

## 5. "It Creates an Object of Itself" — Explained

```java
SpringApplication.run(EhrBridgeMcpApplication.class, args);
```

You pass `EhrBridgeMcpApplication.class` as a **configuration source**. Spring then:
- Uses it as the root for `@ComponentScan` (scans its package and sub-packages)
- Registers it as a `@Configuration` bean (because `@SpringBootApplication` includes `@Configuration`)
- **Creates a Spring-managed instance** of `EhrBridgeMcpApplication` inside the container

So yes — the class that *launches* the container also becomes *a bean inside* that container. This isn't recursion; the `main()` method runs first (plain Java), then Spring takes over and manages a *separate* instance.

---

## 6. What You Can Do With ApplicationContext

```java
ApplicationContext ctx = SpringApplication.run(EhrBridgeMcpApplication.class, args);

// Get any bean by type
UserService service = ctx.getBean(UserService.class);

// Get bean by name
Object bean = ctx.getBean("userService");

// Check if a bean exists
ctx.containsBean("userService");

// Get environment properties
String port = ctx.getEnvironment().getProperty("server.port");

// Publish events
ctx.publishEvent(new MyCustomEvent(this));

// Get all beans of a type
Map<String, UserService> beans = ctx.getBeansOfType(UserService.class);
```

---

## 7. Correct Code (Fixing the Compile Error)

The **wrong** import:
```java
import org.apache.catalina.core.ApplicationContext;  // ❌ Tomcat internal class
```

The **correct** import:
```java
import org.springframework.context.ApplicationContext;  // ✅ Spring's IoC container
```

**Full working example:**

```java
package com.radix.healthbridgemcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class EhrBridgeMcpApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(EhrBridgeMcpApplication.class, args);
    }
}
```

---

## 8. Lazy vs Eager Initialization

### What Are They?

| Aspect | Eager Initialization (Default) | Lazy Initialization |
|--------|-------------------------------|---------------------|
| When bean is created | At application startup (context refresh) | On first access/request |
| Default in Spring | ✅ Yes (for singletons) | ❌ No (must opt-in) |
| Startup time | Slower (all beans created upfront) | Faster (beans created on-demand) |
| Fail-fast | ✅ Errors caught at startup | ❌ Errors appear at runtime |
| Memory at startup | Higher (all beans in memory) | Lower (only needed beans loaded) |

### Eager Initialization (Default Behavior)

By default, `ApplicationContext` creates **all singleton beans** during startup. This is called **eager initialization**.

```java
@Service
public class UserService {
    public UserService() {
        System.out.println("UserService created!");  // Printed at startup
    }
}
```

When the app starts → Spring scans → finds `UserService` → creates it **immediately**, even if no one has called it yet.

**Advantages:**
- Fail-fast: Configuration errors, missing dependencies, and wiring problems are caught at startup
- No latency on first request — everything is pre-built
- Predictable memory usage from the start

### Lazy Initialization

With lazy initialization, a bean is **not created until it is first needed** (injected or requested via `getBean()`).

#### Option 1: Per-bean `@Lazy`

```java
@Service
@Lazy
public class ExpensiveService {
    public ExpensiveService() {
        System.out.println("ExpensiveService created!");  // Only when first used
    }
}
```

#### Option 2: `@Lazy` on injection point

```java
@Service
public class UserService {

    @Autowired
    @Lazy
    private ExpensiveService expensiveService;  // Proxy injected; real object created on first method call
}
```

#### Option 3: Global lazy initialization (Spring Boot 2.2+)

In `application.properties`:
```properties
spring.main.lazy-initialization=true
```

This makes **ALL beans** lazy. Useful for faster dev startup, but **not recommended for production**.

#### Option 4: Programmatic via `@Configuration`

```java
@Configuration
public class AppConfig {

    @Bean
    @Lazy
    public ExpensiveService expensiveService() {
        return new ExpensiveService();
    }
}
```

### How Lazy Works Under the Hood

When you mark a bean as `@Lazy`:
1. Spring creates a **proxy object** (not the real bean)
2. The proxy is injected into dependents
3. On the **first method call** to the proxy → Spring creates the real bean and delegates the call
4. Subsequent calls go directly to the real bean

### BeanFactory vs ApplicationContext (Lazy vs Eager)

| Container | Default Behavior |
|-----------|-----------------|
| `BeanFactory` | **Lazy** — beans created only when requested |
| `ApplicationContext` | **Eager** — all singleton beans created at startup |

This is one of the key differences between the two. `ApplicationContext` eagerly initializes because it's designed for production apps where you want fail-fast behavior.

### When to Use What

**Use Eager (default) when:**
- You want fail-fast startup (catch config errors early)
- Your app is in production
- Beans are lightweight or always needed

**Use Lazy when:**
- A bean is expensive to create and rarely used
- You want faster startup in development
- You have conditional beans that may never be needed
- Integration tests where you don't need all beans

### Example: Seeing the Difference

```java
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        System.out.println("Before context creation");
        ApplicationContext ctx = SpringApplication.run(DemoApplication.class, args);
        System.out.println("After context creation");

        // For @Lazy beans, this triggers actual creation:
        ctx.getBean(ExpensiveService.class);
        System.out.println("After getBean call");
    }
}

@Service
public class EagerService {
    public EagerService() {
        System.out.println("EagerService created");  // Prints BEFORE "After context creation"
    }
}

@Service
@Lazy
public class ExpensiveService {
    public ExpensiveService() {
        System.out.println("ExpensiveService created");  // Prints AFTER "After context creation"
    }
}
```

**Output:**
```
Before context creation
EagerService created
After context creation
ExpensiveService created
After getBean call
```

---

## 9. Summary Table

| Concept | Role |
|---------|------|
| `ApplicationContext` | The IoC container — creates, wires, and manages beans |
| `SpringApplication.run()` | Bootstraps and returns the container |
| `@SpringBootApplication` | Marks the config source + enables auto-config + component scan |
| IoC | Design principle — container controls object creation, not you |
| DI | Implementation of IoC — container *injects* dependencies into beans |
| Eager Init | Beans created at startup (default for ApplicationContext) |
| Lazy Init | Beans created on first use (opt-in with `@Lazy`) |
| `BeanFactory` | Parent interface; lazy by default; minimal features |
| `ApplicationContext` | Child interface; eager by default; full features |

---

*Generated for reference — Radix EHR Bridge MCP Project*
