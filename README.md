# EHR Bridge MCP Server

An MCP (Model Context Protocol) AI Server that exposes EHR Bridge capabilities as AI-callable tools.

## What is this?
This server lets AI assistants (GitHub Copilot, Claude, etc.) query EHR data through natural language.
The AI calls tools exposed by this server, which in turn call the radix-ehr-bridge REST APIs.

## Quick Start

### 1. Build & Run
```bash
cd radix-ehr-bridge-mcp
mvn clean install

# Set your Groq API key (get one from https://console.groq.com/keys)
$env:GROQ_API_KEY = "gsk_your_key_here"    # PowerShell
# export GROQ_API_KEY=gsk_your_key_here     # bash

mvn spring-boot:run
```
Server starts on **http://localhost:8081**

### 2. Verify it's running
Open browser: http://localhost:8081/actuator/health

### 3. Chat with the AI (Groq + llama-3.3-70b-versatile)
```bash
curl -X POST http://localhost:8081/api/chat `
     -H "Content-Type: application/json" `
     -d '{"message": "Show me all providers in Athena for ACME-DEMO"}'
```
Groq auto-invokes the right EHR tool(s) and returns a natural-language answer.

### 4. AI-summarized tool responses
Append `?summarize=true` to any `/api/test/**` endpoint:
```
GET http://localhost:8081/api/test/jobs/failed/ACME-DEMO?hours=24&summarize=true
```
Returns both the raw JSON and a Groq-generated summary.

### 5. Test with MCP Inspector
```bash
npx @modelcontextprotocol/inspector
```
Enter URL: `http://localhost:8081/sse`

### 6. Test with VS Code Copilot
1. Open this folder in VS Code
2. `.vscode/mcp.json` is already configured
3. Open Copilot Chat → switch to **Agent** mode
4. Ask: "Get providers from Athena for tenant ACME-DEMO"

## Available Tools (15 total)

### Athena (5 tools)
| Tool | Description |
|------|-------------|
| `athena_testConnection` | Test Athena API connectivity |
| `athena_getProviders` | List providers/doctors |
| `athena_getAppointmentTypes` | Get appointment types |
| `athena_getDepartments` | Get departments/locations |
| `athena_searchPatients` | Search patients by name |

### ModMed (4 tools)
| Tool | Description |
|------|-------------|
| `modmed_testConnection` | Test ModMed API connectivity |
| `modmed_getProviders` | List providers |
| `modmed_getAppointments` | Get appointments for a date |
| `modmed_getLocations` | Get practice locations |

### Healthjump (3 tools)
| Tool | Description |
|------|-------------|
| `healthjump_testConnection` | Test Healthjump connectivity |
| `healthjump_getPatients` | Get synced patients |
| `healthjump_getSyncStatus` | Check data sync status |

### Allscripts (2 tools)
| Tool | Description |
|------|-------------|
| `allscripts_testConnection` | Test Allscripts connectivity |
| `allscripts_getSchedule` | Get provider schedule for a date |

### Job Dashboard (1 tool)
| Tool | Description |
|------|-------------|
| `jobs_getStatus` | Get sync job status summary |
| `jobs_getFailedJobs` | List failed sync jobs |

## Demo Questions to Ask the AI

- "Test the Athena connection for tenant ACME-DEMO"
- "Show me all providers in ModMed for ACME-DEMO"
- "What appointments are scheduled in ModMed for today?"
- "Are there any failed sync jobs in the last 24 hours?"
- "Compare providers across Athena and ModMed for ACME-DEMO"
- "Show me open slots in the Allscripts schedule for today"
- "What's the Healthjump sync status for ACME-DEMO?"
- "Search for patient Williams in Athena"

## Architecture

```
AI Client (VS Code Copilot / Claude / MCP Inspector)
    │
    │  MCP Protocol (HTTP + SSE)
    ▼
radix-ehr-bridge-mcp (this server, port 8081)
    │
    │  HTTP REST calls (mocked for demo)
    ▼
radix-ehr-bridge (existing service)
    │
    ▼
Athena / ModMed / Healthjump / Allscripts APIs
```

## Project Structure

```
src/main/java/com/radix/ehrbridgemcp/
├── EhrBridgeMcpApplication.java      # Spring Boot entry point
├── config/
│   └── McpServerConfig.java          # Registers tool providers
├── tools/
│   ├── AthenaTools.java              # 5 Athena tools
│   ├── ModMedTools.java              # 4 ModMed tools
│   ├── HealthjumpTools.java          # 3 Healthjump tools
│   ├── AllscriptsTools.java          # 2 Allscripts tools
│   └── JobDashboardTools.java        # 2 Job monitoring tools
├── mock/
│   └── MockDataProvider.java         # Realistic mock EHR data
└── audit/
    └── AuditLogger.java              # Tool call audit logging
```

