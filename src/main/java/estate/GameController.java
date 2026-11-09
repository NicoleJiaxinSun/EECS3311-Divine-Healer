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

    ActionResult requestReview(String npcId) {
        return TalkCommand.review(npcId.toLowerCase(Locale.ROOT)).execute(state);
    }

    ActionResult submitEvidence(String evidenceId, String npcId) {
        GameCommand command = new TalkCommand(
                npcId.toLowerCase(Locale.ROOT), evidenceId.toLowerCase(Locale.ROOT));
        return command.execute(state);
    }

    ActionResult requestAppointment() {
        return TalkCommand.requestAppointment(state);
    }

    ActionResult allocateGrain(int quantity, String recipient) {
        return new AllocateCommand(quantity, recipient.toLowerCase(Locale.ROOT)).execute(state);
    }

    ActionResult operateOrder(IssueOrderCommand.Step step) {
        return new IssueOrderCommand(step).execute(state);
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
