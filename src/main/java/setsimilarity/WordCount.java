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
        ArrayList<String> tokens = tokenize(k);
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

    public ArrayList<String> tokenize(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> returnList = new ArrayList<>();
        int startIx = 0;
        int l = text.length();
        while (startIx < l) {
            while (startIx < l && isSeparator(text.charAt(startIx))) {
                startIx++;
            }

            while (startIx < l && !isSeparator(text.charAt(startIx))) {
                stringBuilder.append(text.charAt(startIx));
                startIx++;
            }

            String token = stringBuilder.toString().toLowerCase();
            if(!token.isEmpty()) {
                returnList.add(token);
                stringBuilder = new StringBuilder();
            }
        }
        return returnList;
    }

    private static boolean isSeparator(char c) {
        return !(Character.isLetterOrDigit(c) || Character.getType(c) == Character.OTHER_LETTER
                || Character.getType(c) == Character.OTHER_NUMBER);
        //return Character.isSpaceChar(c);
    }
}
