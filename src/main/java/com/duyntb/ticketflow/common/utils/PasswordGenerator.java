package com.duyntb.ticketflow.common.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {
    private static final String UPPER = "ABCDEFGHJKMNPQRSTUVWXYZ"; // remove I, O to avoid confusion
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz"; // remove l, o
    private static final String DIGIT = "23456789"; // remove 0, 1
    private static final String SPECIAL = "@$!%*?&";

    private static final String ALL = UPPER + LOWER + DIGIT + SPECIAL;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() {}


    public static String generate(int length) {
        if (length < 8)
            throw new IllegalArgumentException("Password length must be at least 8");

        List<Character> characters = new ArrayList<>(length);

        characters.add(pick(UPPER));
        characters.add(pick(LOWER));
        characters.add(pick(DIGIT));
        characters.add(pick(SPECIAL));

        for (int i = characters.size(); i < length; i++) {
            characters.add(pick(ALL));
        }

        Collections.shuffle(characters, RANDOM);

        StringBuilder stringBuilder = new StringBuilder(length);
        characters.forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    private static char pick(String source) {
        return source.charAt(RANDOM.nextInt(source.length()));
    }
}
