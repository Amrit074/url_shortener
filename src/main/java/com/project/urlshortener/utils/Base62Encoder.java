package com.project.urlshortener.utils;

public class Base62Encoder {

    private static final char[] BASE62_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };
    private static final int BASE = BASE62_CHARS.length;

    public static String encode(long number) {
        if (number == 0) {
            return String.valueOf(BASE62_CHARS[0]);
        }

        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(BASE62_CHARS[(int) (number % BASE)]);
            number /= BASE;
        }
        return sb.reverse().toString();
    }

    public static long decode(String str) {
        long decoded = 0;
        long power = 1;

        for (int i = str.length() - 1; i >= 0; i--) {
            char c = str.charAt(i);
            int digit = -1;

            for (int j = 0; j < BASE; j++) {
                if (BASE62_CHARS[j] == c) {
                    digit = j;
                    break;
                }
            }

            if (digit == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }

            decoded += digit * power;
            power *= BASE;
        }
        return decoded;
    }
}
