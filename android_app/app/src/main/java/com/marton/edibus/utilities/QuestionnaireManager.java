package com.marton.edibus.utilities;


import com.marton.edibus.App;

public class QuestionnaireManager {

    public static final String QUESTIONNAIRE_KEY = "QUESTIONNAIRE";

    public static boolean readQuestionnaireFilledInFromSharedPreferences(){

        String questionnaireString = SharedPreferencesManager.readString(App.getAppContext(), QUESTIONNAIRE_KEY);
        if (questionnaireString == null){
            SharedPreferencesManager.writeString(App.getAppContext(), QUESTIONNAIRE_KEY, "false");

            return false;
        }else return !questionnaireString.equals("false");
    }

    public static void writeQuestionnaireFilledInToSharedPreferences(boolean filledIn){

        SharedPreferencesManager.writeString(App.getAppContext(), QUESTIONNAIRE_KEY, String.valueOf(filledIn));
    }
}
