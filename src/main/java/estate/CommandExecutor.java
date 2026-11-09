package estate;

import java.util.List;

final class CommandExecutor {
    private final List<GameEventListener> listeners;

    CommandExecutor(List<GameEventListener> listeners) {
        this.listeners = List.copyOf(listeners);
    }

    ActionResult execute(GameState state, String action, GameCommand command) {
        ActionResult result = command.execute(state);
        for (GameEventListener listener : listeners) {
            listener.onGameEvent(state, action, result);
        }
        return result;
    }
}
