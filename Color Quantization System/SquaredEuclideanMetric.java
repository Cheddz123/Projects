package cs1501_p5;

public class SquaredEuclideanMetric implements DistanceMetric_Inter {
    /**
     * Computes the distance between the RGB values of two pixels. Different
     * implementations may use different formulas for calculating distance.
     *
     * @param p1 the first pixel
     * @param p2 the second pixel
     * @return The distance between the RGB values of p1 and p2
     */
    public double colorDistance(Pixel p1, Pixel p2){
        //calculate difference between the RGB values of the two pixels
        double redDiff = p1.getRed() - p2.getRed();
        double greenDiff = p1.getGreen() - p2.getGreen();
        double blueDiff=p1.getBlue()-p2.getBlue();
        //euclidean distance
        return (redDiff*redDiff)+(greenDiff*greenDiff)+(blueDiff*blueDiff);

    
    }

}
