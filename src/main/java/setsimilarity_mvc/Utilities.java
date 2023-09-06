package setsimilarity_mvc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public abstract class Utilities {
    public static Double calculateJaccardSimilarity(CharSequence left, CharSequence right) {
        Set<String> intersectionSet = new HashSet<String>();
        Set<String> unionSet = new HashSet<String>();
        boolean unionFilled = false;
        int leftLength = left.length();
        int rightLength = right.length();
        if (leftLength == 0 || rightLength == 0) {
            return 0d;
        }

        for (int leftIndex = 0; leftIndex < leftLength; leftIndex++) {
            unionSet.add(String.valueOf(left.charAt(leftIndex)));
            for (int rightIndex = 0; rightIndex < rightLength; rightIndex++) {
                if (!unionFilled) {
                    unionSet.add(String.valueOf(right.charAt(rightIndex)));
                }
                if (left.charAt(leftIndex) == right.charAt(rightIndex)) {
                    intersectionSet.add(String.valueOf(left.charAt(leftIndex)));
                }
            }
            unionFilled = true;
        }
        return (Double.valueOf(intersectionSet.size()) / Double.valueOf(unionSet.size()));
    }

    public static String[] tokenizer(String text) {
        ArrayList<String> tokens = new ArrayList<>();
        String lowerCaseText = text.toLowerCase();
        int startIx = 0;

        while (startIx < lowerCaseText.length()) {
            while (startIx < lowerCaseText.length() && isSeparator(lowerCaseText.charAt(startIx))) {
                startIx++;
            }
            int tokenStart = startIx;

            while (startIx < lowerCaseText.length() && !isSeparator(lowerCaseText.charAt(startIx))) {
                startIx++;
            }
            int tokenEnd = startIx;

            String token = lowerCaseText.substring(tokenStart, tokenEnd);

            if (!token.isEmpty())
                tokens.add(token);

        }
        String[] arr = new String[tokens.size()];
        arr = tokens.toArray(arr);
        return arr;
    }

    private static boolean isSeparator(char c) {
        return !(Character.isLetterOrDigit(c) || Character.getType(c) == Character.OTHER_LETTER
                || Character.getType(c) == Character.OTHER_NUMBER);
    }
}
