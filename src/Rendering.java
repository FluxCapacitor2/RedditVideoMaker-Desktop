import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class Rendering {

    JPanel panel;
    JLabel title;
    JPanel centerPanel;
    JTextArea logArea;
    JPanel centerCenterPanel;
    JProgressBar progressBar;
    JLabel progressLabel;
    JPanel bottomPanel;
    JButton stopBtn;
    JLabel timeRemaining;
    JScrollPane scrollPane;
    JLabel imageView;

    Options options;

    Rendering() {

        options = new Options();

        imageView = new JLabel();
        imageView.setText("");
        imageView.setMaximumSize(new Dimension(500, 500));

        stopBtn.addActionListener(e -> System.exit(2));

        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }
}
