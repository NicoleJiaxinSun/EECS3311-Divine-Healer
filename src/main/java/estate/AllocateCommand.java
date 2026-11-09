package estate;

record AllocateCommand(int quantity, String recipient) implements GameCommand {
    @Override
    public ActionResult execute(GameState state) {
        if (!state.isReliefOfficer()) {
            return new ActionResult(false, "Only a relief officer may allocate grain.");
        }
        if (!recipient.equals("mill")) {
            return new ActionResult(false, "Unknown relief recipient: " + recipient);
        }
        if (!state.inventory().allocateGrainToMill(quantity)) {
            return new ActionResult(false, "Grain quantity must be positive and within current stock.");
        }
        return new ActionResult(true, "Allocated " + quantity + " grain to the mill household.");
    }
}
