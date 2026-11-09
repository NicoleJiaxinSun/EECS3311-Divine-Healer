package estate;

import java.util.List;
import java.util.Locale;

final class GameController {
    private final GameState state = new GameState();

    ActionResult inspect(String householdId) {
        String target = householdId.strip().toLowerCase(Locale.ROOT);
        if (target.isEmpty()) {
            return new ActionResult(false, "Usage: inspect <household>");
        }

        GameCommand command = new InspectCommand(target);
        return command.execute(state);
    }

    List<String> evidence() {
        return state.evidence();
    }
}
