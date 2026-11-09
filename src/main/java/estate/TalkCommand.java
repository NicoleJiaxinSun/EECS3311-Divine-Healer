package estate;

record TalkCommand(String npcId, String evidenceId) implements GameCommand {
    static TalkCommand review(String npcId) {
        return new TalkCommand(npcId, null);
    }

    @Override
    public ActionResult execute(GameState state) {
        NPC npc = state.npc(npcId);
        if (npc == null) {
            return new ActionResult(false, "Unknown NPC: " + npcId);
        }

        if (evidenceId == null) {
            return npc.negotiate();
        }
        if (!state.hasEvidence(evidenceId)) {
            return new ActionResult(false, "Evidence has not been discovered: " + evidenceId);
        }
        return npc.learn(evidenceId);
    }
}
