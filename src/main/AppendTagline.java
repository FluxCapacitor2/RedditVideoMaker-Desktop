package main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AppendTagline {
    public static void appendTagline(File in, File tag, File out) throws IOException {

        BufferedImage one = ImageIO.read(in);
        BufferedImage two = ImageIO.read(tag);
        BufferedImage both = new BufferedImage(one.getWidth(), one.getHeight() + two.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = both.createGraphics();
        graphics.setColor(new Color(65, 65, 65));
        graphics.fillRect(0, 0, both.getWidth(), both.getHeight());
        graphics.drawImage(one, 0, two.getHeight(), one.getWidth(), one.getHeight(), null);
        graphics.drawImage(two, 0, 0, two.getWidth(), two.getHeight(), null);

        ImageIO.write(both, "png", out);
    }
}
