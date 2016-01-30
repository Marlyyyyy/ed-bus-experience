package com.marton.edibus.main.models;


public class Questionnaire {

    public Questionnaire(int age, String gender, boolean concessionCard, String travelReason){

        this.age = age;
        this.gender = gender;
        this.concessionCard = concessionCard;
        this.travelReason = travelReason;
    }

    public Questionnaire(){}

    private int age;

    private String gender;

    private boolean concessionCard;

    private String travelReason;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isConcessionCard() {
        return concessionCard;
    }

    public void setConcessionCard(boolean concessionCard) {
        this.concessionCard = concessionCard;
    }

    public String getTravelReason() {
        return travelReason;
    }

    public void setTravelReason(String travelReason) {
        this.travelReason = travelReason;
    }
}
