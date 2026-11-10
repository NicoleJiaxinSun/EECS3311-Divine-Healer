package estate;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Scanner;

public final class AgentProbe {
    public static void main(String[] args) {
        LLMClient client = switch (args[0]) {
            case "live" -> new DeepSeekAdapter(System.getenv("DEEPSEEK_API_KEY"));
            case "unavailable" -> (instructions, input) -> {
                throw new IOException("Simulated provider outage.");
            };
            case "fixture" -> (instructions, input) -> JsonParser.parseString(args[1]).getAsJsonObject();
            default -> throw new IllegalArgumentException("Unknown probe mode.");
        };
        GameSession game = new GameSession(client);
        Gson gson = new Gson();
        try (Scanner input = new Scanner(System.in)) {
            while (input.hasNextLine()) {
                System.out.println(gson.toJson(game.run(input.nextLine())));
            }
        }
    }
}
