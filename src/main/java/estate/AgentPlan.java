package estate;

import java.util.ArrayList;
import java.util.List;

final class AgentPlan {
    static final int MAX_STEPS = 8;

    record Snapshot(List<String> steps, Status status, Integer nextStepIndex) {}

    enum Status {
        DRAFT, APPROVED, PAUSED, COMPLETED, CANCELED
    }

    private final List<CommandFactory.Action> steps;
    private Status status = Status.DRAFT;
    private int nextStepIndex;

    AgentPlan(List<CommandFactory.Action> steps) {
        this.steps = List.copyOf(steps);
    }

    Snapshot snapshot() {
        return new Snapshot(steps.stream().map(CommandFactory.Action::text).toList(), status, nextStepIndex);
    }

    static AgentPlan restore(Snapshot saved) {
        if (saved.steps() == null || saved.status() == null || saved.nextStepIndex() == null) {
            throw new IllegalArgumentException("Incomplete saved plan.");
        }
        int size = saved.steps().size();
        int cursor = saved.nextStepIndex();
        if (size == 0 || size > MAX_STEPS || cursor < 0 || cursor > size
                || (saved.status() == Status.COMPLETED) != (cursor == size)
                || (saved.status() == Status.DRAFT && cursor != 0)) {
            throw new IllegalArgumentException("Invalid saved plan progress.");
        }

        AgentPlan restored = new AgentPlan(saved.steps().stream().map(GameAgent::parsePlanStep).toList());
        restored.nextStepIndex = cursor;
        restored.status = saved.status() == Status.APPROVED ? Status.PAUSED : saved.status();
        return restored;
    }

    boolean finished() {
        return status == Status.COMPLETED || status == Status.CANCELED;
    }

    List<String> describe() {
        List<String> lines = new ArrayList<>();
        lines.add("Plan: " + status);
        for (int index = 0; index < steps.size(); index++) {
            String progress = index < nextStepIndex ? " [done]" : "";
            lines.add((index + 1) + ". " + steps.get(index).text() + progress);
        }
        return lines;
    }

    String approve() {
        if (status != Status.DRAFT && status != Status.PAUSED) {
            return "Only a draft or paused plan can be approved.";
        }
        status = Status.APPROVED;
        return "Plan approved. Use plan run to execute the remaining steps.";
    }

    String cancel() {
        if (finished()) {
            return "This plan has already ended.";
        }
        status = Status.CANCELED;
        return "Plan canceled.";
    }

    List<String> run(GameController controller) {
        if (status != Status.APPROVED) {
            return List.of("Approve the plan before running it.");
        }
        List<String> results = new ArrayList<>();
        while (nextStepIndex < steps.size()) {
            CommandFactory.Action step = steps.get(nextStepIndex);
            ActionResult result = controller.execute(step);
            results.add(step.text() + " | " + result.message());
            if (!result.success()) {
                status = Status.PAUSED;
                results.add("Plan paused. Resolve the issue and approve again to retry this step.");
                return results;
            }
            nextStepIndex++;
        }
        status = Status.COMPLETED;
        results.add("Plan completed.");
        return results;
    }
}
