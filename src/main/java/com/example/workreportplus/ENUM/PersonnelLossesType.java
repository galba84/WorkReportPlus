package com.example.workreportplus.ENUM;

public enum PersonnelLossesType {
    LOSS_200("200"),
    LOSS_300("300"),
    LOSS_400("400"),
    LOSS_500("500");

    private final String value;

    PersonnelLossesType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
