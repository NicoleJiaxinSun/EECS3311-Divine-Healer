package estate;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public final class GameGUI {
    private final GameSession session = new GameSession(
            new DeepSeekAdapter(System.getenv("DEEPSEEK_API_KEY")));
    private final JTextArea output = new JTextArea(22, 72);
    private final JTextField input = new JTextField();
    private final JComboBox<String> mode = new JComboBox<>(new String[] {"Command", "AI action", "AI goal"});
    private final JLabel activity = new JLabel("Ready");
    private final List<JButton> buttons = new ArrayList<>();
    private boolean busy;

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Swing requires a graphical desktop. Run estate.GameCLI in this environment.");
            return;
        }
        SwingUtilities.invokeLater(() -> new GameGUI().show());
    }

    private void show() {
        JFrame frame = new JFrame(GameSession.TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel shortcuts = new JPanel(new GridLayout(2, 5, 6, 6));
        addButton(shortcuts, "Status", () -> run("status"));
        addButton(shortcuts, "Evidence", () -> run("evidence"));
        addButton(shortcuts, "Events", () -> run("events"));
        addButton(shortcuts, "History", () -> run("history"));
        addButton(shortcuts, "Recap", () -> run("recap"));
        addButton(shortcuts, "Show plan", () -> run("plan show"));
        addButton(shortcuts, "Approve plan", () -> run("plan approve"));
        addButton(shortcuts, "Run plan", () -> run("plan run"));
        addButton(shortcuts, "Cancel plan", () -> run("plan cancel"));
        addButton(shortcuts, "Help", () -> run("help"));
        content.add(shortcuts, BorderLayout.NORTH);

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setText("Try \"inspect mill\" in Command mode.\n");
        content.add(new JScrollPane(output), BorderLayout.CENTER);

        JPanel entry = new JPanel(new BorderLayout(8, 0));
        mode.getAccessibleContext().setAccessibleName("Input mode");
        input.getAccessibleContext().setAccessibleName("Game input");
        input.addActionListener(event -> submit());
        entry.add(mode, BorderLayout.WEST);
        entry.add(input, BorderLayout.CENTER);
        addButton(entry, "Send", this::submit, BorderLayout.EAST);

        JPanel footer = new JPanel(new BorderLayout(0, 6));
        footer.add(entry, BorderLayout.NORTH);
        footer.add(activity, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.SOUTH);

        frame.setContentPane(content);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        input.requestFocusInWindow();
    }

    private void addButton(JPanel panel, String label, Runnable action) {
        addButton(panel, label, action, null);
    }

    private void addButton(JPanel panel, String label, Runnable action, String position) {
        JButton button = new JButton(label);
        button.addActionListener(event -> action.run());
        buttons.add(button);
        panel.add(button, position);
    }

    private void submit() {
        String text = input.getText().strip();
        if (text.isEmpty() || busy) {
            return;
        }
        String prefix = switch (mode.getSelectedIndex()) {
            case 1 -> "act ";
            case 2 -> "goal ";
            default -> "";
        };
        input.setText("");
        run(prefix + text);
    }

    private void run(String command) {
        if (busy) {
            return;
        }
        append("> " + command);
        setBusy(true);
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return session.run(command);
            }

            @Override
            protected void done() {
                try {
                    get().forEach(GameGUI.this::append);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    append("Command interrupted.");
                } catch (ExecutionException exception) {
                    append("The command could not be completed.");
                } finally {
                    setBusy(false);
                    input.requestFocusInWindow();
                }
            }
        }.execute();
    }

    private void append(String line) {
        output.append(line + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }

    private void setBusy(boolean value) {
        busy = value;
        activity.setText(value ? "Working…" : "Ready");
        buttons.forEach(button -> button.setEnabled(!value));
        mode.setEnabled(!value);
        input.setEnabled(!value);
    }
}
