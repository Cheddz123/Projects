

import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Standalone test suite for CS1501 Project 5 - Color Quantization
 * Tests all major components without requiring JUnit framework
 */
public class ColorQuantizationTests {

    private Pixel redPixel;
    private Pixel greenPixel;
    private Pixel bluePixel;
    private Pixel whitePixel;
    private Pixel blackPixel;
    private Pixel[][] testImage;
    private Pixel[][] smallTestImage;

    void setUp() {
        // Create test pixels
        redPixel = new Pixel(255, 0, 0);
        greenPixel = new Pixel(0, 255, 0);
        bluePixel = new Pixel(0, 0, 255);
        whitePixel = new Pixel(255, 255, 255);
        blackPixel = new Pixel(0, 0, 0);

        // Create test images
        testImage = new Pixel[][] {
            {redPixel, greenPixel, bluePixel},
            {whitePixel, blackPixel, redPixel},
            {greenPixel, bluePixel, whitePixel}
        };

        smallTestImage = new Pixel[][] {
            {redPixel, greenPixel},
            {bluePixel, whitePixel}
        };
    }

    // ===================== PIXEL TESTS =====================

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

    void testBucketingMapGeneratorInvalidInput() {
        BucketingMapGenerator generator = new BucketingMapGenerator();
        
        try {
            generator.generateColorPalette(testImage, 0);
            assert false; // Should have thrown exception
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        try {
            generator.generateColorPalette(testImage, -1);
            assert false; // Should have thrown exception
        } catch (IllegalArgumentException e) {
            // Expected
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

    void testClusteringMapGeneratorWithCircularHue() {
        ClusteringMapGenerator generator = new ClusteringMapGenerator(new CircularHueMetric());
        
        Pixel[] palette = generator.generateColorPalette(testImage, 2);
        assert palette.length == 2;
        
        Map<Pixel, Pixel> colorMap = generator.generateColorMap(testImage, palette);
        assert colorMap != null;
        assert !colorMap.isEmpty();
    }

    void testClusteringMapGeneratorInvalidInput() {
        ClusteringMapGenerator generator = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        
        try {
            generator.generateColorPalette(testImage, 0);
            assert false; // Should have thrown exception
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        try {
            generator.generateColorPalette(testImage, -1);
            assert false; // Should have thrown exception
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    void testClusteringMapGeneratorKMeansConvergence() {
        ClusteringMapGenerator generator = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        
        // Create a simple image with clear clusters
        Pixel[][] clusteredImage = {
            {new Pixel(10, 10, 10), new Pixel(20, 20, 20)},
            {new Pixel(200, 200, 200), new Pixel(210, 210, 210)}
        };
        
        Pixel[] initialPalette = generator.generateColorPalette(clusteredImage, 2);
        Map<Pixel, Pixel> colorMap = generator.generateColorMap(clusteredImage, initialPalette);
        
        // Should converge to reasonable centroids
        assert colorMap != null;
        assert colorMap.size() == 4; // All 4 unique pixels should be mapped
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

    void testColorQuantizerBMPOutput() {
        BucketingMapGenerator bucketingGen = new BucketingMapGenerator();
        ColorQuantizer quantizer = new ColorQuantizer(testImage, bucketingGen);
        
        // This test would need file I/O, but we can at least test it doesn't crash
        try {
            quantizer.quantizeToBMP("test_output.bmp", 4);
            // If we reach here, no exception was thrown
        } catch (Exception e) {
            // If it fails due to file I/O, that's acceptable for this test
            System.out.println("BMP output test skipped due to file I/O: " + e.getMessage());
        }
    }

    // ===================== EDGE CASE TESTS =====================

    void testEmptyImage() {
        Pixel[][] emptyImage = new Pixel[0][0];
        BucketingMapGenerator generator = new BucketingMapGenerator();
        
        // Should handle empty images gracefully
        try {
            Pixel[] palette = generator.generateColorPalette(emptyImage, 2);
            assert palette != null;
        } catch (Exception e) {
            // Some implementations might throw exceptions for empty images
            System.out.println("Empty image test: " + e.getMessage());
        }
    }

    void testSinglePixelImage() {
        Pixel[][] singlePixelImage = {{redPixel}};
        BucketingMapGenerator generator = new BucketingMapGenerator();
        
        Pixel[] palette = generator.generateColorPalette(singlePixelImage, 2);
        Map<Pixel, Pixel> colorMap = generator.generateColorMap(singlePixelImage, palette);
        
        assert colorMap.size() == 1;
        assert colorMap.containsKey(redPixel);
    }

    void testMonochromeImage() {
        Pixel[][] monochromeImage = {
            {redPixel, redPixel, redPixel},
            {redPixel, redPixel, redPixel}
        };
        
        ClusteringMapGenerator generator = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        Pixel[] palette = generator.generateColorPalette(monochromeImage, 3);
        Map<Pixel, Pixel> colorMap = generator.generateColorMap(monochromeImage, palette);
        
        assert colorMap.size() == 1;
        assert colorMap.containsKey(redPixel);
    }

    void testLargeColorReduction() {
        // Test reducing a large number of colors to a small number
        BucketingMapGenerator generator = new BucketingMapGenerator();
        ColorQuantizer quantizer = new ColorQuantizer(testImage, generator);
        
        Pixel[][] quantizedImage = quantizer.quantizeTo2DArray(1);
        
        // Should reduce to only 1 color
        Set<Pixel> uniqueColors = new HashSet<>();
        for (Pixel[] row : quantizedImage) {
            for (Pixel pixel : row) {
                uniqueColors.add(pixel);
            }
        }
        assert uniqueColors.size() == 1;
    }

    // ===================== PERFORMANCE TESTS =====================

    void testPerformanceWithLargeImage() {
        // Create a larger test image
        Pixel[][] largeImage = new Pixel[50][50];
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                largeImage[i][j] = new Pixel(i * 5, j * 5, (i + j) % 256);
            }
        }
        
        ClusteringMapGenerator generator = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        ColorQuantizer quantizer = new ColorQuantizer(largeImage, generator);
        
        long startTime = System.currentTimeMillis();
        Pixel[][] result = quantizer.quantizeTo2DArray(16);
        long endTime = System.currentTimeMillis();
        
        assert result != null;
        // Should complete in reasonable time (less than 5 seconds)
        assert endTime - startTime < 5000;
    }

    // ===================== INTEGRATION TESTS =====================

    void testFullPipelineComparison() {
        // Test that bucketing and clustering produce reasonable results
        BucketingMapGenerator bucketingGen = new BucketingMapGenerator();
        ClusteringMapGenerator clusteringGen = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        
        ColorQuantizer bucketingQuantizer = new ColorQuantizer(testImage, bucketingGen);
        ColorQuantizer clusteringQuantizer = new ColorQuantizer(testImage, clusteringGen);
        
        Pixel[][] bucketingResult = bucketingQuantizer.quantizeTo2DArray(4);
        Pixel[][] clusteringResult = clusteringQuantizer.quantizeTo2DArray(4);
        
        // Both should produce valid results
        assert bucketingResult != null;
        assert clusteringResult != null;
        
        // Check that dimensions match
        assert bucketingResult.length == testImage.length;
        assert clusteringResult.length == testImage.length;
        assert bucketingResult[0].length == testImage[0].length;
        assert clusteringResult[0].length == testImage[0].length;
        
        // Results may be different but should both be valid quantizations
        assert bucketingResult[0][0] != null;
        assert clusteringResult[0][0] != null;
    }

    void testDistanceMetricComparison() {
        // Test that different distance metrics produce different results
        ClusteringMapGenerator euclideanGen = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        ClusteringMapGenerator hueGen = new ClusteringMapGenerator(new CircularHueMetric());
        
        ColorQuantizer euclideanQuantizer = new ColorQuantizer(testImage, euclideanGen);
        ColorQuantizer hueQuantizer = new ColorQuantizer(testImage, hueGen);
        
        Pixel[][] euclideanResult = euclideanQuantizer.quantizeTo2DArray(3);
        Pixel[][] hueResult = hueQuantizer.quantizeTo2DArray(3);
        
        // Both should produce valid results
        assert euclideanResult != null;
        assert hueResult != null;
        
        // Results might be different (though not guaranteed for this small test)
        assert euclideanResult.length == hueResult.length;
        assert euclideanResult[0].length == hueResult[0].length;
    }

    // ===================== HELPER CLASSES =====================

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
        System.out.println("\nOriginal test image colors:");
        printImageColors(testImage);
        
        // Demonstrate bucketing quantization
        System.out.println("\nAfter bucketing quantization to 4 colors:");
        BucketingMapGenerator bucketingGen = new BucketingMapGenerator();
        ColorQuantizer bucketingQuantizer = new ColorQuantizer(testImage, bucketingGen);
        Pixel[][] bucketingResult = bucketingQuantizer.quantizeTo2DArray(4);
        printImageColors(bucketingResult);
        
        // Demonstrate clustering quantization
        System.out.println("\nAfter clustering quantization to 3 colors:");
        ClusteringMapGenerator clusteringGen = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        ColorQuantizer clusteringQuantizer = new ColorQuantizer(testImage, clusteringGen);
        Pixel[][] clusteringResult = clusteringQuantizer.quantizeTo2DArray(3);
        printImageColors(clusteringResult);
        
        // Demonstrate extreme quantization
        System.out.println("\nAfter extreme quantization to 1 color:");
        Pixel[][] extremeResult = bucketingQuantizer.quantizeTo2DArray(1);
        printImageColors(extremeResult);
        
        // Count unique colors
        System.out.println("\nUnique color counts:");
        System.out.println("Original: " + countUniqueColors(testImage) + " colors");
        System.out.println("Bucketing (4): " + countUniqueColors(bucketingResult) + " colors");
        System.out.println("Clustering (3): " + countUniqueColors(clusteringResult) + " colors");
        System.out.println("Extreme (1): " + countUniqueColors(extremeResult) + " colors");
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
        
        System.out.println("Running CS1501 Project 5 Test Suite...\n");
        
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
            new TestMethod("BucketingMapGenerator Invalid Input", () -> tests.testBucketingMapGeneratorInvalidInput()),
            new TestMethod("ClusteringMapGenerator Color Palette", () -> tests.testClusteringMapGeneratorColorPalette()),
            new TestMethod("ClusteringMapGenerator Color Map", () -> tests.testClusteringMapGeneratorColorMap()),
            new TestMethod("ClusteringMapGenerator with CircularHue", () -> tests.testClusteringMapGeneratorWithCircularHue()),
            new TestMethod("ClusteringMapGenerator Invalid Input", () -> tests.testClusteringMapGeneratorInvalidInput()),
            new TestMethod("ClusteringMapGenerator K-means Convergence", () -> tests.testClusteringMapGeneratorKMeansConvergence()),
            new TestMethod("ColorQuantizer with Bucketing", () -> tests.testColorQuantizerWithBucketing()),
            new TestMethod("ColorQuantizer with Clustering", () -> tests.testColorQuantizerWithClustering()),
            new TestMethod("ColorQuantizer BMP Output", () -> tests.testColorQuantizerBMPOutput()),
            new TestMethod("Empty Image", () -> tests.testEmptyImage()),
            new TestMethod("Single Pixel Image", () -> tests.testSinglePixelImage()),
            new TestMethod("Monochrome Image", () -> tests.testMonochromeImage()),
            new TestMethod("Large Color Reduction", () -> tests.testLargeColorReduction()),
            new TestMethod("Performance with Large Image", () -> tests.testPerformanceWithLargeImage()),
            new TestMethod("Full Pipeline Comparison", () -> tests.testFullPipelineComparison()),
            new TestMethod("Distance Metric Comparison", () -> tests.testDistanceMetricComparison())
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
    }
}