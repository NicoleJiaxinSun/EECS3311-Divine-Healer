
# The Divine Healer's Estate

EECS3311 Course Project  

An AI Game Master agent for a political story game. AI interprets player actions and proposes multi-step plans, while Java rules determine action outcomes and NPC negotiation results.

## Stage 1

The complete project design report, including feature specifications, UML diagrams, design pattern explanations, and feature-to-design traceability, is available in:

[Stage 1 Project Design Report](Stage1/Stage1_Project_Design_Report.pdf)

## Stage 2 - Update 1: CLI investigation

From the repository root, use JDK 17 or newer. Download Gson once to compile
and run this version:

```sh
curl -fL https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar -o gson-2.11.0.jar
javac -cp gson-2.11.0.jar -d out src/main/java/estate/*.java
java -cp "out:gson-2.11.0.jar" estate.GameCLI
```

On Windows, use a semicolon between the two classpath entries.

Try `inspect mill` to discover evidence, `inspect manor` to see an access
refusal, and `evidence` to list discoveries. Inspecting the mill again keeps a
single evidence entry. Use `help` for the command list and `quit` to exit.

This slice implements F02 (UC02). `GameCLI` routes inspection through
`GameController` to `InspectCommand`. The command checks access through
`LaborRegister` and records discoveries in `GameState`. The mill and manor
records are starter data, and evidence is held in memory for the session.

## Stage 2 - Update 2: NPC negotiation and evidence submission

Try this sequence in the CLI:

```text
talk steward
inspect mill
submit mill steward
talk steward
```

The steward requires evidence of a disputed charge before agreeing to review
it. Discovering the evidence gives it the ID `mill`, and submitting it makes
it known to the steward. Repeated submissions keep a single knowledge entry.
Try `talk healer` for a cooperative NPC and `submit mill healer` to see an
irrelevant submission refused.

`TalkCommand` routes the rule-based part of F03 (UC03) to an NPC's
`NegotiationStrategy`. For F04 (UC04), it checks that the player discovered
the evidence before the NPC evaluates relevance and records it. Dialogue
uses fixed lines at this stage. NPC knowledge is held in memory.

## Stage 2 - Update 3: Appointment, grain relief, and official orders

```text
inspect mill
submit mill steward
appoint
allocate 3 mill
order request
order approve
order execute
status
inspect mill
```

Submitting the mill evidence to the steward qualifies the player for the
relief officer appointment (F07). That role can allocate grain from a
starting stock of 10 units to the mill household (F05) and operate a single
official order to correct the duplicate labor charge (F06). Request and
approval change the order status. Execution corrects the labor register
once. Reinspection shows the corrected record while the evidence list
retains the original finding.

The grain stock, relief officer role, and correction order are scenario
rules for the current playable slice.

## Stage 2 - Update 4: Political events and action history

Use `events` to see the four story milestones (F08). The first starts
active and each ending unlocks the next. The sequence tracks presenting
the mill evidence to the steward, gaining the relief appointment,
delivering at least 3 grain, and executing the correction order. When
an event unlocks, the game checks whether its goal is already satisfied.
Use `event abandon` to give the active event a failed ending and continue
to the next milestone. A finished event keeps its ending for the session.

Use `history` to review accepted and refused game actions (the
deterministic part of F11). Read-only queries and input usage errors
stay out of the history. The command executor sends action outcomes to
the event manager and the history recorder.

## Stage 2 - Update 5: Natural-language actions, plans, and recap

Set the `DEEPSEEK_API_KEY` environment variable before launching Java to
enable AI features. The adapter uses `deepseek-flash` and
[DeepSeek's Chat Completions API](https://api-docs.deepseek.com/api/create-chat-completion/).
Typed commands work without a key.

```text
act Inspect the mill labor record
goal Get appointed as relief officer and deliver three grain to the mill
plan show
plan approve
plan run
history
recap
```

- F01: `act` interprets a request as one supported game command and runs it
  through the same Java rules as a typed command.
- F09: `goal` creates one draft at a time with up to eight validated commands.
  Drafting changes no game state.
- F10: Review the steps before approving and running them. A failed step
  pauses the plan. After addressing the issue, approve and run again to
  retry that step. Earlier successful steps are retained. Use `plan cancel`
  to end the current plan.
- F11: `recap` summarizes up to 12 recent action records. `history` remains
  available when the AI call fails.

The AI commands send the request, visible game status, and recent action
records as context where applicable. The course's KUMA validation remains to be completed.

## Swing desktop interface

After compiling with the setup above, launch the desktop window:

```sh
java -cp "out:gson-2.11.0.jar" estate.GameGUI
```

Choose **Command** and enter a command such as `inspect mill` or
`submit mill steward`. Use the shortcuts for status, evidence, events,
history, and plan controls. Choose **AI action** or **AI goal** to enter
natural-language text. The window uses the same game rules and API key
configuration as the CLI, with a separate game session each time it starts.

## Save and load a game (F12)

Use `save my game.json` and `load my game.json` in the CLI, or use
the **Save** and **Load** buttons in the desktop window. The file
preserves game progress, evidence, action history, and the current
plan. Loading rebuilds progress from the saved game actions and
restores the plan at its saved step. A previously approved plan
requires approval again after loading.

Saving to an existing CLI path replaces that file. A malformed or
unsupported save reports an error and preserves the current session.

## Stage 3 - Automated Java tests

From the repository root, follow the setup above to obtain Gson, then
download the JUnit runner and run the suite:

```sh
curl -fL https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar -o junit-platform-console-standalone-1.11.4.jar
javac --release 17 -cp "gson-2.11.0.jar:junit-platform-console-standalone-1.11.4.jar" -d out/test src/main/java/estate/*.java src/test/java/estate/*.java
java -jar junit-platform-console-standalone-1.11.4.jar execute --class-path "out/test:gson-2.11.0.jar" --scan-class-path
```

On Windows, use a semicolon between classpath entries. The tests run
without a DeepSeek key. The plan-resume case supplies a fixed model
response to verify the deterministic approval and execution rules.

| Features | Covered behavior |
| --- | --- |
| F02–F04 | Investigation access, evidence deduplication, negotiation, evidence relevance |
| F05–F07 | Resource limits and permissions, order lifecycle, appointment conditions |
| F08 | Event unlocking, failed endings, and retained outcomes |
| F10–F11 | Plan approval and resume, successful and refused history entries |
| F12 | Save/load round trip and preservation of the active session on invalid input |

## Stage 3 - KUMA behavior evaluation

See the [behavior evaluation](Stage3/Behavior-Evaluation.md) for the
requirements, nine offline scenarios, baseline and retest results, and
the command to run the live DeepSeek cases.

## Final project report

The [final report](Final-Project-Report.md) maps Stage 1 features to the
implementation and tests, records the AI-assisted workflow, and
summarizes the evaluation results and remaining live checks.
