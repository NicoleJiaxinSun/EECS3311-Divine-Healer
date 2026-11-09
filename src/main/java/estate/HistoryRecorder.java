package estate;

import java.util.ArrayList;
import java.util.List;

final class HistoryRecorder implements GameEventListener {
    private record Entry(String action, ActionResult result) {
        String display() {
            return (result.success() ? "OK" : "REFUSED") + " | " + action + " | " + result.message();
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    @Override
    public void onGameEvent(GameState state, String action, ActionResult result) {
        entries.add(new Entry(action, result));
    }

    List<String> recordedActions() {
        return entries.stream().map(Entry::action).toList();
    }

    List<String> history() {
        return entries.stream().map(Entry::display).toList();
    }
}
