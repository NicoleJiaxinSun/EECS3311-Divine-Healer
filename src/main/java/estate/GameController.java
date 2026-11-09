package estate;

import java.util.List;

final class GameController {
    private final GameState state = new GameState();
    private final EventManager eventManager = new EventManager();
    private final HistoryRecorder historyRecorder = new HistoryRecorder();
    private final CommandExecutor executor = new CommandExecutor(List.of(eventManager, historyRecorder));

    ActionResult execute(CommandFactory.Action action) {
        return execute(action.text(), action.command());
    }

    ActionResult abandonEvent() {
        return execute("event abandon", gameState -> eventManager.abandonActive());
    }

    List<String> events() {
        return eventManager.events();
    }

    List<String> history() {
        return historyRecorder.history();
    }

    private ActionResult execute(String action, GameCommand command) {
        return executor.execute(state, action, command);
    }

    String status() {
        String rank = state.isReliefOfficer() ? "relief officer" : "visitor";
        return "Rank: " + rank + " | Grain: " + state.inventory().grain()
                + " | Grain delivered to mill: " + state.inventory().millGrainDelivered()
                + " | Correction order: " + state.correctionOrder();
    }

    List<String> evidence() {
        return state.evidence();
    }
}
