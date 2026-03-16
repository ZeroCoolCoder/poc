# Testing the Workflow Engine

## Local Environment Setup

### Prerequisites
- Java 17
- Maven 3.6+
- Redis running on localhost:6379 (no password for dev)
- Node.js 18+ (for frontend)

### Start Backend
```bash
cd workflow-engine
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
- Uses H2 in-memory database (resets on restart)
- schema.sql auto-loads tables and sequences
- Runs on port 8080
- Action handlers registered: log, dataTransform, httpCall
- Rules engines registered: spel, script

### Start Frontend
```bash
cd workflow-ui
npm install && npm run dev
```
- Runs on port 5173
- Requires backend CORS config (dev profile has it)

## Running Unit Tests
```bash
cd workflow-engine
mvn test
```
- 52 repository integration tests using @JdbcTest with H2
- 17 other tests (service, engine, controller layers)
- Note: `WorkflowEngineApplicationTests.contextLoads` may fail due to Redisson auto-configuration exclude issue - this is a pre-existing issue unrelated to JDBC conversion

## End-to-End Testing via API

### Test Case 1: Linear Workflow Lifecycle
1. POST `/api/v1/workflow-definitions` - Create definition with START, AUTO (handler: "log"), WAIT_FOR_INPUT, END nodes and connecting transitions
2. PUT `/api/v1/workflow-definitions/{id}/activate` - Activate the definition
3. POST `/api/v1/workflow-instances/start` - Start instance with initialContext
4. GET `/api/v1/workflow-instances/{id}/executions` - Verify execution chain
5. POST `/api/v1/workflow-instances/action` - Submit external action with nodeKey, action, payload
6. Verify instance status becomes COMPLETED

### Test Case 2: Parallel Approval (FORK/JOIN)
1. Create workflow with 6 nodes: START -> FORK -> [approver1, approver2] (WAIT_FOR_INPUT) -> JOIN -> END
2. **IMPORTANT**: Must include 6 transitions including `join -> end`. Missing this transition will cause the workflow to stall at JOIN.
3. Activate and start instance
4. Submit approver1 action - verify instance stays RUNNING (JOIN waits)
5. Submit approver2 action - verify instance becomes COMPLETED

### Common Pitfalls
- **Missing transitions**: Always verify your test workflow has transitions for ALL node connections, especially `join -> end`. The engine won't advance if there's no outgoing transition.
- **Async execution**: FORK spawns parallel branches via @Async. After starting a FORK workflow, both WAIT_FOR_INPUT branches should be WAITING_FOR_INPUT immediately.
- **Optimistic locking**: WorkflowInstance uses manual optimistic locking (OPT_LOCK_VERSION). The current implementation does not throw on version mismatch - it silently skips the update.

## Key API Endpoints
- `GET /api/v1/workflow-definitions` - List all definitions
- `POST /api/v1/workflow-definitions` - Create definition with nodes and transitions
- `GET /api/v1/workflow-definitions/{id}` - Get definition with full graph
- `PUT /api/v1/workflow-definitions/{id}/activate` - Activate definition
- `POST /api/v1/workflow-instances/start` - Start instance
- `GET /api/v1/workflow-instances/{id}` - Get instance status
- `GET /api/v1/workflow-instances/{id}/executions` - Get execution history
- `POST /api/v1/workflow-instances/action` - Submit external action (requires workflowInstanceId, nodeKey, action, payload)

## Devin Secrets Needed
No secrets required for local testing - H2 in-memory database and Redis run without authentication in dev profile.
