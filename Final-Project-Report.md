# The Divine Healer's Estate

EECS3311 final project report · November 10, 2026

## Project scope and design

The Divine Healer's Estate is a political story game. The player
investigates labor records, presents evidence to NPCs, gains authority,
delivers relief, and corrects an official record. A player can enter a
typed command or ask the AI agent to interpret a natural-language action,
draft a multi-step plan, or recap recent actions.

The [Stage 1 design report](Stage1/Stage1_Project_Design_Report.pdf) defines
12 features, their use cases, and their UML interactions. The implementation
keeps the design's main boundary. DeepSeek proposes actions and writes
recaps. Java decides whether an action is legal and applies the result.
The CLI and Swing window share `GameSession`, which routes commands and AI
requests to the same game logic.

`CommandFactory` turns typed or AI-proposed commands into game commands.
`CommandExecutor` invokes them and notifies `EventManager` and
`HistoryRecorder`. NPC decisions use `NegotiationStrategy`.
`DeepSeekAdapter` isolates the provider API behind `LLMClient`.
`AgentPlan` tracks draft, approval, pause, completion, and cancellation.
A refused step pauses the plan and keeps completed steps. An explicit
approval is required to retry.

## Requirements, implementation, and verification

Class names below refer to Java files under
[`src/main/java/estate`](src/main/java/estate). The JUnit files are under
[`src/test/java/estate`](src/test/java/estate). BT case IDs refer to the
[Stage 3 behavior evaluation](Stage3/Behavior-Evaluation.md).

| Feature / use case | Stage 1 design | Implementation and scope | Verification |
| --- | --- | --- | --- |
| F01 / UC01 · Natural-language action | ActionInterpreter, LLMClient, CommandFactory, CommandExecutor | GameAgent interprets one action using LLMClient and DeepSeekAdapter. CommandFactory and CommandExecutor validate and execute it. | BT-01–04, BT-07–08 |
| F02 / UC02 · Investigation | InspectCommand, LaborRegister, GameState | InspectCommand checks record access and records unique findings. | GameRulesTest · investigation |
| F03 / UC03 · Negotiation | TalkCommand, NPC, NegotiationStrategy | The same roles decide whether the steward or healer cooperates. Dialogue uses fixed lines in this version. | GameRulesTest · steward review |
| F04 / UC04 · Evidence submission | TalkCommand, NPC, EventManager | TalkCommand checks discovery and NPC checks relevance and knowledge. EventManager observes the result. | GameRulesTest · evidence relevance and repeat submission, BT-05 |
| F05 / UC05 · Resource allocation | AllocateCommand, Inventory | The relief officer can deliver grain to the mill within available stock. | GameRulesTest · authority and stock, BT-06 |
| F06 / UC06 · Official orders | IssueOrderCommand, GameRules, LaborRegister | IssueOrderCommand checks authority and advances one correction order from request to approval to execution. LaborRegister applies the correction. | GameRulesTest · order lifecycle |
| F07 / UC07 · Political appointment | TalkCommand, NPC, PoliticalSystem, GameState | TalkCommand requests the relief appointment using the steward's evidence condition and updates GameState on acceptance. | GameRulesTest · appointment |
| F08 / UC08 · Political events | EventManager, PoliticalEvent, EventState, GameRules | EventManager tracks four milestones, checks goals from game state, and retains resolved or failed outcomes. | GameRulesTest · milestone order and failure |
| F09 / UC09 · AI plan generation | GameController, AgentPlanner, LLMClient, AgentPlan | GameAgent drafts up to eight allowed commands. AgentPlan holds one current plan. | BT-05–06 |
| F10 / UC10 · Approved execution | GameController, AgentExecutor, AgentPlan, CommandExecutor | AgentPlan calls the shared executor through GameController and pauses on the first refused step. | BT-05–06, GamePersistenceTest · plan resume |
| F11 / UC11 · History and recap | HistoryRecorder, GameController, LLMClient | HistoryRecorder stores game action outcomes. GameAgent requests a recap of up to 12 recent entries. | GameRulesTest · action history, BT-07, BT-09 |
| F12 / UC12 · Save and load | GameStateRepository, JsonGameStateRepository, GameController | GameSession calls JsonGameStateRepository. A versioned JSON file stores recorded commands and the current plan snapshot. | GamePersistenceTest · round trip, plan resume, invalid load |

## Design changes

