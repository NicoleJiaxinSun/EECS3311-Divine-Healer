package estate;

import java.util.function.Predicate;

final class PoliticalEvent {
    private final String title;
    private final Predicate<GameState> goal;
    private EventState state = LockedState.LOCKED;

    PoliticalEvent(String title, Predicate<GameState> goal) {
        this.title = title;
        this.goal = goal;
    }

    String title() {
        return title;
    }

    void activate() {
        state = state.activate();
    }

    void handleAction(GameState game) {
        state = state.resolve(goal.test(game));
    }

    void fail() {
        state = state.fail();
    }

    boolean finished() {
        return state.finished();
    }

    String describe() {
        return state.name() + " | " + title;
    }
}
