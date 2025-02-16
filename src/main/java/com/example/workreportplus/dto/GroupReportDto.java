package com.example.workreportplus.dto;


import jakarta.validation.constraints.NotBlank;

public class GroupReportDto {

    @NotBlank
    private String groupName;

    @NotBlank
    private String groupMembers;

    @NotBlank
    private String place;

    private boolean workedRepaired;

    private String description;

    // Getters and Setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(String groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public boolean isWorkedRepaired() {
        return workedRepaired;
    }

    public void setWorkedRepaired(boolean workedRepaired) {
        this.workedRepaired = workedRepaired;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
