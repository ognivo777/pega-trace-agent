# pega-trace-agent

A Java agent that instruments a running Pega Infinity process at runtime to produce lightweight traces of key rule-engine execution points: service requests, flows, connectors and queue processor (dataflow) executions. It is built on [Byte Buddy](https://bytebuddy.net/) and targets Java 8.

## Purpose

Pega's built-in tracer produces extremely verbose output and is hard to use for coarse-grained performance analysis of production traffic. This agent instead attaches to a handful of well-known engine entry points and emits one line per event, giving a compact, near-real-time view of what the engine is doing and how long each step took.

It is intended for diagnostics and profiling, not for production tracing of full page context.

## Traced events

The agent registers a set of advice handlers against Pega internals. Each emits a single `System.out` line with the format `[TAG][duration_ms][...context...] |[error]`:

| Tag         | What is traced                 | Entry class / method                 | Output details                                             |
|-------------|--------------------------------|--------------------------------------|------------------------------------------------------------|
| `SERVICE`   | REST / SOAP service requests   | `ServiceAPI#processRequest`          | service type, data volume, HTTP method, package/class/method, requestor |
| `FLOW`      | Generated flow (FUA) execution | generated `ra_action_*` flows, `perform` | flow class, type, ruleset, name, draft mode, interest page, requestor |
| `CONNECTOR` | Connector invocation           | `ConnectorBaseImpl#invoke`           | connector class, page class, rule class/name/ruleset, requestor |
| `QP`        | Queue processor (dataflow)     | `QueueProcessorExecutor#execute`     | dataflow purpose, activity to run, requestor                |
| `ACTIVITY`  | Activity execution (disabled)  | `Executable#doActivity/doAction`    | activity class/name, class, step page, stack info           |

`ACTIVITY` instrumentation is present but currently commented out in the advice registry; enable it by editing `AdviceRegestry`.

## How it works

1. The JVM is started with `-javaagent` and the agent's `premain` runs.
2. An `AgentBuilder` from Byte Buddy installs a class-transformation `AgentBuilder.Listener` into `Instrumentation` (see `MethodLoggerAgent.install`).
3. `AdviceRegestry` builds the set of `PegaEventHandler` handlers.
4. Each handler either extends `BasicClassMethodHandler` (match types by supertype/name, instrument named methods) or provides a custom `inject` via `Advice.to(...)`.
5. At runtime, the woven advice captures `System.nanoTime()` on method entry and prints an annotated line on exit, including elapsed time and any thrown exception.

Byte Buddy `Advice` inlines the instrumentation so no extra classes need to be created at runtime. `disableClassFormatChanges()` keeps class layout stable (no added fields, no signature changes), so class redefinition and retransformation are enabled.

## Building

Requires JDK 8+ and the Gradle wrapper.

```bash
.\gradlew.bat build --no-daemon        # Windows
./gradlew build --no-daemon            # Linux / macOS
```

The build produces a self-contained fat jar (Byte Buddy bundled, Pega `libs/*.jar` excluded from re-packaging but required on the classpath):

```
build/libs/pega-trace-agent-1.0-SNAPSHOT.jar
```

### Local Pega libraries

The project depends on four proprietary Pega jars (see `build.gradle`):

- `libs/prpublic.jar`
- `libs/prprivate-session.jar`
- `libs/prenginext.jar`
- `libs/printegrint.jar`

These are referenced via `implementation files('libs/...')` and are intentionally git-ignored (`/libs/*.jar`). They must be present locally for the project to compile. They are not bundled into the agent jar; they are only needed on the compile classpath (instrumentation of Pega internals is done through reflection and Byte Buddy field reads at runtime).

## Attaching the agent

Attach on JVM startup:

```bash
java -javaagent:build/libs/pega-trace-agent-1.0-SNAPSHOT.jar -jar your-application.jar
```

or attach dynamically to a running Java process using the `Agent-Class` manifest entry (JDK `jcmd` / Attach API support required).

To avoid an enormous fat jar (Byte Buddy is bundled in), you can instead place the agent jar on a dedicated path and add Byte Buddy to the runtime classpath:

```bash
java -javaagent:build/libs/pega-trace-agent-1.0-SNAPSHOT.jar \
     -cp "agent.jar:byte-buddy-1.18.12.jar" \
     -jar your-application.jar
```

## Output example

```
[CONNECTOR][1460ms][Rule-Connect-REST][Rule-Connect-REST][Code-Pega-List][pxAllAvailableReleases][Pega-Desktop:08-06-02][[STANDARD][A0MLI1OUUR36NHDUFYAI10VO475OEBER4A]  |
[SERVICE][678ms][Rest][0][STANDARD][AD6833SETE23Z49PSN66NI1H28GP89IO4A] GET|test|1|test098f6bcd4621d373cade4e832627b4f6||
[FLOW][138ms][CL:Work-ProjectManagement-SaveAs ID:SaveAsModalFlow RS:Pega-Desktop][SaveAsModalFlow][Draft:false][Interest:Work-ProjectManagement-SaveAs][pyID:][TABTHREAD3][HETZ9L3V93EO7GTJPNVDIBKUX16WRBTQTA]  |
[QP][57ms][Test_QP][null] |
```

## Configuration

The build supports pluggable instrumentation via `AgentConfig.parse(agentArgs)`, but the configuration syntax is not yet implemented — `AgentConfig` currently returns an empty instance and the set of active advices is chosen in code (`AdviceRegestry`). To select which events to trace, enable/disable handlers in the registry and rebuild.

## Status

Early-stage diagnostics tool. Configuration parsing, agent argument handling and choosing instrumentation per config are still work in progress.