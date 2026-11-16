package estate;

enum LockedState implements EventState {
    LOCKED;

    @Override
    public EventState activate() {
        return ActiveState.ACTIVE;
    }
}
