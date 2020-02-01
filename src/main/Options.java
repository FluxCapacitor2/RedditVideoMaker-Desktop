package main;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Options {
    JPanel frame;
    JComboBox<String> bg;
    JButton saveBtn;
    JTextField dlFolder;
    JTextField libFolder;
    JTextField outFolder;

    private ArrayList<File> backgrounds;

    Options() {
        JFrame jframe = new JFrame();
        jframe.setContentPane(frame);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(500, 250);
        jframe.setVisible(true);

        saveBtn.addActionListener(e -> save());

        File dir = new File(Config.getLibraryFolder() + "/backgrounds/");
        if (dir.listFiles() != null) {
            File[] files = dir.listFiles();
            if (files == null) files = new File[]{};
            backgrounds = new ArrayList<>(Arrays.asList(files));
        } else {
            backgrounds = new ArrayList<>();
        }

        bg.removeAllItems();
        bg.addItem("Random");
        for (File f : backgrounds) {
            bg.addItem(f.getName());
        }

        /*
        Load current values for the options.
         */

        //BACKGROUND
        if (Config.getBackground() != null) {
            bg.setSelectedItem(new File(Config.getBackground()).getName());
        } else {
            bg.setSelectedIndex(0);
        }

        //FOLDER STRUCTURE
        dlFolder.setText(Config.getDownloadsFolder());
        libFolder.setText(Config.getLibraryFolder());
        outFolder.setText(Config.getOutputFolder());

        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        dlFolder.addActionListener(e -> {
            int returnVal = fc.showDialog(null, "Select Folder");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                dlFolder.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        libFolder.addActionListener(e -> {
            int returnVal = fc.showDialog(null, "Select Folder");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                libFolder.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        outFolder.addActionListener(e -> {
            int returnVal = fc.showDialog(null, "Select Folder");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                outFolder.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        libFolder.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                File dir = new File(Config.getLibraryFolder() + "/backgrounds/");
                if (dir.listFiles() != null) //noinspection ConstantConditions
                    backgrounds = new ArrayList<>(Arrays.asList(dir.listFiles()));
                else backgrounds = new ArrayList<>();

                bg.removeAllItems();
                bg.addItem("Random");
                for (File f : backgrounds) {
                    bg.addItem(f.getName());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    public static void main(String[] args) {
        new Options();
    }

    private void save() {
        /*
        Folder Structure
         */
        Config.setDownloadsFolder(dlFolder.getText());
        Config.setLibraryFolder(libFolder.getText());
        Config.setOutputFolder(outFolder.getText());
        /*
        Background
         */
        int index = bg.getSelectedIndex() - 1;
        //If they selected "Random", choose a random one when it asks which background to use.
        if (index == -1) {
            Config.setBackground("null");
        } else {
            Config.setBackground(backgrounds.get(index).getAbsolutePath());
        }
        //Done saving settings!
        Main.out("Settings saved.");
        JOptionPane.showMessageDialog(null, "Settings saved.");
    }
}
