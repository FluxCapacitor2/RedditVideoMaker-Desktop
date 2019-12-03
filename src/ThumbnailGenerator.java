import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ThumbnailGenerator {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    public static void main(String[] args) throws IOException {
        String title = Main.requestUserInput("Please enter the video title.");
        String DLid = Main.requestUserInput("Please enter the download ID associated with the output file.");

        VideoManifest m = new VideoManifest();
        m.title = title;
        VideoManifestComment comment = new VideoManifestComment();
        comment.DLid = DLid;
        m.comments = new VideoManifestComment[]{comment};
        generateThumbnail(m);
    }

    static String generateThumbnail(VideoManifest vm) throws IOException {
        String thumbnailPath;
        int fontSize = 160;
        vm.title = JOptionPane.showInputDialog(null,
                "Text to be shown in thumbnail:\n\n(Originally \"" + vm.title + "\")", vm.title);
        do {
            fontSize -= 10;
            //Generate a thumbnail for YouTube
            BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = bi.getGraphics();
            //Draw a dark gray background (instead of just black)
            graphics.setColor(new Color(41, 41, 41));
            graphics.fillRect(0, 0, 1920, 1080);
            //Add the reddit icon in the top left corner
            graphics.drawImage(ImageIO.read(new File(Main.LIBRARY_FOLDER + "/reddit_logo.png")), 40, 40, null);
            //Add the text (title of the post)
            graphics.setColor(new Color(255, 255, 255));
            graphics.setFont(new Font("Roboto", Font.PLAIN, fontSize));
            //Figure out when we need to wrap the text
            FontMetrics fontMetrics = graphics.getFontMetrics();
            ArrayList<String> lines = new ArrayList<>(LineBreak.wrap(vm.title, fontMetrics, WIDTH - 400));
            Main.out("Wrapped text = " + lines);
            //We have to draw each line individually because graphics#drawString doesn't respect multi-line strings.
            for (int i = 0; i < lines.size(); i++) {
                Main.out("Drawing line \"" + lines.get(i) + "\" on thumbnail...");
                graphics.drawString(lines.get(i), 40, 350 + (150 * i));
            }
            //Add the subreddit text
            graphics.setColor(new Color(255, 255, 255));
            graphics.setFont(new Font("Roboto Thin", Font.PLAIN, 100));
            graphics.drawString(vm.subreddit.substring(1, vm.subreddit.length() - 1), 250, 155);
            //Add the exquisite reddit "ER" logo in the bottom right
            graphics.drawImage(ImageIO.read(new File(Main.LIBRARY_FOLDER + "/er_logo.png")), 1650, 40, 200, 200, null);
            //Save the image
            thumbnailPath = Main.DOWNLOADS_FOLDER + "/rvm_final_" + vm.comments[0].DLid + "_thumbnail.png";
            File outputFile = new File(thumbnailPath);
            ImageIO.write(bi, "png", outputFile);
            //Desktop.getDesktop().open(outputFile);
            //Instead of just opening it in Photos, display it in the window itself:
            Main.gui.imageView.setIcon(new ImageIcon(new ImageIcon(ImageIO.read(outputFile)).getImage().getScaledInstance(WIDTH / 2, HEIGHT / 2, Image.SCALE_SMOOTH)));
            Main.gui.scrollPane.setViewportView(Main.gui.imageView);

        } while (checkFontSize());
        return thumbnailPath;
    }

    private static boolean checkFontSize() {
        boolean y = Main.requestUserYesOrNo("Is the font size too large in this image?");
        Main.gui.scrollPane.setViewportView(Main.gui.logArea);
        return y;
    }
}
