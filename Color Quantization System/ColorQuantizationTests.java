
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * Enhanced test suite for CS1501 Project 5 - Color Quantization
 * Now supports both synthetic test images and real image files
 */
public class ColorQuantizationTests {

    private Pixel redPixel;
    private Pixel greenPixel;
    private Pixel bluePixel;
    private Pixel whitePixel;
    private Pixel blackPixel;
    private Pixel[][] testImage;
    private Pixel[][] smallTestImage;
    private Pixel[][] realImage;

    void setUp() {
        // Create test pixels
        redPixel = new Pixel(255, 0, 0);
        greenPixel = new Pixel(0, 255, 0);
        bluePixel = new Pixel(0, 0, 255);
        whitePixel = new Pixel(255, 255, 255);
        blackPixel = new Pixel(0, 0, 0);

        // Create synthetic test images
        testImage = new Pixel[][] {
            {redPixel, greenPixel, bluePixel},
            {whitePixel, blackPixel, redPixel},
            {greenPixel, bluePixel, whitePixel}
        };

        smallTestImage = new Pixel[][] {
            {redPixel, greenPixel},
            {bluePixel, whitePixel}
        };

        // Try to load a real image
        realImage = loadRealImage("test_images/sample.bmp");
        if (realImage == null) {
            realImage = loadRealImage("build/resources/main/image.bmp");
        }
        if (realImage == null) {
            realImage = loadRealImage("image.bmp");
        }
        if (realImage == null) {
            System.out.println("No real image found. Tests will use synthetic images only.");
            realImage = testImage; // Fallback to synthetic image
        }
    }

