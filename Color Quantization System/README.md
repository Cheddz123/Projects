**Color Quantization Project (CS1501 Project 5)**

A Java implementation of image color quantization using two different approaches: **Bucketing and K-Means Clustering**. This project reduces the number of colors in bitmap images while preserving visual quality.

🎯 **Overview**

**Color quantization** is the process of reducing the number of distinct colors in an image. This implementation provides two algorithms:

**Bucketing Algorithm**: Divides the 24-bit color space into equal buckets

**K-Means Clustering**: Uses Lloyd's algorithm with intelligent centroid initialization

🚀 **Features**

**Multiple Distance Metrics**:

Squared Euclidean Distance (RGB-based)
Circular Hue Distance (HSV-based)


**Flexible Input/Output**: Supports JPEG, PNG, and BMP formats
**Performance Optimized**: Efficient implementations with configurable parameters
**Comprehensive Testing**: Extensive test suite with performance benchmarking

🛠️ **Installation & Setup**

**Prerequisites**

Java 8 or higher,
Any image file (JPEG, PNG, or BMP) for testing

🔬 **Algorithm Details**

**Bucketing Algorithm**

**Time Complexity**: O(n) where n is the number of pixels

**Space Complexity**: O(k) where k is the number of output colors

**Method**: Divides 24-bit RGB space (16.7M colors) into equal-sized buckets

**Pros**: Fast, deterministic results

**Cons**: May not adapt well to image content

🧪 **Testing**

**Comprehensive Test Suite** (ColorQuantizationTest.java)

**Functionality Tests**: Both algorithms with various parameters

**Performance Benchmarks**: Speed comparison between approaches
Edge Cases: Extreme color counts (1, 2, 512+ colors)
Quality Comparison: Visual output analysis

Simple Test Suite (SimpleColorQuantizationTest.java)

File Detection: Automatically finds available images
Basic Validation: Core functionality verification
Error Handling: Graceful failure reporting


**K-Means Clustering Algorithm**

**Time Complexity**: O(i × n × k) where i is iterations, n is pixels, k is clusters

**Space Complexity**: O(n + k)

**Initialization**: Farthest-first traversal for optimal centroid placement

**Convergence**: Iterative refinement until centroids stabilize

**Pros**: Adaptive to image content, better visual quality

**Cons**: Slower, non-deterministic results

📚 **References**

**Lloyd, S. P. (1982)**. "Least squares quantization in PCM"

**Linde, Y., Buzo, A., & Gray, R. (1980)**. "An Algorithm for Vector Quantizer Design"

**Heckbert, P. (1982)**. "Color image quantization for frame buffer display"

👥 **Authors**

**Course**: CS1501 Algorithm Implementation

**Institution**: University of Pittsburgh

**Instructors**: Dr. Farnan, Dr. Garrison

**Student Implementation**: [Nicholas Cheddar]