- **Agent responsibilities.** Stage 1 proposed separate interpreter,
  planner, and execution services. The current implementation groups
  interpretation, planning, and recap in `GameAgent` and keeps the
  execution cursor in `AgentPlan`. Both interfaces use one path for
  validating and executing actions.
- **Game rules and event states.** Stage 1 proposed a central `GameRules`
  class, a separate political listener, and concrete event state classes.
  The current scenario uses local command and domain guards, an
  evidence-based appointment in `TalkCommand`, and event enums with goal
  predicates. This represents the current four milestones without a class
  per event state.
- **Persistence.** The design called for restoring domain data. The
  implementation rebuilds it by replaying recorded commands into a fresh
  controller and restores the plan separately. This reuses the rules for
  evidence, authority, inventory, and events. The save format depends on
  versioned command semantics. A failed read leaves the active session
  intact, and restored approvals must be renewed.
- **Interface and scenario scope.** Swing provides a command field,
  separate AI action and goal modes, and shortcuts for common views and
  controls. The playable scenario uses two NPCs, one relief recipient,
  one correction order, and fixed negotiation dialogue.

## AI tools and collaboration

| Tool or platform | Model | Use |
| --- | --- | --- |
| OpenAI Codex | GPT-6 family. The exact serving variant was not recorded for each interaction. | Reviewed Stage 1 artifacts, implemented Java slices, ran smoke tests, prepared JUnit and KUMA checks, and diagnosed the recap failure. |
| DeepSeek API | `deepseek-flash` | Configured runtime model for action proposals, plan drafts, and history recaps. |
| KUMA 0.3.9 with local providers | No model in the recorded local judge runs | Delivered cases, captured responses, and evaluated checks against response and history evidence. |

Representative interactions below record user direction and observed
outcomes. The record covers the tasks discussed in the Codex chat.

| Task | Human direction or decision | Codex contribution | Outcome |
| --- | --- | --- | --- |
| Break implementation into reviewable updates | Asked for five separate updates with a pause after each because GitHub submissions were required. | Implemented one playable increment at a time and verified each before stopping. | Five distinct commits from `c9babf2` through `48822f6`. |
| Publish the updates | Supplied the intended GitHub repository and asked Codex to upload. Then requested each next update separately. | Checked the remote base, committed the requested slice, pushed to `main`, and verified the remote commit. | The repository history preserves the successive game, interface, persistence, and validation updates. |
| Add deterministic validation | Requested another update after save/load was available. | Turned previous smoke checks into reproducible JUnit cases covering access, rules, events, and persistence. | Nine tests passed in `7a87f4c`. |
| Evaluate AI behavior | Requested the next update after the deterministic suite. | Built the local KUMA case runner, diagnosed the multiline recap failure, and reran both suites. | `f6f8a68` records the cases, fix, and retest. |

## Verification and lessons learned

The deterministic suite exercises the same `GameSession` used by CLI and
Swing. All nine JUnit tests passed. They cover game outcomes and
non-mutation on rejection, including denied inspections, invalid
allocations, unauthorized or repeated execution, and failed loads.
A fixture supplies a plan for the approval and resume check. Five additional
JUnit tests exercise the DeepSeek adapter through a local HTTP server.
They verify request encoding, successful parsing, missing-key handling,
HTTP errors, and rejection of truncated or malformed completions. The
combined JUnit suite passed 14 of 14.

The KUMA suite uses nine scenarios with a local judge. It checks both the
visible reply and game history so a reported refusal can be compared with
whether an action actually ran. The offline baseline passed 8 of 9.
BT-09 found that the application rejected a valid two-line recap because
it applied the command control-character rule to prose. The fix
normalizes recap line breaks while preserving single-command validation.
The offline retest passed 9 of 9, and the JUnit regression run passed 9 of 9.

The distinction between a game action and a recap mattered during
validation. A malformed command can affect game state if accepted, while
a newline is ordinary formatting in a recap. Testing the outputs
separately exposed a failure that a single happy-path demonstration would
have missed. The authority and plan checks also show which safeguards
can be tested without depending on model behavior.

Five cases are configured for live DeepSeek testing. No
`DEEPSEEK_API_KEY` was configured in the evaluation environment, so live
model behavior remains unverified. The [README](README.md) explains how
to run both interfaces and the test suite. The
[behavior evaluation](Stage3/Behavior-Evaluation.md) records the local
results and the live run command.
