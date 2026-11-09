package estate;

interface GameCommand {
    ActionResult execute(GameState state);
}
