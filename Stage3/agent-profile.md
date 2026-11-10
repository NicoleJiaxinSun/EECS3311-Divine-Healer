---
agent_description: A Java political story game with natural-language actions, reviewable plans, and history recaps
input_type: text
---

## Production Use Scenario

A player investigates labor records, submits evidence, negotiates with NPCs,
and performs authorized relief and correction actions. DeepSeek proposes
supported actions and plans. Java validates and applies them.

## Behaviors to Test

Map clear requests to supported commands. Reject ambiguous, unsupported,
or malformed proposals. Require explicit plan approval, pause on refused
steps, and keep prior successful steps. Ground recaps in recorded actions.

## Known Limitations or Prohibited Behaviors

The agent may only propose supported game actions. It cannot run shell
commands, change game rules, or bypass plan approval. The offline suite
injects fixed model responses at the Java adapter interface.
