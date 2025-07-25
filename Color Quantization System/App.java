/**
 * A driver for CS1501 Project 5
 * @author	Dr. Farnan
 */


import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class App {
    public static void main(String[] args) {

        try {
            // Load bitmap image
            BufferedImage image = ImageIO.read(new File("build/resources/main/image.bmp"));

            // Create pixel matrix
            Pixel[][] pixelMatrix = Util.convertBitmapToPixelMatrix(image);

            // Save pixel matrix to file
            Util.savePixelMatrixToFile("build/resources/main/pixel_matrix.txt", pixelMatrix);
        } catch (Exception e) {
            // This is very bad exception handling, but is only a proof of concept
            e.printStackTrace();
        }
    }
}
