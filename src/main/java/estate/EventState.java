package estate;

interface EventState {
    String name();

    default EventState activate() {
        return this;
    }

    default EventState resolve(boolean goalAchieved) {
        return this;
    }

    default EventState fail() {
        return this;
    }

    default boolean finished() {
        return false;
    }
}
