


import java.util.HashMap;
import java.util.Map;

public class BucketingMapGenerator implements ColorMapGenerator_Inter {

    public BucketingMapGenerator() {

    }

    /**
     * Produces an initial palette. For bucketing implementations, the initial
     * palette will be the centers of the evenly-divided buckets. For clustering
     * implementations, the initial palette will be the initial centroids. When
     * needed, a distance metric should be specified when the color map
     * generator is constructed.
     *
     * @param pixelArray the 2D Pixel array that represents a bitmap image
     * @param numColors  the number of desired colors in the palette
     * @return a Pixel array containing numColors elements
     */
    public Pixel[] generateColorPalette(Pixel[][] pixelArray, int numColors) {

        if (numColors <= 0) {
            throw new IllegalArgumentException("Number of colors must be positive");
        }
        
        Pixel[] colorPalette = new Pixel[numColors];

        // Calculate bucket size based on 24-bit color space
        long totalColors = 1L << 24; // 2^24
        double bucketSize = (double) totalColors / numColors;

        for (int i = 0; i < numColors; i++) {
            // Calculate center of bucket
            long centerValue = (long) (bucketSize * i + bucketSize / 2);

            // Extract RGB components
            int red = (int) ((centerValue >> 16) & 0xFF);
            int green = (int) ((centerValue >> 8) & 0xFF);
            int blue = (int) (centerValue & 0xFF);

            colorPalette[i] = new Pixel(red, green, blue);
        }

        return colorPalette;
    }

    /**
     * Computes the reduced color map. For bucketing implementations, this will
     * map each color to the center of its bucket. For clustering
     * implementations, this will map each color to its final centroid. When
     * needed, a distance metric should be specified when the color map
     * generator is constructed.
     *
     * @param pixelArray          the pixels array that represents a bitmap image
     * @param initialColorPalette an initial color palette, such as those
     *                            generated by generateColorPalette, represented as
     *                            an array of Pixels
     * @return A Map that maps each distinct color in pixelArray to a final
     *         color
     */
    public Map<Pixel, Pixel> generateColorMap(Pixel[][] pixelArray, Pixel[] initialColorPalette) {
        Map<Pixel, Pixel> colorMap = new HashMap<>();

        long totalColors = 1L << 24; // 2^24
        double bucketSize = (double)totalColors / initialColorPalette.length;

        // Create a set of all unique colors in the image
        for (Pixel[] row : pixelArray) {
            for (Pixel pixel : row) {
                // If we haven't mapped this color yet
                if (!colorMap.containsKey(pixel)) {
                    // Convert pixel to 24-bit integer value
                    int pixelVal = (pixel.getRed() << 16) | (pixel.getGreen() << 8) | pixel.getBlue();

                    // Determine which bucket this pixel belongs to
                    int bucketIndex = (int) (pixelVal / bucketSize);

                    // Handle edge case
                    if (bucketIndex >= initialColorPalette.length) {
                        bucketIndex = initialColorPalette.length - 1;
                    }

                    // Map this pixel to the corresponding color in the palette
                    colorMap.put(pixel, initialColorPalette[bucketIndex]);
                }
            }
        }
        return colorMap;
    }
}
