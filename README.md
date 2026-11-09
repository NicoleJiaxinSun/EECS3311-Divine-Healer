
# The Divine Healer's Estate

EECS3311 Course Project  

An AI Game Master agent for a political story game. AI interprets player actions and proposes multi-step plans, while Java rules determine action outcomes and NPC negotiation results.

## Stage 1

The complete project design report, including feature specifications, UML diagrams, design pattern explanations, and feature-to-design traceability, is available in:

[Stage 1 Project Design Report](Stage1/Stage1_Project_Design_Report.pdf)

## Stage 2 - Update 1: CLI investigation

From the repository root, compile and run with JDK 17 or newer:

```sh
javac -d out src/main/java/estate/*.java
java -cp out estate.GameCLI
```

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
uses fixed lines at this stage. NPC knowledge is held in memory, and story event progression is planned for
a later update.

## Planned Implementation

- Swing GUI and additional CLI commands
- DeepSeek API integration
- Command, Adapter, Strategy, State, and Observer patterns
