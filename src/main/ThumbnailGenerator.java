package main;

import javax.imageio.ImageIO;
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

    private static int fontSize = 140;
    private static int lineHeight;
    static int horizontalMargin = 40;
    static int topMargin = 375;

    static String generateThumbnail(String DLid, String t, String subreddit) throws IOException {

        lineHeight = fontSize + 10;

        String thumbnailPath = Config.getDownloadsFolder() + "/rvm_final_" + DLid + "_thumbnail.png";
        File outputFile = new File(thumbnailPath);

        if (outputFile.exists()) {
            //If the image already exists, return the path of the existing image.
            return thumbnailPath;
        }

        //Generate a thumbnail for YouTube
        BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) bi.getGraphics();
        //Set some rendering hints to improve the text and image quality
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHints(rh);
        //Draw a dark gray background (instead of just black)
        graphics.setColor(new Color(41, 41, 41));
        graphics.fillRect(0, 0, 1920, 1080);
        //Add the reddit icon in the top left corner
        graphics.drawImage(ImageIO.read(new File(Config.getLibraryFolder() + "/reddit_logo.png")), 40, 40, null);
        //Add the text (title of the post)
        graphics.setColor(new Color(255, 255, 255));
        graphics.setFont(new Font("Roboto", Font.PLAIN, fontSize));
        //Figure out when we need to wrap the text
        FontMetrics fontMetrics = graphics.getFontMetrics(new Font("Roboto", Font.PLAIN, fontSize));
        //Highlight all of the text.
        graphics.setColor(new Color(172, 74, 74));
        //Check the font size before continuing to make sure the whole title fits on the screen
        while (checkFontSize(fontSize, t, graphics.getFontMetrics(new Font("Roboto", Font.PLAIN, fontSize)))) {
            System.out.println("Decreasing font size from " + fontSize + " to " + (fontSize - 1) + ".");
            fontSize -= 1;
            lineHeight = fontSize + 10;
            graphics.setFont(new Font("Roboto", Font.PLAIN, fontSize));
            fontMetrics = graphics.getFontMetrics(new Font("Roboto", Font.PLAIN, fontSize));
        }
        //Wrap the lines.
        ArrayList<String> lines = new ArrayList<>(LineBreak.wrap(t, fontMetrics, WIDTH - horizontalMargin));
        Main.out("Wrapped text = " + lines);
        //Find text that should be highlighted.
        highlightLines(t, lines, fontMetrics, graphics);
        //Draw the wrapped text after we draw the highlights.
        //We have to draw each line individually because graphics#drawString doesn't respect multi-line strings.
        graphics.setColor(new Color(255, 255, 255));
        for (int i = 0; i < lines.size(); i++) {
            Main.out("Drawing line \"" + lines.get(i) + "\" on thumbnail...");
            graphics.drawString(lines.get(i).replaceAll("[<>]", ""), horizontalMargin, topMargin + (lineHeight * i));
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
        return thumbnailPath;
    }

    private static boolean checkFontSize(int fontSize, String t, FontMetrics m) {
        //Return true if the font size is too large or false if it will fit in the thumbnail
        java.util.List<String> lines = LineBreak.wrap(t, m, WIDTH - horizontalMargin);
        return lines.size() * lineHeight > HEIGHT - topMargin;
    }

    private static void highlightLines(String str, ArrayList<String> lines, FontMetrics fontMetrics, Graphics graphics) {
        Pattern pattern = Pattern.compile("<(.*?)>");
        int ln = 0;
        for (String line : lines) {
            int offset = 0;
            //If there are multiple highlighted words on one line, combine the selection into one.
            line = line.replaceAll("<(.*)>[ ]<(.*)>", "<$1 $2>");
            Matcher m = pattern.matcher(line);
            while (m.find()) {
                int start = m.start() - offset;
                int end = m.end() - offset;
                offset += 2;
                String tline = line.replaceAll("[<>]", "");
                System.out.println("Match: " + tline.substring(start, end - 2));
                System.out.println("Position: " + start + "-" + (end - 2));
                System.out.println("Line: " + ln);
                int realPosStart = fontMetrics.stringWidth(tline.substring(0, start));
                int realPosWidth = fontMetrics.stringWidth(tline.substring(start, end - 2));
                int y = lineHeight * (ln - 1) + topMargin + 30;
                int arc = fontSize / 3;

                graphics.fillRoundRect(realPosStart + horizontalMargin, y, realPosWidth, lineHeight - 20, arc, arc);
            }
            ln++;
        }
    }
}
