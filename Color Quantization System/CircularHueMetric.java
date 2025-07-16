

public class CircularHueMetric implements DistanceMetric_Inter {

      /**
     * Computes the distance between the RGB values of two pixels. Different
     * implementations may use different formulas for calculating distance.
     *
     * @param p1 the first pixel
     * @param p2 the second pixel
     * @return The distance between the RGB values of p1 and p2
     */
    public double colorDistance(Pixel p1, Pixel p2){
        //circular hue distance
        double p1Hue=p1.getHue();
        double p2Hue=p2.getHue();

        //calculate absolute difference between hues
        double distance=Math.abs(p1Hue-p2Hue);
        //Determines the shorter distance by comparing the direct distance with going the other way around the circle 
        return Math.min(distance, 360-distance);

    }


}
