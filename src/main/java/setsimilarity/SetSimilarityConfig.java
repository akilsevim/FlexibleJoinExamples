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


import org.apache.asterix.external.cartilage.base.Configuration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SetSimilarityConfig implements Configuration {
    private HashMap<String, Integer> S = new HashMap<>();

    SetSimilarityConfig(String[] OrderedTokens) {
        for (int i = 0; i < OrderedTokens.length; i++) {
            S.put(OrderedTokens[i], i);
        }
        //System.out.println("Number of Tokens First Config:" + S.size());
    }

    public HashMap<String, Integer> getS() {
        return this.S;
    }
}
