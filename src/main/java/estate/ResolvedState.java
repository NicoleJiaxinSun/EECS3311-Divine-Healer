package estate;

enum ResolvedState implements EventState {
    RESOLVED;

    @Override
    public boolean finished() {
        return true;
    }
}
