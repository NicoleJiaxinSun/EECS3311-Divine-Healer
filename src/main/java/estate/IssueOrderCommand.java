package estate;

record IssueOrderCommand(Step step) implements GameCommand {
    enum Step {
        REQUEST, APPROVE, EXECUTE
    }

    enum Status {
        NONE, REQUESTED, APPROVED, EXECUTED
    }

    @Override
    public ActionResult execute(GameState state) {
        if (!state.isReliefOfficer()) {
            return new ActionResult(false, "Only a relief officer may operate the correction order.");
        }
        return switch (step) {
            case REQUEST -> advance(state, Status.NONE, Status.REQUESTED,
                    "Correction order requested.", "The correction order has already been requested.");
            case APPROVE -> advance(state, Status.REQUESTED, Status.APPROVED,
                    "Correction order approved.", "Only a requested correction order can be approved.");
            case EXECUTE -> executeApprovedOrder(state);
        };
    }

    private ActionResult executeApprovedOrder(GameState state) {
        if (state.correctionOrder() != Status.APPROVED) {
            return new ActionResult(false, "Only an approved correction order can be executed once.");
        }
        state.laborRegister().correctMillCharge();
        state.setCorrectionOrder(Status.EXECUTED);
        return new ActionResult(true, "Correction order executed. The duplicate mill charge was removed.");
    }

    private ActionResult advance(GameState state, Status required, Status next,
            String successMessage, String failureMessage) {
        if (state.correctionOrder() != required) {
            return new ActionResult(false, failureMessage);
        }
        state.setCorrectionOrder(next);
        return new ActionResult(true, successMessage);
    }
}
