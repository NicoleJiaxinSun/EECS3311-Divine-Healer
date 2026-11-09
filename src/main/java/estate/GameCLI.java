package estate;

import java.util.Scanner;

public final class GameCLI {
    public static void main(String[] args) {
        GameSession session = new GameSession(new DeepSeekAdapter(System.getenv("DEEPSEEK_API_KEY")));
        try (Scanner input = new Scanner(System.in)) {
            System.out.println(GameSession.TITLE);
            System.out.println(GameSession.HELP + ", quit");
            while (input.hasNextLine()) {
                String line = input.nextLine().strip();
                String command = line.split("\\s+", 2)[0];
                if (command.equalsIgnoreCase("quit")) {
                    return;
                }
                if (command.equalsIgnoreCase("help")) {
                    System.out.println(GameSession.HELP + ", quit");
                } else {
                    session.run(line).forEach(System.out::println);
                }
            }
        }
    }
}
