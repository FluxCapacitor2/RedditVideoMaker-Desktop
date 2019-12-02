import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class EditManifest {
    JPanel parentPanel;
    private JScrollPane scrollPane;
    private JPanel panel;
    private JButton saveBtn;
    private JFrame frame;
    private VideoManifestComment[] comments;

    public EditManifest(VideoManifestComment[] comments) {
        this.comments = comments;
        frame = new JFrame("Grid Bag Example");
        frame.setSize(1920, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(parentPanel);
        GridBagLayout gbl = new GridBagLayout();
        panel.setLayout(gbl);

        saveBtn.addActionListener(e -> save());

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        frame.setVisible(true);

        renderComments(comments);
    }

    private void save() {
        try {
            VideoManifest vm = Main.getManifest(Main.getDLid());
            vm.comments = comments;
            //Save the manifest
            Gson gson = new Gson();
            String json = gson.toJson(vm);
            FileWriter fw = new FileWriter(new File(Config.getDownloadsFolder() + "/rvm_manifest_" + Main.getDLid() + ".json"), false);
            fw.write(json);
            fw.close();
            JOptionPane.showMessageDialog(frame, "Saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
            //Continue with the program by re-opening the options menu.
            frame.setVisible(false);
            Main.openOptionsMenu();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void renderComments(VideoManifestComment[] comments) {

        panel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridy = 0;
        gbc.gridx = 0;

        for (int i = 0; i < comments.length; i++) {
            VideoManifestComment c = comments[i];
            gbc.gridy += 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            Main.out("(" + gbc.gridx + ", " + gbc.gridy + ") jLabel1");
            Graphics g = frame.getGraphics();
            FontMetrics fm = g.getFontMetrics();
            gbc.gridx = 0;
            panel.add(new JLabel("#" + i), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel("<html>" + String.join("<br />", LineBreak.wrap(c.text, fm, (int) (frame.getBounds().getWidth() / 2))) + "</html>"), gbc);
            gbc.gridx = 2;
            panel.add(new JLabel(c.DLid), gbc);
            gbc.gridx = 3;
            JButton editBtn = new JButton("Edit");
            int finalI = i;
            editBtn.addActionListener(e -> updateManifest(finalI, JOptionPane.showInputDialog("Edit comment:\n\"" + comments[finalI].text + "\"")));
            panel.add(editBtn, gbc);
            gbc.gridx = 4;
            JButton removeBtn = new JButton("Remove");
            removeBtn.addActionListener(e -> removeFromManifest(finalI));
            panel.add(removeBtn, gbc);
        }

        panel.revalidate();
        panel.repaint();
    }

    private void removeFromManifest(int index) {
        Main.out("Removing index " + index + " from manifest.");
        ArrayList<VideoManifestComment> cs = new ArrayList<>(Arrays.asList(this.comments));
        cs.remove(index);
        this.comments = cs.toArray(new VideoManifestComment[]{});
        renderComments(this.comments);
        Main.out("Removed index " + index + " from manifest.");
    }

    private void updateManifest(int index, String s) {
        Main.out("Changing index " + index + " to " + s);
        if (s != null) {
            ArrayList<VideoManifestComment> cs = new ArrayList<>(Arrays.asList(this.comments));
            VideoManifestComment newComment = cs.get(index);
            newComment.text = s;
            cs.set(index, newComment);
            this.comments = cs.toArray(new VideoManifestComment[]{});
        }
        renderComments(this.comments);
    }

    public void hide() {
        frame.setVisible(false);
    }

    public void dispose() {
        frame.dispose();
    }
}