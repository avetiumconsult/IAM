package com.aventium.identity_service.util;

public final class PasswordPolicy {
    private PasswordPolicy() {}

    public static boolean isStrong(String p) {
        if (p == null || p.length() < 12) return false;
        boolean u=false,l=false,d=false,s=false;
        for (char c : p.toCharArray()) {
            if (Character.isUpperCase(c)) u=true;
            else if (Character.isLowerCase(c)) l=true;
            else if (Character.isDigit(c)) d=true;
            else s=true;
        }
        return u && l && d && s;
    }
}
