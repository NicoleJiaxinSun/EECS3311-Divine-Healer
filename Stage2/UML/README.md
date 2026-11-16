# Current implementation UML

These diagrams describe the implemented Java code as of November 15,
2026. Each PNG has an editable PlantUML source with the same base name.
Class views omit method visibility and some supporting record and enum
details to keep the module responsibilities readable.

- [CD02A - Interfaces and agent](CD02A-Interfaces-Agent.png)
- [CD02B - Commands and domain](CD02B-Commands-Domain.png)
- [CD02C - Event states](CD02C-Events.png)
- [UCD02 - Implemented use cases](UCD02-Implemented.png)
- [SD08 - Action and observer flow](SD08-Action.png)
- [SD09 - Plan and approval flow](SD09-Plan.png)
- [SD10 - Save and load](SD10-Save-Load.png)
- [SD11 - NPC dialogue](SD11-NPC-Dialogue.png)

The current class views account for the combined GameAgent responsibilities,
shared GameSession, local configuration, command replay persistence, lambda
negotiation policies, and concrete event states. Stage 1 diagrams remain in
their original directory as the design baseline.
