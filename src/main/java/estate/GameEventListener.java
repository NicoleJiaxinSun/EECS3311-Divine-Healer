package estate;

interface GameEventListener {
    void onGameEvent(GameState state, String action, ActionResult result);
}
