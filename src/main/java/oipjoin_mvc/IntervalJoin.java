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
package oipjoin_mvc;

import org.apache.asterix.external.cartilage.base.FlexibleJoin;
import org.apache.asterix.external.cartilage.base.Summary;

public class IntervalJoin implements FlexibleJoin<long[], IntervalJoinConfig> {
    private long k = 2;
    private static long matchCounter = 0;

    public IntervalJoin(long k) {
        this.k = k;
    }

    @Override
    public Summary<long[]> createSummarizer1() {
        return new IntervalSummary();
    }

    @Override
    public IntervalJoinConfig divide(Summary<long[]> s1, Summary<long[]> s2) {

        IntervalSummary iS1 = (IntervalSummary) s1;
        IntervalSummary iS2 = (IntervalSummary) s2;

        iS1.add(iS2);

        double d1 = (double) (iS1.oEnd - iS1.oStart) / k;
        double d2 = (double) (iS1.oEnd - iS1.oStart) / k;

        return new IntervalJoinConfig(d1, d2, iS1, iS2, k);
    }

    @Override
    public int[] assign1(long[] k1, IntervalJoinConfig intervalJoinConfig) {

        short i = (short) ((k1[0] - intervalJoinConfig.iS1.oStart) / intervalJoinConfig.d1);
        short j = (short) (Math.ceil((k1[1] - intervalJoinConfig.iS1.oStart) / intervalJoinConfig.d1) - 1);

        int bucketId = (i << 16) | (j & 0xFFFF);
        return new int[] {bucketId};
    }

    @Override
    public boolean match(int b1, int b2) {
        short b1Start = (short) (b1 >> 16);
        short b1End = (short) b1;

        short b2Start = (short) (b2 >> 16);
        short b2End = (short) b2;
        matchCounter++;
        return (b1Start <= b2End && b1End >= b2Start);
    }

    @Override
    public boolean verify(int b1, long[] k1, int b2, long[] k2, IntervalJoinConfig c) {
        if(matchCounter != 0) {
            System.out.println("OIP Join Match Counter:"+matchCounter);
            matchCounter = 0;
        }
        return true;
    }

    @Override
    public boolean verify(long[] longs, long[] t1) {
        return true;
    }
}
