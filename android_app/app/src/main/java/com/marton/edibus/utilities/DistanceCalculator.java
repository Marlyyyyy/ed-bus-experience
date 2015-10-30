package com.marton.edibus.utilities;


public class DistanceCalculator {

    // Returns the distance between two points in meters
    public static double getDistanceBetweenPoints(double firstLatitude, double firstLongitude, double secondLatitude, double secondLongitude){

        double theta = firstLongitude - secondLongitude;

        double dist = Math.sin(fromDegreeToRadian(firstLatitude)) * Math.sin(fromDegreeToRadian(secondLatitude)) +
                Math.cos(fromDegreeToRadian(firstLatitude)) * Math.cos(fromDegreeToRadian(secondLatitude)) * Math.cos(fromDegreeToRadian(theta));
        dist = Math.acos(dist);
        dist = fromRadianToDegree(dist);
        dist = dist * 60 * 1.1515;

        return dist * 1609.344;
    }

    // Converts decimal degrees to radians
    private static double fromDegreeToRadian(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // Converts radians to decimal degrees
    private static double fromRadianToDegree(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