    /**
     * Load a real image file and convert it to a Pixel array
     */
    private Pixel[][] loadRealImage(String filename) {
        try {
            File imageFile = new File(filename);
            if (!imageFile.exists()) {
                return null;
            }
            
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                return null;
            }
            
            System.out.println("Loaded real image: " + filename + " (" + 
                             image.getWidth() + "x" + image.getHeight() + ")");
            
            return Util.convertBitmapToPixelMatrix(image);
        } catch (IOException e) {
            System.out.println("Could not load image: " + filename);
            return null;
        }
    }

    /**
     * Create a directory for test images if it doesn't exist
     */
    private void createTestImageDirectory() {
        File dir = new File("test_images");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ===================== REAL IMAGE TESTS =====================

    void testRealImageQuantization() {
        if (realImage == null) {
            System.out.println("Skipping real image test - no image loaded");
            return;
        }

        System.out.println("\nTesting real image quantization...");
        System.out.println("Original image size: " + realImage.length + "x" + realImage[0].length);
        System.out.println("Original unique colors: " + countUniqueColors(realImage));

        // Test bucketing quantization
        BucketingMapGenerator bucketingGen = new BucketingMapGenerator();
        ColorQuantizer bucketingQuantizer = new ColorQuantizer(realImage, bucketingGen);
        
        Pixel[][] bucketingResult = bucketingQuantizer.quantizeTo2DArray(16);
        System.out.println("After bucketing to 16 colors: " + countUniqueColors(bucketingResult));
        
        // Test clustering quantization
        ClusteringMapGenerator clusteringGen = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        ColorQuantizer clusteringQuantizer = new ColorQuantizer(realImage, clusteringGen);
        
        Pixel[][] clusteringResult = clusteringQuantizer.quantizeTo2DArray(16);
        System.out.println("After clustering to 16 colors: " + countUniqueColors(clusteringResult));
        
        // Verify results
        assert bucketingResult.length == realImage.length;
        assert clusteringResult.length == realImage.length;
        assert bucketingResult[0].length == realImage[0].length;
        assert clusteringResult[0].length == realImage[0].length;
        
        // Save quantized images
        createTestImageDirectory();
        bucketingQuantizer.quantizeToBMP("test_images/bucketing_16colors.bmp", 16);
        clusteringQuantizer.quantizeToBMP("test_images/clustering_16colors.bmp", 16);
        
        System.out.println("Saved quantized images to test_images/");
    }

    void testMultipleColorReductions() {
        if (realImage == null) {
            System.out.println("Skipping multiple color reduction test - no image loaded");
            return;
        }

        System.out.println("\nTesting multiple color reductions...");
        
        BucketingMapGenerator bucketingGen = new BucketingMapGenerator();
        ClusteringMapGenerator clusteringGen = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        
        ColorQuantizer bucketingQuantizer = new ColorQuantizer(realImage, bucketingGen);
        ColorQuantizer clusteringQuantizer = new ColorQuantizer(realImage, clusteringGen);
        
        int[] colorCounts = {2, 4, 8, 16, 32, 64};
        createTestImageDirectory();
        
        for (int numColors : colorCounts) {
            System.out.println("Reducing to " + numColors + " colors...");
            
            // Test bucketing
            Pixel[][] bucketingResult = bucketingQuantizer.quantizeTo2DArray(numColors);
            bucketingQuantizer.quantizeToBMP("test_images/bucketing_" + numColors + "colors.bmp", numColors);
            
            // Test clustering
            Pixel[][] clusteringResult = clusteringQuantizer.quantizeTo2DArray(numColors);
            clusteringQuantizer.quantizeToBMP("test_images/clustering_" + numColors + "colors.bmp", numColors);
            
            // Verify that we actually reduced the colors
            int actualBucketingColors = countUniqueColors(bucketingResult);
            int actualClusteringColors = countUniqueColors(clusteringResult);
            
            System.out.println("  Bucketing actual colors: " + actualBucketingColors);
            System.out.println("  Clustering actual colors: " + actualClusteringColors);
            
            assert actualBucketingColors <= numColors;
            assert actualClusteringColors <= numColors;
        }
    }

    void testHueBasedQuantization() {
        if (realImage == null) {
            System.out.println("Skipping hue-based quantization test - no image loaded");
            return;
        }

        System.out.println("\nTesting hue-based quantization...");
        
        // Compare Euclidean vs Hue-based clustering
        ClusteringMapGenerator euclideanGen = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        ClusteringMapGenerator hueGen = new ClusteringMapGenerator(new CircularHueMetric());
        
        ColorQuantizer euclideanQuantizer = new ColorQuantizer(realImage, euclideanGen);
        ColorQuantizer hueQuantizer = new ColorQuantizer(realImage, hueGen);
        
        createTestImageDirectory();
        
        // Test with different color counts
        int[] colorCounts = {8, 16, 32};
        
        for (int numColors : colorCounts) {
            System.out.println("Comparing metrics with " + numColors + " colors...");
            
            euclideanQuantizer.quantizeToBMP("test_images/euclidean_" + numColors + "colors.bmp", numColors);
            hueQuantizer.quantizeToBMP("test_images/hue_" + numColors + "colors.bmp", numColors);
            
            Pixel[][] euclideanResult = euclideanQuantizer.quantizeTo2DArray(numColors);
            Pixel[][] hueResult = hueQuantizer.quantizeTo2DArray(numColors);
            
            System.out.println("  Euclidean result colors: " + countUniqueColors(euclideanResult));
            System.out.println("  Hue result colors: " + countUniqueColors(hueResult));
        }
    }

    void testImageFromFile() {
        // Test loading an image directly from file using ColorQuantizer constructor
        String[] possiblePaths = {
            "test_images/sample.bmp",
            "build/resources/main/image.bmp",
            "image.bmp"
        };
        
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                System.out.println("Testing image loading from: " + path);
                
                try {
                    BucketingMapGenerator bucketingGen = new BucketingMapGenerator();
                    ColorQuantizer quantizer = new ColorQuantizer(path, bucketingGen);
                    
                    // Test quantization
                    Pixel[][] result = quantizer.quantizeTo2DArray(8);
                    assert result != null;
                    assert result.length > 0;
                    assert result[0].length > 0;
                    
                    // Save result
                    createTestImageDirectory();
                    quantizer.quantizeToBMP("test_images/loaded_from_file_8colors.bmp", 8);
                    
                    System.out.println("Successfully loaded and quantized image from file");
                    break;
                } catch (Exception e) {
                    System.out.println("Could not load image from: " + path + " - " + e.getMessage());
                }
            }
        }
    }

    // ===================== ORIGINAL PIXEL TESTS =====================

    void testPixelConstructorAndGetters() {
        Pixel pixel = new Pixel(128, 64, 192);
        assert pixel.getRed() == 128;
        assert pixel.getGreen() == 64;
        assert pixel.getBlue() == 192;
    }

    void testPixelToString() {
        Pixel pixel = new Pixel(255, 128, 0);
        assert pixel.toString().equals("(255,128,0)");
    }

    void testPixelEquals() {
        Pixel pixel1 = new Pixel(100, 150, 200);
        Pixel pixel2 = new Pixel(100, 150, 200);
        Pixel pixel3 = new Pixel(100, 150, 201);

        assert pixel1.equals(pixel2);
        assert !pixel1.equals(pixel3);
        assert !pixel1.equals(null);
        assert !pixel1.equals("not a pixel");
        assert pixel1.equals(pixel1); // reflexive
    }

    void testPixelHashCode() {
        Pixel pixel1 = new Pixel(100, 150, 200);
        Pixel pixel2 = new Pixel(100, 150, 200);
        assert pixel1.hashCode() == pixel2.hashCode();
    }

    void testPixelHue() {
        // Test pure colors
        assert redPixel.getHue() == 0; // Red should be 0
        assert greenPixel.getHue() == 120; // Green should be 120
        assert bluePixel.getHue() == 240; // Blue should be 240
        
        // Test grayscale (should be 0)
        assert whitePixel.getHue() == 0;
        assert blackPixel.getHue() == 0;
        assert new Pixel(128, 128, 128).getHue() == 0;
        
        // Test some mixed colors
        Pixel yellow = new Pixel(255, 255, 0);
        assert yellow.getHue() == 60;
        
        Pixel cyan = new Pixel(0, 255, 255);
        assert cyan.getHue() == 180;
        
        Pixel magenta = new Pixel(255, 0, 255);
        assert magenta.getHue() == 300;
    }

    // ===================== DISTANCE METRIC TESTS =====================

    void testSquaredEuclideanMetric() {
        SquaredEuclideanMetric metric = new SquaredEuclideanMetric();
        
        // Test same pixel (distance should be 0)
        assert Math.abs(metric.colorDistance(redPixel, redPixel) - 0.0) < 0.001;
        
        // Test different pixels
        // Red (255,0,0) to Green (0,255,0) = 255^2 + 255^2 + 0^2 = 130050
        assert Math.abs(metric.colorDistance(redPixel, greenPixel) - 130050.0) < 0.001;
        
        // Test black to white
        // (0,0,0) to (255,255,255) = 255^2 + 255^2 + 255^2 = 195075
        assert Math.abs(metric.colorDistance(blackPixel, whitePixel) - 195075.0) < 0.001;
        
        // Test symmetric property
        assert Math.abs(metric.colorDistance(redPixel, bluePixel) - 
                       metric.colorDistance(bluePixel, redPixel)) < 0.001;
    }

    void testCircularHueMetric() {
        CircularHueMetric metric = new CircularHueMetric();
        
        // Test same pixel (distance should be 0)
        assert Math.abs(metric.colorDistance(redPixel, redPixel) - 0.0) < 0.001;
        
        // Test red to green (0 to 120 degrees)
        assert Math.abs(metric.colorDistance(redPixel, greenPixel) - 120.0) < 0.001;
        
        // Test circular property: red (0) to magenta (300) should be 60, not 300
        Pixel magenta = new Pixel(255, 0, 255);
        assert Math.abs(metric.colorDistance(redPixel, magenta) - 60.0) < 0.001;
        
        // Test grayscale pixels (hue = 0)
        assert Math.abs(metric.colorDistance(whitePixel, blackPixel) - 0.0) < 0.001;
        
        // Test symmetric property
        assert Math.abs(metric.colorDistance(redPixel, bluePixel) - 
                       metric.colorDistance(bluePixel, redPixel)) < 0.001;
    }

    // ===================== BUCKETING MAP GENERATOR TESTS =====================

    void testBucketingMapGeneratorColorPalette() {
        BucketingMapGenerator generator = new BucketingMapGenerator();
        
        // Test with 2 colors
        Pixel[] palette = generator.generateColorPalette(testImage, 2);
        assert palette.length == 2;
        
        // Test with 8 colors
        palette = generator.generateColorPalette(testImage, 8);
        assert palette.length == 8;
        
        // Verify palette colors are evenly distributed
        // For 2 colors: centers should be at 1/4 and 3/4 of color space
        palette = generator.generateColorPalette(testImage, 2);
        
        // First bucket center should be less than second
        assert palette[0].getRed() < palette[1].getRed();
        assert palette[0].getGreen() < palette[1].getGreen();
        assert palette[0].getBlue() < palette[1].getBlue();
    }

    void testBucketingMapGeneratorColorMap() {
        BucketingMapGenerator generator = new BucketingMapGenerator();
        Pixel[] palette = generator.generateColorPalette(testImage, 4);
        
        Map<Pixel, Pixel> colorMap = generator.generateColorMap(testImage, palette);
        
        // All unique pixels in the image should be mapped
        Set<Pixel> uniquePixels = new HashSet<>();
        for (Pixel[] row : testImage) {
            for (Pixel pixel : row) {
                uniquePixels.add(pixel);
            }
        }
        
        assert colorMap.size() == uniquePixels.size();
        
        // Each pixel should be mapped to something in the palette
        for (Pixel originalPixel : uniquePixels) {
            assert colorMap.containsKey(originalPixel);
            assert Arrays.asList(palette).contains(colorMap.get(originalPixel));
        }
    }

    // ===================== CLUSTERING MAP GENERATOR TESTS =====================

    void testClusteringMapGeneratorColorPalette() {
        ClusteringMapGenerator generator = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        
        // Test with 2 colors
        Pixel[] palette = generator.generateColorPalette(testImage, 2);
        assert palette.length == 2;
        
        // First centroid should be the first pixel
        assert palette[0].equals(redPixel);
        
        // Second centroid should be different from first
        assert palette[1] != null;
        assert !palette[0].equals(palette[1]);
        
        // Test with more colors than unique pixels
        Pixel[][] simpleImage = {{redPixel, greenPixel}};
        palette = generator.generateColorPalette(simpleImage, 5);
        assert palette.length == 5;
        // Should not crash even with more colors requested than unique pixels
    }

    void testClusteringMapGeneratorColorMap() {
        ClusteringMapGenerator generator = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        Pixel[] initialPalette = generator.generateColorPalette(testImage, 3);
        
        Map<Pixel, Pixel> colorMap = generator.generateColorMap(testImage, initialPalette);
        
        // All unique pixels should be mapped
        Set<Pixel> uniquePixels = new HashSet<>();
        for (Pixel[] row : testImage) {
            for (Pixel pixel : row) {
                uniquePixels.add(pixel);
            }
        }
        
        assert colorMap.size() == uniquePixels.size();
        
        // Each pixel should be mapped to one of the centroids
        for (Pixel originalPixel : uniquePixels) {
            assert colorMap.containsKey(originalPixel);
            assert colorMap.get(originalPixel) != null;
        }
    }

    // ===================== COLOR QUANTIZER TESTS =====================

    void testColorQuantizerWithBucketing() {
        BucketingMapGenerator bucketingGen = new BucketingMapGenerator();
        ColorQuantizer quantizer = new ColorQuantizer(testImage, bucketingGen);
        
        Pixel[][] quantizedImage = quantizer.quantizeTo2DArray(4);
        
        // Check dimensions
        assert quantizedImage.length == testImage.length;
        assert quantizedImage[0].length == testImage[0].length;
        
        // Check that all pixels are quantized
        for (int i = 0; i < quantizedImage.length; i++) {
            for (int j = 0; j < quantizedImage[i].length; j++) {
                assert quantizedImage[i][j] != null;
            }
        }
    }

    void testColorQuantizerWithClustering() {
        ClusteringMapGenerator clusteringGen = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        ColorQuantizer quantizer = new ColorQuantizer(testImage, clusteringGen);
        
        Pixel[][] quantizedImage = quantizer.quantizeTo2DArray(3);
        
        // Check dimensions
        assert quantizedImage.length == testImage.length;
        assert quantizedImage[0].length == testImage[0].length;
        
        // Check that all pixels are quantized
        for (int i = 0; i < quantizedImage.length; i++) {
            for (int j = 0; j < quantizedImage[i].length; j++) {
                assert quantizedImage[i][j] != null;
            }
        }
        
        // Count unique colors in quantized image (should be <= 3)
        Set<Pixel> uniqueColors = new HashSet<>();
        for (Pixel[] row : quantizedImage) {
            for (Pixel pixel : row) {
                uniqueColors.add(pixel);
            }
        }
        assert uniqueColors.size() <= 3;
    }

    // ===================== UTILITY METHODS =====================

    private void printImageColors(Pixel[][] image) {
        for (Pixel[] row : image) {
            for (Pixel pixel : row) {
                System.out.print(pixel + " ");
            }
            System.out.println();
        }
    }
    
    private int countUniqueColors(Pixel[][] image) {
        Set<Pixel> uniqueColors = new HashSet<>();
        for (Pixel[] row : image) {
            for (Pixel pixel : row) {
                uniqueColors.add(pixel);
            }
        }
        return uniqueColors.size();
    }

    private void demonstrateImageAlteration() {
        System.out.println("\nDemonstrating image alteration with real image...");
        
        Pixel[][] imageToDemo = realImage;
        String imageType = "real";
        
        if (realImage == testImage) {
            imageType = "synthetic";
        }
        
        System.out.println("Using " + imageType + " image (" + 
                          imageToDemo.length + "x" + imageToDemo[0].length + ")");
        System.out.println("Original unique colors: " + countUniqueColors(imageToDemo));
        
        // Demonstrate bucketing quantization
        System.out.println("\nAfter bucketing quantization to 4 colors:");
        BucketingMapGenerator bucketingGen = new BucketingMapGenerator();
        ColorQuantizer bucketingQuantizer = new ColorQuantizer(imageToDemo, bucketingGen);
        Pixel[][] bucketingResult = bucketingQuantizer.quantizeTo2DArray(4);
        System.out.println("Unique colors: " + countUniqueColors(bucketingResult));
        
        // Demonstrate clustering quantization
        System.out.println("\nAfter clustering quantization to 3 colors:");
        ClusteringMapGenerator clusteringGen = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        ColorQuantizer clusteringQuantizer = new ColorQuantizer(imageToDemo, clusteringGen);
        Pixel[][] clusteringResult = clusteringQuantizer.quantizeTo2DArray(3);
        System.out.println("Unique colors: " + countUniqueColors(clusteringResult));
        
        // Demonstrate extreme quantization
        System.out.println("\nAfter extreme quantization to 1 color:");
        Pixel[][] extremeResult = bucketingQuantizer.quantizeTo2DArray(1);
        System.out.println("Unique colors: " + countUniqueColors(extremeResult));
        
        // Save demonstration images
        if (imageType.equals("real")) {
            createTestImageDirectory();
            bucketingQuantizer.quantizeToBMP("test_images/demo_bucketing_4colors.bmp", 4);
            clusteringQuantizer.quantizeToBMP("test_images/demo_clustering_3colors.bmp", 3);
            bucketingQuantizer.quantizeToBMP("test_images/demo_extreme_1color.bmp", 1);
            System.out.println("\nSaved demonstration images to test_images/");
        }
    }

    private static class TestResult {
        final String testName;
        final boolean passed;
        final String error;
        
        TestResult(String testName, boolean passed, String error) {
            this.testName = testName;
            this.passed = passed;
            this.error = error;
        }
    }
    
    private static class TestMethod {
        final String name;
        final Runnable test;
        
        TestMethod(String name, Runnable test) {
            this.name = name;
            this.test = test;
        }
    }

    private static TestResult runTest(String testName, Runnable test) {
        try {
            test.run();
            System.out.print("✓");
            return new TestResult(testName, true, null);
        } catch (AssertionError e) {
            System.out.print("✗");
            return new TestResult(testName, false, "Assertion failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.print("✗");
            return new TestResult(testName, false, e.getMessage());
        }
    }

    // ===================== MAIN METHOD FOR RUNNING TESTS =====================

    public static void main(String[] args) {
        ColorQuantizationTests tests = new ColorQuantizationTests();
        
        System.out.println("Running Enhanced CS1501 Project 5 Test Suite...\n");
        
        // Setup test data
        tests.setUp();
        
        int passed = 0;
        int total = 0;
        
        // Test method definitions
        TestMethod[] testMethods = {
            new TestMethod("Pixel Constructor and Getters", () -> tests.testPixelConstructorAndGetters()),
            new TestMethod("Pixel toString", () -> tests.testPixelToString()),
            new TestMethod("Pixel equals", () -> tests.testPixelEquals()),
            new TestMethod("Pixel hashCode", () -> tests.testPixelHashCode()),
            new TestMethod("Pixel getHue", () -> tests.testPixelHue()),
            new TestMethod("SquaredEuclideanMetric", () -> tests.testSquaredEuclideanMetric()),
            new TestMethod("CircularHueMetric", () -> tests.testCircularHueMetric()),
            new TestMethod("BucketingMapGenerator Color Palette", () -> tests.testBucketingMapGeneratorColorPalette()),
            new TestMethod("BucketingMapGenerator Color Map", () -> tests.testBucketingMapGeneratorColorMap()),
            new TestMethod("ClusteringMapGenerator Color Palette", () -> tests.testClusteringMapGeneratorColorPalette()),
            new TestMethod("ClusteringMapGenerator Color Map", () -> tests.testClusteringMapGeneratorColorMap()),
            new TestMethod("ColorQuantizer with Bucketing", () -> tests.testColorQuantizerWithBucketing()),
            new TestMethod("ColorQuantizer with Clustering", () -> tests.testColorQuantizerWithClustering()),
            new TestMethod("Real Image Quantization", () -> tests.testRealImageQuantization()),
            new TestMethod("Multiple Color Reductions", () -> tests.testMultipleColorReductions()),
            new TestMethod("Hue-Based Quantization", () -> tests.testHueBasedQuantization()),
            new TestMethod("Image Loading from File", () -> tests.testImageFromFile())
        };
        
        // Run all tests and track results
        TestResult[] results = new TestResult[testMethods.length];
        for (int i = 0; i < testMethods.length; i++) {
            results[i] = runTest(testMethods[i].name, testMethods[i].test);
            total++;
            if (results[i].passed) passed++;
        }
        
        // Print results
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST RESULTS:");
        System.out.println("=".repeat(60));
        
        for (TestResult result : results) {
            System.out.printf("%-40s %s\n", result.testName, result.passed ? "PASSED" : "FAILED");
            if (!result.passed && result.error != null) {
                System.out.println("  Error: " + result.error);
            }
        }
        
        System.out.println("=".repeat(60));
        System.out.printf("SUMMARY: %d/%d tests passed (%.1f%%)\n", 
                         passed, total, (passed * 100.0) / total);
        
        if (passed == total) {
            System.out.println("🎉 All tests passed!");
        } else {
            System.out.println("❌ Some tests failed. Check the implementation.");
        }
        
        // Demonstrate image alteration
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMONSTRATING IMAGE ALTERATION:");
        System.out.println("=".repeat(60));
        
        tests.demonstrateImageAlteration();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("INSTRUCTIONS FOR ADDING YOUR OWN IMAGES:");
        System.out.println("=".repeat(60));
        System.out.println("1. Head to the 'test_images' directory in your project root");
        System.out.println("2. Add your BMP images to this directory");
        System.out.println("3. Name one of them 'sample.bmp' for automatic testing");
        System.out.println("4. Run this test suite to see quantization results");
    }
}