package estate;

enum ActiveState implements EventState {
    ACTIVE;

    @Override
    public EventState resolve(boolean goalAchieved) {
        return goalAchieved ? ResolvedState.RESOLVED : this;
    }

    @Override
    public EventState fail() {
        return FailedState.FAILED;
    }
}
