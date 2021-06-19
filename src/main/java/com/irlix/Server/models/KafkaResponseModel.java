package com.irlix.Server.models;


public class KafkaResponseModel {
    boolean validationState;
    String description;

//    public KafkaResponseModel(boolean validationState, String description) {
//        this.validationState = validationState;
//        this.description = description;
//    }

    public boolean getValidationState() {
        return validationState;
    }

    public String getDescription() {
        return description;
    }

    public void setValidationState(boolean validationState) {
        this.validationState = validationState;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
