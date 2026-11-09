package estate;

import java.util.Locale;
import java.util.Scanner;

public final class GameCLI {
    private static final String HELP = "Commands: inspect <household>, evidence, "
            + "talk <npc>, submit <evidence-id> <npc>, appoint, "
            + "allocate <grain-quantity> <recipient>, order <request|approve|execute>, status, help, quit";

    public static void main(String[] args) {
        GameController controller = new GameController();
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("The Divine Healer's Estate");
            System.out.println(HELP);
            while (input.hasNextLine()) {
                String line = input.nextLine().strip();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+", 2);
                String argument = parts.length == 2 ? parts[1] : "";
                switch (parts[0].toLowerCase(Locale.ROOT)) {
                    case "inspect" -> {
                        ActionResult result = controller.inspect(argument);
                        System.out.println(result.message());
                    }
                    case "talk" -> {
                        if (argument.isEmpty()) {
                            System.out.println("Usage: talk <npc>");
                        } else {
                            System.out.println(controller.requestReview(argument).message());
                        }
                    }
                    case "submit" -> {
                        String[] submission = argument.split("\\s+");
                        if (submission.length != 2) {
                            System.out.println("Usage: submit <evidence-id> <npc>");
                        } else {
                            System.out.println(controller.submitEvidence(submission[0], submission[1]).message());
                        }
                    }
                    case "appoint" -> System.out.println(controller.requestAppointment().message());
                    case "allocate" -> System.out.println(allocate(controller, argument).message());
                    case "order" -> System.out.println(operateOrder(controller, argument).message());
                    case "status" -> System.out.println(controller.status());
                    case "evidence" -> {
                        var evidence = controller.evidence();
                        if (evidence.isEmpty()) {
                            System.out.println("No evidence discovered.");
                        }
                        evidence.forEach(System.out::println);
                    }
                    case "help" -> System.out.println(HELP);
                    case "quit" -> {
                        return;
                    }
                    default -> System.out.println("Unknown command. Type help for available commands.");
                }
            }
        }
    }

    private static ActionResult allocate(GameController controller, String argument) {
        String[] allocation = argument.split("\\s+");
        if (allocation.length != 2) {
            return new ActionResult(false, "Usage: allocate <grain-quantity> <recipient>");
        }
        try {
            return controller.allocateGrain(Integer.parseInt(allocation[0]), allocation[1]);
        } catch (NumberFormatException exception) {
            return new ActionResult(false, "Grain quantity must be a whole number.");
        }
    }

    private static ActionResult operateOrder(GameController controller, String argument) {
        IssueOrderCommand.Step step;
        try {
            step = IssueOrderCommand.Step.valueOf(argument.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return new ActionResult(false, "Usage: order <request|approve|execute>");
        }
        return controller.operateOrder(step);
    }
}
