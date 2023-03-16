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

    @Override
    public int[] assign1(String k1, SetSimilarityConfig setSimilarityConfig) {

        ArrayList<Integer> ranks = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        int startIx = 0;
        int l = k1.length();
        int rLength = 0;
//        StringBuilder stringBuilder1 = new StringBuilder();
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
//                stringBuilder1.append(token).append(":").append(setSimilarityConfig.S.get(token)).append(",");
                rLength++;
                stringBuilder = new StringBuilder();
            }
        }

        if(rLength == 0) return new int[0];

        int PrefixLength = rLength==1?rLength:(int) (rLength - Math.ceil(SimilarityThreshold * rLength));
//        System.out.println(stringBuilder1 + " Pl: " + PrefixLength);
        int[] ranksToReturn = new int[PrefixLength];

        Collections.sort(ranks);
        ranksToReturn[0] = ranks.get(0);
        for(int i = 1,added = 1; i < ranks.size() & added < PrefixLength; i++) {
            if(ranks.get(i) != ranksToReturn[added - 1]) {
                ranksToReturn[added] = ranks.get(i);
                added++;
            }
        }
        return ranksToReturn;
    }

//    @Override
//    public boolean verify(int b1, String k1, int b2, String k2, SetSimilarityConfig setSimilarityConfig) {
//        return true;
//    }

    @Override
    public boolean verify(String k1, String k2) {

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
        String temp;
        int tempL;
        if(leftLength < rightLength) {
            temp = k1;
            tempL = leftLength;
            leftLength = rightLength;
            rightLength = tempL;
            k1 = k2;
            k2 = temp;
        }

        HashMap<String, Integer> map = new HashMap<>();
        //StringBuilder stringBuilder = new StringBuilder();
        int startIx = 0;
        int l = leftLength;
        while (startIx < l) {
            int tokenStart = startIx;

            while (startIx < l && !isSeparator(k1.charAt(startIx))) {
                startIx++;
            }
            int tokenEnd = startIx;

            // Emit token.


            if(tokenStart < tokenEnd) {
                String token = k1.substring(tokenStart, tokenEnd).toLowerCase();

                map.merge(token, 1, Integer::sum);
                leftLength++;
                //stringBuilder = new StringBuilder();
            }
        }

        //stringBuilder = new StringBuilder();
        startIx = 0;
        int minUnionSize = leftLength;
        l = rightLength;
        while (startIx < l) {
            int tokenStart = startIx;

            while (startIx < l && !isSeparator(k2.charAt(startIx))) {
                startIx++;
            }
            int tokenEnd = startIx;

            // Emit token.


            if(tokenStart < tokenEnd) {
                String token = k2.substring(tokenStart, tokenEnd).toLowerCase();
                //String token = stringBuilder.toString();
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
                rightLength++;
                //stringBuilder = new StringBuilder();
            }
        }

        double sim = (intersectionSize / ((leftLength + rightLength) - intersectionSize));
        sim = Math.round(sim * 100000000d) / 100000000d;

        return sim >= SimilarityThreshold;
        //return true;

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
        //text = text.toLowerCase();
        StringBuilder stringBuilder = new StringBuilder();
        int startIx = 0;
        int l = text.length();
        while (startIx < l) {
            while (startIx < l && isSeparator(Character.toLowerCase(text.charAt(startIx)))) {
                startIx++;
            }
            while (startIx < l && !isSeparator(Character.toLowerCase(text.charAt(startIx)))) {
                stringBuilder.append(Character.toLowerCase(text.charAt(startIx)));
                startIx++;
            }
            if(stringBuilder.length() > 0) {
                String token = stringBuilder.toString();
                tokens.add(token);
                stringBuilder = new StringBuilder();
            }
        }
        return tokens;
    }

}

