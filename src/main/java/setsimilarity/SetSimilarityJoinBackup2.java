///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package setsimilarity;
//
//import org.apache.asterix.external.cartilage.base.FlexibleJoin;
//import org.apache.asterix.external.cartilage.base.Summary;
//
//import java.lang.reflect.Array;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Locale;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class SetSimilarityJoinBackup2 implements FlexibleJoin<String, SetSimilarityConfig> {
//    Double SimilarityThreshold = 0.0;
//
//    public SetSimilarityJoinBackup2(Double SimilarityThreshold) {
//        this.SimilarityThreshold = SimilarityThreshold;
//    }
//
//    @Override
//    public Summary<String> createSummarizer1() {
//        return (Summary<String>) new WordCount();
//    }
//
//    @Override
//    public SetSimilarityConfig divide(Summary<String> s1, Summary<String> s2) {
//        WordCount s1wc = (WordCount) s1;
//        WordCount s2wc = (WordCount) s2;
//        for (String token : s1wc.WordCountMap.keySet()) {
//            s2wc.WordCountMap.merge(token, s1wc.WordCountMap.get(token), Integer::sum);
//        }
//
//        LinkedHashMap<String, Integer> SortedWordCountMap =
//                s2wc.WordCountMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(
//                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
//
//        return new SetSimilarityConfig(SortedWordCountMap.keySet().toArray(String[]::new));
//
//    }
//
//    @Override
//    public int[] assign1(String k1, SetSimilarityConfig setSimilarityConfig) {
//
//
//        ArrayList<Integer> ranks = new ArrayList<>();
//        ArrayList<String> tokens = tokenize(k1);
//        for(String token:tokens) {
//            ranks.add(setSimilarityConfig.S.get(token));
//        }
//        int PrefixLength = tokens.size() == 0?0:(int) (tokens.size() - Math.ceil(SimilarityThreshold * tokens.size()) + 1);
//
//        int[] ranksToReturn = new int[PrefixLength];
//
//        Collections.sort(ranks);
//        for (int i = 0; i < PrefixLength; i++) {
//            ranksToReturn[i] = ranks.get(i);
//        }
//        return ranksToReturn;
//    }
//
//    @Override
//    public boolean verify(String k1, String k2) {
//        ArrayList<String> tokens1 = tokenize(k1);
//        ArrayList<String> tokens2 = tokenize(k2);
//
//        int leftLength = tokens1.size();
//        int rightLength = tokens2.size();
//
//        // apply length filter
//        int lengthLowerBound = (int) Math.ceil(SimilarityThreshold * leftLength);
//
//        boolean passesLengthFilter =
//                (lengthLowerBound <= rightLength) && (rightLength <= 1.0f / SimilarityThreshold * leftLength);
//        if (!passesLengthFilter) {
//            return false;
//        }
//
//        return calculateJaccardSimilarityHashMapTokens(tokens1, tokens2) >= SimilarityThreshold;
//
//    }
//
//    public static double calculateJaccardSimilarityHashMap(String left, String right) {
//
//        double intersectionSize = 0;
//
//        int leftLength = left.length();
//        int rightLength = right.length();
//
//        int leftTokenC = 0;
//        int rightTokenC = 0;
//
//        HashMap<String, Integer> map = new HashMap<>();
//
//        String probe;
//        String build;
//
//        if(leftLength<rightLength) {
//            build = left.toLowerCase();
//            probe = right.toLowerCase();
//        } else {
//            build = right.toLowerCase();
//            probe = left.toLowerCase();
//        }
//
//        int startIx = 0;
//        int l = build.length();
//
//        // Skip separators at beginning of string.
//        StringBuilder stringBuilder = new StringBuilder();
//        while (startIx < l) {
//            while (startIx < l && isSeparator(build.charAt(startIx))) {
//                startIx++;
//            }
//            int tokenStart = startIx;
//
//            while (startIx < l && !isSeparator(build.charAt(startIx))) {
//                stringBuilder.append(build.charAt(startIx));
//                startIx++;
//            }
//            int tokenEnd = startIx;
//
//            // Emit token.
//            //String token = build.substring(tokenStart, tokenEnd);
//            String token = stringBuilder.toString();
//            if(!token.isEmpty()) {
//                map.merge(token, 1, Integer::sum);
//                leftTokenC++;
//                stringBuilder = new StringBuilder();
//            }
//        }
//
//        startIx = 0;
//        l = probe.length();
//
//        // Skip separators at beginning of string.
//        stringBuilder = new StringBuilder();
//        while (startIx < l) {
//            while (startIx < l && isSeparator(probe.charAt(startIx))) {
//                startIx++;
//            }
//            int tokenStart = startIx;
//
//            while (startIx < l && !isSeparator(probe.charAt(startIx))) {
//                stringBuilder.append(probe.charAt(startIx));
//                startIx++;
//            }
//            int tokenEnd = startIx;
//
//            // Emit token.
//            //String token = probe.substring(tokenStart, tokenEnd);
//            String token = stringBuilder.toString();
//            if(!token.isEmpty()) {
//                if (map.containsKey(token)) {
//                    map.merge(token, -1, Integer::sum);
//                    if (map.get(token) == 0) map.remove(token);
//                    intersectionSize++;
//                }
//                rightTokenC++;
//                stringBuilder = new StringBuilder();
//            }
//        }
//
//        double sim = (intersectionSize / ((leftTokenC + rightTokenC) - intersectionSize));
//        sim = Math.round(sim * 100000000d) / 100000000d;
//        return sim;
//    }
//
//    public static double calculateJaccardSimilarityHashMapTokens(ArrayList<String> left, ArrayList<String> right) {
//
//        double intersectionSize = 0;
//
//        int leftLength = left.size();
//        int rightLength = right.size();
//
//        int leftTokenC = 0;
//        int rightTokenC = 0;
//
//        HashMap<String, Integer> map = new HashMap<>();
//
//        ArrayList<String> probe;
//        ArrayList<String> build;
//
//        if(leftLength<rightLength) {
//            build = left;
//            probe = right;
//        } else {
//            build = right;
//            probe = left;
//        }
//
//        for (String token:build
//        ) {
//            map.merge(token, 1, Integer::sum);
//            leftTokenC++;
//        }
//
//        for (String token:probe
//        ) {
//            if (map.containsKey(token)) {
//                map.merge(token, -1, Integer::sum);
//                if (map.get(token) == 0) map.remove(token);
//                intersectionSize++;
//            }
//            rightTokenC++;
//        }
//
//        double sim = (intersectionSize / ((leftTokenC + rightTokenC) - intersectionSize));
//        sim = Math.round(sim * 100000000d) / 100000000d;
//        return sim;
//    }
//
//
//    private static boolean isSeparator(char c) {
//        return !(Character.isLetterOrDigit(c) || Character.getType(c) == Character.OTHER_LETTER
//                || Character.getType(c) == Character.OTHER_NUMBER);
//        //return Character.isSpaceChar(c);
//    }
//
//    public ArrayList<String> tokenize(String text) {
//        StringBuilder stringBuilder = new StringBuilder();
//        ArrayList<String> returnList = new ArrayList<>();
//        int startIx = 0;
//        int l = text.length();
//        while (startIx < l) {
//            while (startIx < l && isSeparator(text.charAt(startIx))) {
//                startIx++;
//            }
//
//            while (startIx < l && !isSeparator(text.charAt(startIx))) {
//                stringBuilder.append(text.charAt(startIx));
//                startIx++;
//            }
//
//            String token = stringBuilder.toString().toLowerCase();
//            if(!token.isEmpty()) {
//                returnList.add(token);
//                stringBuilder = new StringBuilder();
//            }
//        }
//        return returnList;
//    }
//
//}
