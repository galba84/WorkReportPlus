package com.example.workreportplus.ENUM;

public enum ContractorStatus {

    SERVICE("Служба", "Service"),
    VACATION("Відпустка", "Vacation"),
    ASSIGNMENT("Прикомандирований", "Assignment"),
    TRAINING("Навчання", "Training"),
    TRANSFER("Переведення", "Transfer"),
    DISCHARGE("Звільнення", "Discharge");

    private final String ukrainian;
    private final String english;

    ContractorStatus(String ukrainian, String english) {
        this.ukrainian = ukrainian;
        this.english = english;
    }

    public String getUkrainian() {
        return ukrainian;
    }

    public String getEnglish() {
        return english;
    }
}
