# Startup Bean Logger

## What is this?

The `StartupLogger` component logs all registered Spring beans at application startup. It's useful for:

- 🔍 **Debugging bean registration** - See which beans Spring created
- 🎭 **Identifying proxies** - Spots CGLIB proxies vs regular beans
- 🤖 **Verifying MCP setup** - Confirms tool providers are registered
- 📚 **Understanding Spring lifecycle** - Learn which beans load and in what form

## How to Enable

### Option 1: Via `application.yml` (Persistent)

Edit `src/main/resources/application.yml`:

```yaml
app:
  debug:
    startup-logging: true  # Change to 'true' to enable
```

### Option 2: Via Environment Variable (Temporary)

**PowerShell:**
```powershell
$env:APP_DEBUG_STARTUP_LOGGING = "true"
mvn spring-boot:run
```

**Bash:**
```bash
export APP_DEBUG_STARTUP_LOGGING=true
mvn spring-boot:run
```

### Option 3: Via Command Line Argument

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--app.debug.startup-logging=true"
```

## Sample Output

When enabled, you'll see output like this at startup:

```
==========================================
   REGISTERED BEANS (Startup Debug)
==========================================
🎭 mcpServerConfig → com.radix...McpServerConfig$$SpringCGLIB$$0 [PROXIED]
📦 athenaTools → com.radix.healthbridgemcp.tools.AthenaTools
📦 modmedTools → com.radix.healthbridgemcp.tools.ModMedTools
📦 athenaToolProvider → org.springframework.ai.tool.method.MethodToolCallbackProvider
📦 modmedToolProvider → org.springframework.ai.tool.method.MethodToolCallbackProvider
📦 groqChatService → com.radix.healthbridgemcp.chat.GroqChatService
📦 auditLogger → com.radix.healthbridgemcp.audit.AuditLogger
==========================================
   Total beans logged: 15
==========================================
```

## Key Indicators

- 🎭 **[PROXIED]** - Bean is wrapped in a CGLIB or JDK proxy (e.g., `@Configuration` classes)
- 📦 **Regular** - Standard Spring-managed singleton bean

## Default State

**Default: OFF** (`false`)

The feature is **disabled by default** to avoid noise in production logs. You must explicitly enable it when needed.

## Implementation Details

- **Annotation**: `@ConditionalOnProperty` ensures the bean only loads when enabled
- **Event**: Listens to `ContextRefreshedEvent` (fires after all beans are registered)
- **Filter**: Only logs beans matching: `tool`, `mcp`, `groq`, `chat`, `audit`, `config`
- **Logger**: Uses SLF4J (respects your logging configuration)

## When to Use

✅ **Enable when:**
- Learning Spring bean lifecycle
- Debugging MCP tool registration issues
- Verifying proxy creation
- Troubleshooting dependency injection

❌ **Disable when:**
- Running in production
- Don't need startup diagnostics
- Want cleaner logs
