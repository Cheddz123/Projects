

import java.io.*;
import java.util.Map;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ColorQuantizer implements ColorQuantizer_Inter {

    private Pixel[][] pixelArray;
    private ColorMapGenerator_Inter colorMapGenerator;

    public ColorQuantizer(Pixel[][] pixelArray, ColorMapGenerator_Inter gen) {

        this.pixelArray = pixelArray;
        this.colorMapGenerator = gen;

    }

    // read from the file with a bitmap reader
    public ColorQuantizer(String bmpFilename, ColorMapGenerator_Inter gen) {
        try {
            BufferedImage image = ImageIO.read(new File(bmpFilename));
            this.pixelArray = Util.convertBitmapToPixelMatrix(image);
            this.colorMapGenerator = gen;

        } catch (IOException e) {
            System.out.println("Error");
        }

    }

    /**
     * Performs color quantization using the color map generator specified when
     * this quantizer was constructed.
     *
     * @param numColors number of colors to use for color quantization
     * @return A two dimensional array where each index represents the pixel
     *         from the original bitmap image and contains a Pixel representing its
     *         color after quantization
     */
    public Pixel[][] quantizeTo2DArray(int numColors) {
        // generate a color palette with the specified number of colors using
        // colorMapGenerator
        Pixel[] colorPalette = colorMapGenerator.generateColorPalette(pixelArray, numColors);

        // Generate a mapping from original colors to palette colors, this is the data
        // structure that holds the reduced colors
        Map<Pixel, Pixel> colorMap = colorMapGenerator.generateColorMap(pixelArray, colorPalette);

        // create a pixel array with the same dimentions as the og.
        int height = pixelArray.length;
        int width = (height > 0) ? pixelArray[0].length : 0;
        Pixel[][] quantizedArray = new Pixel[height][width];

        // Replace each pixel with its mapped color from the reduced palette
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                Pixel ogPixel = pixelArray[i][j];
                Pixel newPixel = colorMap.get(ogPixel);
                quantizedArray[i][j] = newPixel;
            }
        }
        return quantizedArray;

    }

    /**
     * Performs color quantization using the color map generator specified when
     * this quantizer was constructed. Rather than returning the pixel array,
     * this method writes the resulting image in bmp format to the specified
     * file.
     *
     * @param numColors number of colors to use for color quantization
     * @param fileName  File to write resulting image to
     */
    public void quantizeToBMP(String fileName, int numColors) {
        try {
            // Calls the quantizeTo2DArray method that you've already implemented to perform
            // the color quantization
            // and get the resulting 2D array of pixels
            Pixel[][] newPixelArray = quantizeTo2DArray(numColors);
            Util.savePixelMatrixToBitmap(fileName, newPixelArray);
        }

        catch (Exception e) {
            System.out.println("Error");

        }

    }

}
