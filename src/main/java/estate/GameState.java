package estate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class GameState {
    private final LaborRegister laborRegister;
    private final Inventory inventory = new Inventory();
    private boolean reliefOfficer;
    private IssueOrderCommand.Status correctionOrder = IssueOrderCommand.Status.NONE;
    private final Map<String, String> discoveredEvidence = new LinkedHashMap<>();

    private final Map<String, NPC> npcs = Map.of(
            "steward", new NPC("Steward", evidence -> {
                if (evidence.contains("mill")) {
                    return new ActionResult(true, "Steward: I agree to review the disputed charge.");
                }
                return new ActionResult(false, "Steward: Show me evidence of a disputed charge first.");
            }, Set.of("mill")),
            "healer", new NPC("Healer",
                    evidence -> new ActionResult(true, "Healer: I agree to hear your complaint."),
                    Set.of()));

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

    NPC npc(String npcId) {
        return npcs.get(npcId);
    }

    boolean hasEvidence(String evidenceId) {
        return discoveredEvidence.containsKey(evidenceId);
    }

    String evidenceFor(String evidenceId) {
        return discoveredEvidence.get(evidenceId);
    }

    Inventory inventory() {
        return inventory;
    }

    boolean isReliefOfficer() {
        return reliefOfficer;
    }

    void appointReliefOfficer() {
        reliefOfficer = true;
    }

    IssueOrderCommand.Status correctionOrder() {
        return correctionOrder;
    }

    void setCorrectionOrder(IssueOrderCommand.Status status) {
        correctionOrder = status;
    }

    List<String> evidence() {
        return List.copyOf(discoveredEvidence.values());
    }
}
