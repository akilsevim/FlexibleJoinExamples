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

public class WordCount implements Summary<String> {

    public Map<String, Integer> WordCountMap = new HashMap<>();

    @Override
    public void add(String k) {
        String[] tokens = tokenizer(k);
        for (String token : tokens) {
            WordCountMap.merge(token, 1, Integer::sum);
        }
    }

    @Override
    public void add(Summary<String> s) {
        WordCount wc = (WordCount) s;
        for (String token : wc.WordCountMap.keySet()) {
            WordCountMap.merge(token, wc.WordCountMap.get(token), Integer::sum);
        }
    }

    public String[] tokenizer(String text) {
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

    private boolean isSeparator(char c) {
        return !(Character.isLetterOrDigit(c) || Character.getType(c) == Character.OTHER_LETTER
                || Character.getType(c) == Character.OTHER_NUMBER);
    }
}
