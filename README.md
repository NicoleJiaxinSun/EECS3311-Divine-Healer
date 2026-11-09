
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

## Planned Implementation

- Swing GUI and additional CLI commands
- DeepSeek API integration
- Command, Adapter, Strategy, State, and Observer patterns
