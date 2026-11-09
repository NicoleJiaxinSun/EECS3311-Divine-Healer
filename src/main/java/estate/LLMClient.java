package estate;

import com.google.gson.JsonObject;
import java.io.IOException;

interface LLMClient {
    JsonObject generate(String instructions, String input) throws IOException;
}
