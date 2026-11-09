package estate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class GameState {
    private final LaborRegister laborRegister;
    private final Map<String, String> discoveredEvidence = new LinkedHashMap<>();

    GameState() {
        laborRegister = new LaborRegister(
                Map.of(
                        "mill", "The mill household was charged for the same shift twice.",
                        "manor", "The manor labor register records the steward's private assignments."),
                Set.of("mill"));
    }

    LaborRegister laborRegister() {
        return laborRegister;
    }

    boolean recordEvidence(String householdId, String finding) {
        return discoveredEvidence.putIfAbsent(householdId, finding) == null;
    }

    List<String> evidence() {
        return List.copyOf(discoveredEvidence.values());
    }
}
