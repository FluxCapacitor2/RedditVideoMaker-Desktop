package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThumbnailGenerator {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    private static int fontSize = 160;
    private static int lineHeight = fontSize + 10;
    private static String videoTitle = null;

    static String generateThumbnail(String DLid, String t, String subreddit) throws IOException {

        videoTitle = t;

        int horizontalMargin = 40;
        int topMargin = 375;
        int highlightHorizontalMargin = 20;
        int highlightVerticalMargin = -20;

        String thumbnailPath = Config.getDownloadsFolder() + "/rvm_final_" + DLid + "_thumbnail.png";
        File outputFile = new File(thumbnailPath);

        if (outputFile.exists()) {
            //If the image already exists, skip the entire process and just use the exiting one.
            return thumbnailPath;
        }
/*
        videoTitle = JOptionPane.showInputDialog(null,
                "Text to be shown in thumbnail:\n\n(Originally \"" + videoTitle + "\")", videoTitle);
*/
        do {
            fontSize -= 10;
            lineHeight = fontSize + 10;
            //Generate a thumbnail for YouTube
            BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = bi.getGraphics();
            //Draw a dark gray background (instead of just black)
            graphics.setColor(new Color(41, 41, 41));
            graphics.fillRect(0, 0, 1920, 1080);
            //Add the reddit icon in the top left corner
            graphics.drawImage(ImageIO.read(new File(Config.getLibraryFolder() + "/reddit_logo.png")), 40, 40, null);
            //Add the text (title of the post)
            graphics.setColor(new Color(255, 255, 255));
            graphics.setFont(new Font("Roboto", Font.PLAIN, fontSize));
            //Figure out when we need to wrap the text
            FontMetrics fontMetrics = graphics.getFontMetrics();
            //Highlight all of the text.
            graphics.setColor(new Color(172, 74, 74));
            //Wrap the lines.
            ArrayList<String> lines = new ArrayList<>(LineBreak.wrap(videoTitle, fontMetrics, WIDTH - horizontalMargin));
            Main.out("Wrapped text = " + lines);
            //Find text that should be highlighted.
            Pattern pattern = Pattern.compile("<(.*?)>", Pattern.DOTALL);
            int j = 0;
            for (String line : lines) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    //Highlight the word we found!
                    String match = matcher.group(1);
                    //Calculate the position of the word/phrase.
                    //Get the width of the phrase.
                    int w = fontMetrics.stringWidth(match);
                    //Calculate the starting position of it.
                    String l = line.replaceAll("[<>]", "");
                    for (String word : line.split(" ")) {
                        if (word.startsWith("<" + match) | word.endsWith(match + ">")) {
                            String beforeMatch = l.substring(0, l.indexOf(match));
                            int beforeMatchWidth = fontMetrics.stringWidth(beforeMatch);
                            int posX = horizontalMargin + beforeMatchWidth - highlightHorizontalMargin;
                            int posY = (j - 1) * lineHeight + topMargin + 30 - highlightVerticalMargin;
                            int drawW = w + (highlightHorizontalMargin * 2);
                            int drawH = lineHeight + highlightVerticalMargin;
                            graphics.fillRoundRect(posX, posY, drawW, drawH, lineHeight / 2, lineHeight / 2);
                            System.out.println("Drawing emphasis for text \"" + match + "\" at x=" + posX + ",y=" + posY +
                                    ",w=" + drawW + ",h=" + drawH + " (full line text = " + l + ")");

                            Matcher lineMatcher = pattern.matcher(lines.get(j));
                            while (lineMatcher.find()) {
                                System.out.println("\tChecking for match...");
                                String match2 = lineMatcher.group(1);
                                if (match2.equals(match)) {
                                    System.out.println("\tFound match...");
                                    //We found the current highlighted text in the line
                                    if (lineMatcher.find()) {
                                        System.out.println("\tThere's another match!");
                                        //There's another highlighted word after this current word.
                                        System.out.println("\tThere is another word after the current highlighted word. Removing margins...");
                                        graphics.fillRect(posX + (lineHeight / 2) +
                                                (fontMetrics.stringWidth(lineMatcher.group(1)) / 3), posY, drawW, drawH);
                                    }
                                }
                            }
                        }

                        //System.out.println("l = " + l);
                        //System.out.println("Replacing text... (" + lines.get(j) + " => " + l + ")");
                        lines.set(j, lines.get(j).replaceAll("[<>]", ""));
                    }
                    //Use these coordinates to draw a rounded rectangle.
                }
                j++;
            }
            //Draw the wrapped text after we draw the highlights.
            //We have to draw each line individually because graphics#drawString doesn't respect multi-line strings.
            graphics.setColor(new Color(255, 255, 255));
            for (int i = 0; i < lines.size(); i++) {
                Main.out("Drawing line \"" + lines.get(i) + "\" on thumbnail...");
                graphics.drawString(lines.get(i), horizontalMargin, topMargin + (lineHeight * i));
            }
            //Add the subreddit text
            graphics.setColor(new Color(255, 255, 255));
            graphics.setFont(new Font("Roboto Thin", Font.PLAIN, 100));
            graphics.drawString("r/", 250, 155);
            graphics.setFont(new Font("Roboto Black", Font.PLAIN, 100));
            graphics.drawString(subreddit.substring(3, subreddit.length() - 1),
                    250 + graphics.getFontMetrics(new Font("Roboto Thin", Font.PLAIN, 100)).stringWidth("r/"), 155);
            //Add the exquisite reddit "ER" logo in the bottom right
            graphics.drawImage(ImageIO.read(new File(Config.getLibraryFolder() + "/er_logo.png")), 1650, 40, 200, 200, null);
            //Save the image
            ImageIO.write(bi, "png", outputFile);
            //Desktop.getDesktop().open(outputFile);
            //Instead of just opening it in Photos, display it in the window itself:
            Main.gui.imageView.setIcon(new ImageIcon(new ImageIcon(ImageIO.read(outputFile)).getImage().getScaledInstance(WIDTH / 2, HEIGHT / 2, Image.SCALE_SMOOTH)));
            Main.gui.scrollPane.setViewportView(Main.gui.imageView);
        } while (checkFontSize());
        Main.gui.scrollPane.setViewportView(Main.gui.logArea);
        return thumbnailPath;
    }

    private static boolean checkFontSize() {
        int response = JOptionPane.showOptionDialog(Main.getGUI(), "Please select an option.", "Thumbnail Generator",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Done", "Font size is too large", "Font size is too small", "Edit Text", "Quit"}, "Done");
        if (response == 0) return false;
        else if (response == 1) return true;
        else if (response == 2) {
            fontSize += 20;
            lineHeight = fontSize + 10;
            return true;
        } else if (response == 3) {
            videoTitle = JOptionPane.showInputDialog(Main.getGUI(),
                    "Please enter the new text to display in the thumbnail:\n\n(Originally \"" + videoTitle + "\")", videoTitle);
            fontSize = 160;
            return true;
        } else if (response == 4) {
            Main.exit(0);
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        generateThumbnail(System.currentTimeMillis() + "-TEST", "Abuse your 9 year old student? Enjoy <losing> <your> <career> <forever>!", "/r/ProRevenge/");
    }
}
