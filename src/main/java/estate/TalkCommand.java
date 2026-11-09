package estate;

record TalkCommand(String npcId, String evidenceId) implements GameCommand {
    static TalkCommand review(String npcId) {
        return new TalkCommand(npcId, null);
    }

    static ActionResult requestAppointment(GameState state) {
        if (state.isReliefOfficer()) {
            return new ActionResult(true, "You are already a relief officer.");
        }
        if (!state.npc("steward").negotiate().success()) {
            return new ActionResult(false, "Steward: Submit evidence of the disputed charge before requesting appointment.");
        }
        state.appointReliefOfficer();
        return new ActionResult(true, "Steward: You are appointed relief officer.");
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
