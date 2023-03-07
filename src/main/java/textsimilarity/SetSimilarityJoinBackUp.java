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
package textsimilarity;

import org.apache.asterix.external.cartilage.base.FlexibleJoin;
import org.apache.asterix.external.cartilage.base.Summary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SetSimilarityJoinBackUp implements FlexibleJoin<String, SetSimilarityConfig> {
    Double SimilarityThreshold = 0.0;

    public SetSimilarityJoinBackUp(Double SimilarityThreshold) {
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
        int startIx = 0;
        int l = k1.length();
        //k1 = k1.toLowerCase();

        ArrayList<Integer> ranks = new ArrayList<>();
        int length = 0;
        StringBuilder stringBuilder = new StringBuilder();
        //System.out.println("Text:" + k1);
        while (startIx < l) {

            while (startIx < l && isSeparator(k1.charAt(startIx))) {
                startIx++;
            }
            int tokenStart = startIx;

            while (startIx < l && !isSeparator(k1.charAt(startIx))) {

                stringBuilder.append(k1.charAt(startIx));
                startIx++;

            }
            int tokenEnd = startIx;

            // Emit token.

            //String token = k1.substring(tokenStart, tokenEnd);
            if(stringBuilder.length() != 0) {
                //System.out.println("Token:" + stringBuilder.toString());
                ranks.add(setSimilarityConfig.S.get(stringBuilder.toString()));
                stringBuilder = new StringBuilder();
                length++;
            }

        }
        if(ranks!= null && ranks.size() > 0) {
            for(int r: ranks) {
                System.out.println("Rank:"+r);
            }
            int PrefixLength = (int) (length - Math.ceil(SimilarityThreshold * length) + 1);
            PrefixLength = Math.min(length, PrefixLength);
            int[] ranksToReturn = new int[PrefixLength];
            Collections.sort(ranks);
            for (int i = 0; i < PrefixLength; i++) {
                ranksToReturn[i] = ranks.get(i);
            }
            return ranksToReturn;
        } else return new int[0];

    }

    @Override
    public boolean verify(int b1, String k1, int b2, String k2, SetSimilarityConfig setSimilarityConfig) {
        return verify(k1, k2);
    }

    @Override
    public boolean verify(String k1, String k2) {
        /*int leftLength = k1.length();
        int rightLength = k2.length();

        // apply length filter
        int lengthLowerBound = (int) Math.ceil(SimilarityThreshold * leftLength);

        boolean passesLengthFilter =
                (lengthLowerBound <= rightLength) && (rightLength <= 1.0f / SimilarityThreshold * leftLength);
        if (!passesLengthFilter) {
            return false;
        }*/

        return calculateJaccardSimilarityHashMap(k1, k2) >= SimilarityThreshold;
        //return true;

    }

    public static double calculateJaccardSimilarityHashMap(String left, String right) {

        double intersectionSize = 0;

        int leftLength = left.length();
        int rightLength = right.length();

        int leftTokenC = 0;
        int rightTokenC = 0;

        HashMap<String, Integer> map = new HashMap<>();

        String probe;
        String build;

        if(leftLength<rightLength) {
            build = left;
            probe = right;
        } else {
            build = right;
            probe = left;
        }

        int startIx = 0;
        int l = build.length();

        // Skip separators at beginning of string.
        StringBuilder stringBuilder = new StringBuilder();

        while (startIx < l) {
            while (startIx < l && isSeparator(build.charAt(startIx))) {
                startIx++;
            }
            int tokenStart = startIx;

            while (startIx < l && !isSeparator(build.charAt(startIx))) {
                stringBuilder.append(build.charAt(startIx));
                startIx++;
            }
            int tokenEnd = startIx;

            // Emit token.
            //String token = build.substring(tokenStart, tokenEnd);
            if(stringBuilder.length() != 0) {
                map.merge(stringBuilder.toString(), 1, Integer::sum);
                stringBuilder = new StringBuilder();
                leftTokenC++;
            }
        }

        startIx = 0;
        l = probe.length();

        // Skip separators at beginning of string.
        stringBuilder = new StringBuilder();
        while (startIx < l) {
            while (startIx < l && isSeparator(probe.charAt(startIx))) {
                startIx++;
            }
            int tokenStart = startIx;

            while (startIx < l && !isSeparator(probe.charAt(startIx))) {
                startIx++;
                stringBuilder.append(probe.charAt(startIx));
            }
            int tokenEnd = startIx;

            // Emit token.
            //String token = probe.substring(tokenStart, tokenEnd);
            if(stringBuilder.length() != 0) {
                String token = stringBuilder.toString();
                if (map.containsKey(token)) {
                    map.merge(token, -1, Integer::sum);
                    if (map.get(token) == 0) map.remove(token);
                    intersectionSize++;
                }
                stringBuilder = new StringBuilder();
                rightTokenC++;
            }
        }

        double sim = (intersectionSize / ((leftTokenC + rightTokenC) - intersectionSize));
        sim = Math.round(sim * 100000000d) / 100000000d;
        return sim;
    }

    private static boolean isSeparator(char c) {
        return Character.isSpaceChar(c);
        //return !(Character.isLetterOrDigit(c) || Character.getType(c) == Character.OTHER_LETTER
        //        || Character.getType(c) == Character.OTHER_NUMBER);
    }

}
