package estate;

enum FailedState implements EventState {
    FAILED;

    @Override
    public boolean finished() {
        return true;
    }
}
