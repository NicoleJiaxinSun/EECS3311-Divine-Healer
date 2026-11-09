package estate;

record InspectCommand(String householdId) implements GameCommand {
    @Override
    public ActionResult execute(GameState state) {
        LaborRegister register = state.laborRegister();
        if (!register.contains(householdId)) {
            return new ActionResult(false, "Unknown household: " + householdId);
        }

        var finding = register.inspect(householdId);
        if (finding.isEmpty()) {
            return new ActionResult(false, "Access denied: " + householdId);
        }

        String evidence = finding.get();
        if (state.recordEvidence(householdId, evidence)) {
            return new ActionResult(true, "Evidence discovered: " + evidence);
        }
        return new ActionResult(true, "Already discovered: " + evidence);
    }
}
