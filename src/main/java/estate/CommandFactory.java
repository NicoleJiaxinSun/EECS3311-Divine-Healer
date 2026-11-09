package estate;

import java.util.Locale;

final class CommandFactory {
    record Action(String text, GameCommand command) {}

    static Action parse(String input) {
        String[] parts = input.strip().toLowerCase(Locale.ROOT).split("\\s+");
        return switch (parts[0]) {
            case "inspect" -> {
                requireLength(parts, 2, "inspect <household>");
                yield new Action("inspect " + parts[1], new InspectCommand(parts[1]));
            }
            case "talk" -> {
                requireLength(parts, 2, "talk <npc>");
                yield new Action("talk " + parts[1], TalkCommand.review(parts[1]));
            }
            case "submit" -> {
                requireLength(parts, 3, "submit <evidence-id> <npc>");
                yield new Action(String.join(" ", parts), new TalkCommand(parts[2], parts[1]));
            }
            case "appoint" -> {
                requireLength(parts, 1, "appoint");
                yield new Action("appoint", TalkCommand::requestAppointment);
            }
            case "allocate" -> {
                requireLength(parts, 3, "allocate <grain-quantity> <recipient>");
                try {
                    int quantity = Integer.parseInt(parts[1]);
                    yield new Action(String.join(" ", parts), new AllocateCommand(quantity, parts[2]));
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("Grain quantity must be a whole number.");
                }
            }
            case "order" -> {
                String usage = "order <request|approve|execute>";
                requireLength(parts, 2, usage);
                IssueOrderCommand.Step step;
                try {
                    step = IssueOrderCommand.Step.valueOf(parts[1].toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("Usage: " + usage);
                }
                yield new Action(String.join(" ", parts), new IssueOrderCommand(step));
            }
            default -> throw new IllegalArgumentException("Unsupported game action.");
        };
    }

    private static void requireLength(String[] parts, int expected, String usage) {
        if (parts.length != expected) {
            throw new IllegalArgumentException("Usage: " + usage);
        }
    }
}
