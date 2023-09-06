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
package setsimilarity_mvc;

import org.apache.asterix.external.cartilage.base.FlexibleJoin;
import org.apache.asterix.external.cartilage.base.Summary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SetSimilarityJoin implements FlexibleJoin<String, SetSimilarityConfig> {
    Double SimilarityThreshold = 0.0;
    HashMap<String, Integer> concurrentHashMap;
    ArrayList<Integer> ranks = new ArrayList<>();
    private static long matchCounter = 0;
    public SetSimilarityJoin(Double SimilarityThreshold) {
        this.SimilarityThreshold = SimilarityThreshold;
    }

    @Override
    public Summary<String> createSummarizer1() {
        return (Summary<String>) new WordCount();
    }

    @Override
    public SetSimilarityConfig divide(Summary<String> s1, Summary<String> s2) {
        HashMap<String, Integer> s1wc = ((WordCount) s1).getWordCountMap();
        HashMap<String, Integer> s2wc = ((WordCount) s2).getWordCountMap();
        //System.out.println("Wc1 size:"+s1wc.size());
        //System.out.println("Wc2 size:"+s2wc.size());
        for (String token : s1wc.keySet()) {
            s2wc.merge(token, s1wc.get(token), Integer::sum);
        }

        LinkedHashMap<String, Integer> SortedWordCountMap =
                s2wc.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return new SetSimilarityConfig(SortedWordCountMap.keySet().toArray(String[]::new));

    }

    @Override
    public int[] assign1(String k1, SetSimilarityConfig setSimilarityConfig) {
//        countAssign++;
//        if(countAssign % 10000 == 0) System.out.println("Assign counter:"+countAssign);
        if(concurrentHashMap ==  null) concurrentHashMap = setSimilarityConfig.getS();

        ranks.clear();

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
                //if(concurrentHashMap.containsKey(token)) {
                Integer rank = concurrentHashMap.get(token);
                    if(rank != null) {
                        ranks.add(rank);
                        tokenCount++;
                    }

//                } else {
//                    System.out.println("Token not found:" + token);
//                    System.out.println("Number of Tokens In Assign:" + concurrentHashMap.size());
//
//                }

            }
        }

        if(tokenCount == 0) return new int[0];

        int PrefixLength = (int) (tokenCount - Math.ceil(SimilarityThreshold * tokenCount)) + 1;
        int[] ranksToReturn = new int[PrefixLength];

        Collections.sort(ranks);
        ranksToReturn[0] = ranks.get(0);

        int added = 1;
        for(int i = 1; i < tokenCount && added < PrefixLength; i++) {
            if(ranks.get(i) != ranksToReturn[added - 1]) {
                ranksToReturn[added] = ranks.get(i);
                added++;
            }
        }
        return ranksToReturn;
    }

    @Override
    public boolean match(int b1, int b2) {
        matchCounter++;
        return FlexibleJoin.super.match(b1, b2);
    }

    @Override
    public boolean verify(int b1, String k1, int b2, String k2, SetSimilarityConfig c) {
        if(matchCounter != 0) {
            System.out.println("OIP Join Match Counter:"+matchCounter);
            matchCounter = 0;
        }
        return true;
    }

    @Override
    public boolean verify(String k1, String k2) {
        return true;
    }

    private static boolean isSeparator(char c) {
        return !(Character.isLetterOrDigit(c) || Character.getType(c) == Character.OTHER_LETTER
                || Character.getType(c) == Character.OTHER_NUMBER);
        //return Character.isSpaceChar(c);
    }

}

