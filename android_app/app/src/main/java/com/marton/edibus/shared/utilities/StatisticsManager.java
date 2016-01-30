package com.marton.edibus.shared.utilities;


import com.marton.edibus.App;

public class StatisticsManager {

    public static final String JOURNEYS_KEY = "JOURNEYS";

    public static final String TOTAL_WAITING_TIME_KEY = "TOTAL_WAITING_TIME";

    public static final String TOTAL_TRAVELLING_TIME_KEY = "TOTAL_TRAVELLING_TIME";

    public static final String TOTAL_TRAVELLING_DISTANCE_KEY = "TOTAL_TRAVELLING_DISTANCE";

    public static int readJourneysFromSharedPreferences(){

        String journeysString = SharedPreferencesManager.readString(App.getAppContext(), JOURNEYS_KEY);
        int journeys;
        if (journeysString == null){
            journeys = 0;
            SharedPreferencesManager.writeString(App.getAppContext(), JOURNEYS_KEY, String.valueOf(journeys));
        }else{
            journeys = Integer.valueOf(journeysString);
        }

        return journeys;
    }

    public static int readTotalWaitingTimeFromSharedPreferences(){

        String totalWaitingTimeString = SharedPreferencesManager.readString(App.getAppContext(), TOTAL_WAITING_TIME_KEY);
        int totalWaitingTime;
        if (totalWaitingTimeString == null){
            totalWaitingTime = 0;
            SharedPreferencesManager.writeString(App.getAppContext(), TOTAL_WAITING_TIME_KEY, String.valueOf(totalWaitingTime));
        }else{
            totalWaitingTime = Integer.valueOf(totalWaitingTimeString);
        }

        return totalWaitingTime;
    }

    public static int readTotalTravellingTimeFromSharedPreferences(){

        String totalTravellingTimeString = SharedPreferencesManager.readString(App.getAppContext(), TOTAL_TRAVELLING_TIME_KEY);
        int totalTravellingTime;
        if (totalTravellingTimeString == null){
            totalTravellingTime = 0;
            SharedPreferencesManager.writeString(App.getAppContext(), TOTAL_TRAVELLING_TIME_KEY, String.valueOf(totalTravellingTime));
        }else{
            totalTravellingTime = Integer.valueOf(totalTravellingTimeString);
        }

        return totalTravellingTime;
    }

    public static double readTotalTravellingDistanceFromSharedPreferences(){

        String totalTravellingDistanceString = SharedPreferencesManager.readString(App.getAppContext(), TOTAL_TRAVELLING_DISTANCE_KEY);
        double totalTravellingDistance;
        if (totalTravellingDistanceString == null){
            totalTravellingDistance = 0;
            SharedPreferencesManager.writeString(App.getAppContext(), TOTAL_TRAVELLING_DISTANCE_KEY, String.valueOf(totalTravellingDistance));
        }else{
            totalTravellingDistance = Double.valueOf(totalTravellingDistanceString);
        }

        return totalTravellingDistance;
    }

    public static void clearStatistics(){

        SharedPreferencesManager.writeString(App.getAppContext(), JOURNEYS_KEY, String.valueOf(0));
        SharedPreferencesManager.writeString(App.getAppContext(), TOTAL_WAITING_TIME_KEY, String.valueOf(0));
        SharedPreferencesManager.writeString(App.getAppContext(), TOTAL_TRAVELLING_TIME_KEY, String.valueOf(0));
        SharedPreferencesManager.writeString(App.getAppContext(), TOTAL_TRAVELLING_DISTANCE_KEY, String.valueOf(0));
    }
}
