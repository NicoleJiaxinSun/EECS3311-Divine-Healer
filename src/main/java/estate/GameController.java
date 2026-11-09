package estate;

import java.util.List;
import java.util.Locale;

final class GameController {
    private final GameState state = new GameState();
    private final EventManager eventManager = new EventManager();
    private final HistoryRecorder historyRecorder = new HistoryRecorder();
    private final CommandExecutor executor = new CommandExecutor(List.of(eventManager, historyRecorder));

    ActionResult inspect(String householdId) {
        String target = householdId.strip().toLowerCase(Locale.ROOT);
        if (target.isEmpty()) {
            return new ActionResult(false, "Usage: inspect <household>");
        }

        return execute("inspect " + target, new InspectCommand(target));
    }

    ActionResult requestReview(String npcId) {
        String npc = npcId.toLowerCase(Locale.ROOT);
        return execute("talk " + npc, TalkCommand.review(npc));
    }

    ActionResult submitEvidence(String evidenceId, String npcId) {
        String npc = npcId.toLowerCase(Locale.ROOT);
        String evidence = evidenceId.toLowerCase(Locale.ROOT);
        return execute("submit " + evidence + " " + npc, new TalkCommand(npc, evidence));
    }

    ActionResult requestAppointment() {
        return execute("appoint", TalkCommand::requestAppointment);
    }

    ActionResult allocateGrain(int quantity, String recipient) {
        String target = recipient.toLowerCase(Locale.ROOT);
        return execute("allocate " + quantity + " " + target, new AllocateCommand(quantity, target));
    }

    ActionResult operateOrder(IssueOrderCommand.Step step) {
        return execute("order " + step.name().toLowerCase(Locale.ROOT), new IssueOrderCommand(step));
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
