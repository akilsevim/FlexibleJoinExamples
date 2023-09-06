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

import org.apache.asterix.external.cartilage.base.Summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WordCount implements Summary<String> {

    public ConcurrentHashMap<String, Integer> WordCountMap;
    //static private AtomicInteger addCount = new AtomicInteger(0);

    public WordCount() {
        //addCount.set(0);
        WordCountMap = new ConcurrentHashMap<>();
        //System.out.println("New Summary");
    }

    @Override
    public void add(String k) {
        //addCount.incrementAndGet();
        int characterIndex = 0;
        int stringLength = k.length();
        int tokenStart;
        int tokenEnd;
        ArrayList<String> tokens = new ArrayList<>();
        while (characterIndex < stringLength) {
            //skip separators
            while (characterIndex < stringLength && isSeparator(k.charAt(characterIndex))) {
                characterIndex++;
            }
            tokenStart = characterIndex;
            //collect characters
            while (characterIndex < stringLength && !isSeparator(k.charAt(characterIndex))) {
                characterIndex++;
            }
            tokenEnd = characterIndex;
            if(tokenEnd > tokenStart) {
//                tokens.add(k.substring(tokenStart, tokenEnd).toLowerCase());
                String token = k.substring(tokenStart, tokenEnd).toLowerCase();
                WordCountMap.merge(token,1,Integer::sum);
//                String token = k.substring(tokenStart, tokenEnd).toLowerCase();
//                Integer val = WordCountMap.getOrDefault(token, 0);
//                WordCountMap.put(token, val + 1);
            }
        }
//        for (String token: tokens
//             ) {
//            WordCountMap.merge(token,1,Integer::sum);
//        }
    }

    public void add_sb(String k) {
        StringBuilder stringBuilder = new StringBuilder();
        int startIx = 0;
        int l = k.length();
        while (startIx < l) {
            while (startIx < l && isSeparator(k.charAt(startIx))) {
                startIx++;
            }
            while (startIx < l && !isSeparator(k.charAt(startIx))) {
                stringBuilder.append(Character.toLowerCase(k.charAt(startIx)));
                startIx++;
            }
            if(stringBuilder.length() > 0) {
                String token = stringBuilder.toString();
                WordCountMap.merge(token, 1, Integer::sum);
                stringBuilder = new StringBuilder();
            }
        }
    }

    @Override
    public void add(Summary<String> s) {
        WordCount wc = (WordCount) s;
        for (String token : wc.WordCountMap.keySet()) {
            Integer val = WordCountMap.getOrDefault(token, 0);
            WordCountMap.put(token, val + wc.WordCountMap.get(token));
            //WordCountMap.merge(token, wc.WordCountMap.get(token), Integer::sum);
        }
    }

    public ArrayList<String> tokenize(String text) {
        ArrayList<String> tokens = new ArrayList<>();
        //text = text.toLowerCase();
        StringBuilder stringBuilder = new StringBuilder();
        int startIx = 0;
        int l = text.length();
        while (startIx < l) {
            while (startIx < l && isSeparator(text.charAt(startIx))) {
                startIx++;
            }
            while (startIx < l && !isSeparator(text.charAt(startIx))) {
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

    private static boolean isSeparator(char c) {
        return !(Character.isLetterOrDigit(c) || Character.getType(c) == Character.OTHER_LETTER
                || Character.getType(c) == Character.OTHER_NUMBER);
        //return Character.isSpaceChar(c);
    }

//    public AtomicInteger getAddCount() {
//        return addCount;
//    }
}

