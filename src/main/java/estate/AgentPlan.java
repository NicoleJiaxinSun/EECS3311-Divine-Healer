package estate;

import java.util.ArrayList;
import java.util.List;

final class AgentPlan {
    enum Status {
        DRAFT, APPROVED, PAUSED, COMPLETED, CANCELED
    }

    private final List<CommandFactory.Action> steps;
    private Status status = Status.DRAFT;
    private int nextStepIndex;

    AgentPlan(List<CommandFactory.Action> steps) {
        this.steps = List.copyOf(steps);
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
