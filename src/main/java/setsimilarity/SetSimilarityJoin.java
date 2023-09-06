/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package setsimilarity;

import org.apache.asterix.external.cartilage.base.FlexibleJoin;
import org.apache.asterix.external.cartilage.base.Summary;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SetSimilarityJoin implements FlexibleJoin<String, SetSimilarityConfig> {
    Double SimilarityThreshold = 0.0;
    boolean printedWC = false;
    public SetSimilarityJoin(Double SimilarityThreshold) {
        this.SimilarityThreshold = SimilarityThreshold;
    }

    @Override
    public Summary<String> createSummarizer1() {
        return (Summary<String>) new WordCount();
    }

    @Override
    public SetSimilarityConfig divide(Summary<String> s1, Summary<String> s2) {
        WordCount s1wc = (WordCount) s1;
        WordCount s2wc = (WordCount) s2;

        for (String token : s1wc.WordCountMap.keySet()) {
            s2wc.WordCountMap.merge(token, s1wc.WordCountMap.get(token), Integer::sum);
        }

        LinkedHashMap<String, Integer> SortedWordCountMap =
                s2wc.WordCountMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return new SetSimilarityConfig(SortedWordCountMap.keySet().toArray(String[]::new));

    }

    public int[] assign1_sb(String k1, SetSimilarityConfig setSimilarityConfig) {

        ArrayList<Integer> ranks = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        int startIx = 0;
        int l = k1.length();
        int rLength = 0;
        //StringBuilder stringBuilder1 = new StringBuilder();
        //System.out.println("");
        while (startIx < l) {
            while (startIx < l && isSeparator(k1.charAt(startIx))) {
                startIx++;
            }
            while (startIx < l && !isSeparator(k1.charAt(startIx))) {
                stringBuilder.append(Character.toLowerCase(k1.charAt(startIx)));
                startIx++;
            }
            if(stringBuilder.length() > 0) {
                String token = stringBuilder.toString();
                ranks.add(setSimilarityConfig.S.get(token));
                //stringBuilder1.append(token).append(":").append(setSimilarityConfig.S.get(token)).append(",");
                rLength++;
                stringBuilder = new StringBuilder();
            }
        }

        if(rLength == 0) return new int[0];

        int PrefixLength = (int) (rLength - Math.ceil(SimilarityThreshold * rLength)) + 1;
        //System.out.println("\n"+stringBuilder1 + " Pl: " + PrefixLength);
        int[] ranksToReturn = new int[PrefixLength];

        Collections.sort(ranks);
        ranksToReturn[0] = ranks.get(0);
        //System.out.print("Ranks:");
        int added = 1;
        for(int i = 1; i < PrefixLength; i++) {
            if(ranks.get(i) != ranksToReturn[added - 1]) {
                ranksToReturn[added] = ranks.get(i);
                //System.out.print(ranksToReturn[added] + ",");
                added++;
            }
        }
        return ArrayUtils.subarray(ranksToReturn, 0, added);
    }


    @Override
    public int[] assign1(String k1, SetSimilarityConfig setSimilarityConfig) {

        ArrayList<Integer> ranks = new ArrayList<>();
//        if(!printedWC) {
//            System.out.println("WC1 Size:"+setSimilarityConfig.S.size());
//            System.out.println("Add Counter:"+setSimilarityConfig.addCount);
//            printedWC = true;
//        }
        int characterIndex = 0;
        int stringLength = k1.length();
        int tokenCount = 0;
        int tokenStart;
        int tokenEnd;
        while (characterIndex < stringLength) {
            //skip separators
            while (characterIndex < stringLength && isSeparator(k1.charAt(characterIndex))) {
                characterIndex++;
            }
            tokenStart = characterIndex;
            //collect characters
            while (characterIndex < stringLength && !isSeparator(k1.charAt(characterIndex))) {
                characterIndex++;
            }
            tokenEnd = characterIndex;
            if(tokenEnd > tokenStart) {
                String token = k1.substring(tokenStart, tokenEnd).toLowerCase();
                //if(setSimilarityConfig.S.containsKey(token)) {
                    Integer rank = setSimilarityConfig.S.get(token);
//                    if(rank!=null) {
                        ranks.add(rank);
                        tokenCount++;

//                    } else System.out.println("Couldn't find '"+token+"' in hash map");
                //}

            }
        }

        if(tokenCount == 0) return new int[0];

        int PrefixLength = (int) (tokenCount - Math.ceil(SimilarityThreshold * tokenCount)) + 1;
        int[] ranksToReturn = new int[PrefixLength];

        Collections.sort(ranks);
        ranksToReturn[0] = ranks.get(0);

        int added = 1;
        for(int i = 1; i < PrefixLength; i++) {
            if(ranks.get(i) != ranksToReturn[added - 1]) {
                ranksToReturn[added] = ranks.get(i);
                added++;
            }
        }
        return ArrayUtils.subarray(ranksToReturn, 0, added);
    }

//    @Override
//    public boolean verify(int b1, String k1, int b2, String k2, SetSimilarityConfig setSimilarityConfig) {
//        return true;
//    }

    public boolean verify_sb(String k1, String k2) {

//        int leftLength = k1.length();
//        int rightLength = k2.length();
//
//        // apply length filter
//        int lengthLowerBound = (int) Math.ceil(SimilarityThreshold * leftLength);
//
//        boolean passesLengthFilter =
//                (lengthLowerBound <= rightLength) && (rightLength <= 1.0f / SimilarityThreshold * leftLength);
//        if (!passesLengthFilter) {
//            return false;
//        }
        int leftLength = k1.length();
        int rightLength = k2.length();

        double intersectionSize = 0;
        /*String temp;
        int tempL;
        if(leftLength < rightLength) {
            temp = k1;
            tempL = leftLength;
            leftLength = rightLength;
            rightLength = tempL;
            k1 = k2;
            k2 = temp;
        }*/

        HashMap<String, Integer> map = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        int startIx = 0;
        int l = leftLength;
        int lC = 0;
        while (startIx < l) {
            int tokenStart = startIx;

            while (startIx < l && isSeparator(k1.charAt(startIx))) {
                startIx++;
            }
            while (startIx < l && !isSeparator(k1.charAt(startIx))) {
                stringBuilder.append(Character.toLowerCase(k1.charAt(startIx)));
                startIx++;
            }

            // Emit token.


            if(stringBuilder.length() > 0) {
                //String token = k1.substring(tokenStart, tokenEnd).toLowerCase();

                map.merge(stringBuilder.toString(), 1, Integer::sum);
                lC++;
                stringBuilder = new StringBuilder();
            }
        }

        //stringBuilder = new StringBuilder();
        startIx = 0;
        //int minUnionSize = leftLength;
        l = rightLength;
        int rC = 0;
        while (startIx < l) {
            while (startIx < l && isSeparator(k2.charAt(startIx))) {
                startIx++;
            }
            while (startIx < l && !isSeparator(k2.charAt(startIx))) {
                stringBuilder.append(Character.toLowerCase(k2.charAt(startIx)));
                startIx++;
            }

            // Emit token.


            if(stringBuilder.length() > 0) {
                //String token = k2.substring(tokenStart, tokenEnd).toLowerCase();
                String token = stringBuilder.toString();
                if (map.containsKey(token)) {
                    if (map.get(token) == 1) map.remove(token);
                    else {
                        map.merge(token, -1, Integer::sum);
                    }
                    intersectionSize++;
                }
//                else {
//                    // Could not find element in other set. Increase min union size by 1.
//                    minUnionSize++;
//                    // Check whether jaccThresh can still be satisfied if there was a mismatch.
//                    int maxIntersectionSize = Math.min(leftLength, intersectionSize + (probeListSize - rightLength));
//                    int lowerBound = (int) Math.floor(SimilarityThreshold * minUnionSize);
//                    if (maxIntersectionSize < lowerBound) {
//                        // Cannot satisfy jaccThresh.
//                        return false;
//                    }
//                }
                rC++;
                stringBuilder = new StringBuilder();
            }
        }

        double sim = (intersectionSize / ((lC + rC) - intersectionSize));
        sim = Math.round(sim * 100000000d) / 100000000d;

        return sim >= SimilarityThreshold;
        //return true;

    }

    public boolean verify_s(String k1, String k2) {

        int leftLength = k1.length();
        int rightLength = k2.length();

        // apply length filter
        int lengthLowerBound = (int) Math.ceil(SimilarityThreshold * leftLength);

        boolean passesLengthFilter =
                (lengthLowerBound <= rightLength) && (rightLength <= 1.0f / SimilarityThreshold * leftLength);
        if (!passesLengthFilter) {
            return false;
        }

        double intersectionSize = 0;

        HashMap<String, Integer> map = new HashMap<>();

        int characterIndex = 0;
        int stringLength = leftLength;
        int tokenCount = 0;
        int tokenStart;
        int tokenEnd;
        while (characterIndex < stringLength) {
            //skip separators
            while (characterIndex < stringLength && isSeparator(k1.charAt(characterIndex))) {
                characterIndex++;
            }
            tokenStart = characterIndex;
            //collect characters
            while (characterIndex < stringLength && !isSeparator(k1.charAt(characterIndex))) {
                characterIndex++;
            }
            tokenEnd = characterIndex;
            if(tokenEnd > tokenStart) {
                String token = k1.substring(tokenStart, tokenEnd).toLowerCase();
                map.merge(token, 1, Integer::sum);
                tokenCount++;
            }
        }

        characterIndex = 0;
        stringLength = rightLength;

        while (characterIndex < stringLength) {
            //skip separators
            while (characterIndex < stringLength && isSeparator(k2.charAt(characterIndex))) {
                characterIndex++;
            }
            tokenStart = characterIndex;
            //collect characters
            while (characterIndex < stringLength && !isSeparator(k2.charAt(characterIndex))) {
                characterIndex++;
            }
            tokenEnd = characterIndex;
            if(tokenEnd > tokenStart) {
                String token = k2.substring(tokenStart, tokenEnd).toLowerCase();
                if (map.containsKey(token)) {
                    if (map.get(token) == 1) map.remove(token);
                    else {
                        map.merge(token, -1, Integer::sum);
                    }
                    intersectionSize++;
                }
                tokenCount++;
            }
        }

        double sim = (intersectionSize / (tokenCount - intersectionSize));
        sim = Math.round(sim * 100000000d) / 100000000d;

        return sim >= SimilarityThreshold;

    }

    public boolean verify_js(String k1, String k2) {

        ArrayList<String> left = tokenize(k1);
        ArrayList<String> right = tokenize(k2);

        int leftLength = left.size();
        int rightLength = right.size();

        // apply length filter
        int lengthLowerBound = (int) Math.ceil(SimilarityThreshold * leftLength);

        boolean passesLengthFilter =
                (lengthLowerBound <= rightLength) && (rightLength <= 1.0f / SimilarityThreshold * leftLength);
        if (!passesLengthFilter) {
            return false;
        }

        double intersectionSize = 0;

        HashMap<String, Integer> map = new HashMap<>();

        for(String token: left) {
            map.merge(token, 1, Integer::sum);
        }

        for(String token: right) {
            if (map.containsKey(token)) {
                if (map.get(token) == 1) map.remove(token);
                else {
                    map.merge(token, -1, Integer::sum);
                }
                intersectionSize++;
            }
        }

        double sim = (intersectionSize / ((leftLength + rightLength) - intersectionSize));
        sim = Math.round(sim * 100000000d) / 100000000d;

        return sim >= SimilarityThreshold;

    }

    public double calculateJaccardSimilarityHashMap(ArrayList<String> build, ArrayList<String> probe) {

        double intersectionSize = 0;

        int leftTokenC = build.size();
        int rightTokenC = probe.size();

        HashMap<String, Integer> map = new HashMap<>();
        ArrayList<String> temp;

        if(rightTokenC < leftTokenC) {
            temp = build;
            build = probe;
            probe = temp;
        }

        for(String token:build) {
            map.merge(token, 1, Integer::sum);
        }

        for(String token:probe) {
            if (map.containsKey(token)) {
                if (map.get(token) == 1) map.remove(token);
                else {
                    map.merge(token, -1, Integer::sum);
                }
                intersectionSize++;
            }
        }

        double sim = (intersectionSize / ((leftTokenC + rightTokenC) - intersectionSize));
        sim = Math.round(sim * 100000000d) / 100000000d;
        return sim;
    }

    private static boolean isSeparator(char c) {
        return !(Character.isLetterOrDigit(c) || Character.getType(c) == Character.OTHER_LETTER
                || Character.getType(c) == Character.OTHER_NUMBER);
        //return Character.isSpaceChar(c);
    }

    public ArrayList<String> tokenize(String text) {
        ArrayList<String> tokens = new ArrayList<>();
        int characterIndex = 0;
        int stringLength = text.length();
        int tokenStart;
        int tokenEnd;
        while (characterIndex < stringLength) {
            //skip separators
            while (characterIndex < stringLength && isSeparator(text.charAt(characterIndex))) {
                characterIndex++;
            }
            tokenStart = characterIndex;
            //collect characters
            while (characterIndex < stringLength && !isSeparator(text.charAt(characterIndex))) {
                characterIndex++;
            }
            tokenEnd = characterIndex;
            if(tokenEnd > tokenStart) {
                String token = text.substring(tokenStart, tokenEnd).toLowerCase();
                tokens.add(token);
            }
        }
        return tokens;
    }

    @Override
    public boolean verify(int b1, String k1, int b2, String k2, SetSimilarityConfig c) {
        ArrayList<String> left = tokenize(k1);
        ArrayList<String> right = tokenize(k2);

        int leftLength = left.size();
        int rightLength = right.size();

        // apply length filter
        int lengthLowerBound = (int) Math.ceil(SimilarityThreshold * leftLength);

        boolean passesLengthFilter =
                (lengthLowerBound <= rightLength) && (rightLength <= 1.0f / SimilarityThreshold * leftLength);
        if (!passesLengthFilter) {
            return false;
        }

        double intersectionSize = 0;

        HashMap<String, Integer> map = new HashMap<>();

        ArrayList<Integer> ranksLeft = new ArrayList<>();
        ArrayList<Integer> ranksRight = new ArrayList<>();

        for(String token: left) {
            map.merge(token, 1, Integer::sum);
            ranksLeft.add(c.S.get(token));
        }

        for(String token: right) {
            ranksRight.add(c.S.get(token));
            if (map.containsKey(token)) {
                if (map.get(token) == 1) map.remove(token);
                else {
                    map.merge(token, -1, Integer::sum);
                }
                intersectionSize++;
            }
        }

        double sim = (intersectionSize / ((leftLength + rightLength) - intersectionSize));
        sim = Math.round(sim * 100000000d) / 100000000d;

        if (sim >= SimilarityThreshold) {

            Collections.sort(ranksLeft);
            Collections.sort(ranksRight);

            int i = 0;
            int j = 0;

            while(i < ranksLeft.size() && j < ranksRight.size()) {
                if (ranksLeft.get(i).equals(ranksRight.get(j))) {
                    return ranksLeft.get(i) == b1 && ranksRight.get(j) == b2;
                }

                if (ranksLeft.get(i) > ranksRight.get(j)) {
                    ++j;
                } else {
                    ++i;
                }
            }
        }

        return false;
    }

    @Override
    public boolean verify(String s, String t1) {
        return false;
    }

}

