package com.knu.ddip.location.application.util;

public abstract class LocationKeyFactory {
    public static String createUserIdKey(String encodedUserId) {
        return "user:" + encodedUserId;
    }

    public static String createCellIdUsersKey(String cellId) {
        return "cell:" + cellId + ":users";
    }

    public static String createCellIdExpiriesKey(String cellId) {
        return "cell:" + cellId + ":expiry";
    }
}
