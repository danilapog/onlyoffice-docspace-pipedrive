package com.onlyoffice.docspacepipedrive.security.util;

import org.springframework.stereotype.Component;

import java.util.Random;


@Component
public final class RandomPasswordGenerator {
    public static final String UPPER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWER_CHARS = "abcdefghijklmnopqrstuvwxyz";
    public static final String NUMBERS = "1234567890";
    public static final String SPECIAL_CHARS = "!@#$%^&*()_+{}";

    public static String generatePassword(int length) {
        char[] password = new char[length];
        String charSet = "";
        Random random = new Random();

        charSet = charSet.concat(UPPER_CHARS)
                .concat(LOWER_CHARS)
                .concat(NUMBERS)
                .concat(SPECIAL_CHARS);

        for (int i = 0; i < length; i++) {
            password[i] = charSet.toCharArray()[random.nextInt(charSet.length() - 1)];
        }

        return String.valueOf(password);
    }
}
