package com.example.workreportplus.ENUM;

public enum ReportStatus {
    ACTIVE(1),
    DELETED(0);

    private final int value;

    ReportStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ReportStatus fromValue(int value) {
        for (ReportStatus status : ReportStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ReportStatus value: " + value);
    }

    public static ReportStatus fromString(String name) {
        try {
            return ReportStatus.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ReportStatus name: " + name);
        }
    }

    public static ReportStatus fromBoolean(Boolean status) {
        if (status == null) {
            throw new IllegalArgumentException("Boolean status cannot be null");
        }
        return status ? ACTIVE : DELETED;  // ✅ Proper boolean mapping
    }
}
