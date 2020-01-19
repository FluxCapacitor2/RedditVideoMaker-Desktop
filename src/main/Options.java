package main;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

public class Options {
    JPanel frame;
    JButton saveAndStartBtn;
    JCheckBox alwaysOnTop;
    JComboBox<String> bg;
    JButton closeBtn;
    JTextField dlFolder;
    JTextField libFolder;
    JTextField outFolder;
    JButton editManifestBtn;

    private ArrayList<File> backgrounds;

    Options() {

        saveAndStartBtn.addActionListener(e -> save());
        closeBtn.addActionListener(e -> close());
        editManifestBtn.addActionListener(e -> edit());

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
        //ALWAYS ON TOP
        alwaysOnTop.setSelected(Config.getAlwaysOnTop());

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

    private void edit() {
        //Main.configFrame.setVisible(false);
        try {
            new EditManifest(Main.getManifest(Main.getDLid()).comments);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
        /*
        Always On Top
         */
        Config.setAlwaysOnTop(alwaysOnTop.isSelected());
        //Done saving settings!
        Main.out("Settings saved.");
        //Now start RVM.
        close();
        Main.onAfterOptionsMenuClosed();
    }

    void close() {
        //Main.configFrame.setVisible(false);
    }
}
