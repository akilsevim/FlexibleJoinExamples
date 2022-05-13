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
package oipjoin;

import org.apache.asterix.external.cartilage.base.FlexibleJoin;
import org.apache.asterix.external.cartilage.base.Summary;
import org.apache.asterix.external.cartilage.base.types.Interval;

public class IntervalJoin implements FlexibleJoin<Interval, IntervalJoinConfig> {
    private double k = 2;

    public IntervalJoin(double k) {
        this.k = k;
    }

    @Override
    public Summary<Interval> createSummarizer1() {
        return new IntervalSummary();
    }

    @Override
    public IntervalJoinConfig divide(Summary<Interval> s1, Summary<Interval> s2) {

        IntervalSummary iS1 = (IntervalSummary) s1;
        IntervalSummary iS2 = (IntervalSummary) s2;

        iS1.add(iS2);

        double d1 = (iS1.oEnd - iS1.oStart) / k;
        double d2 = (iS1.oEnd - iS1.oStart) / k;

        return new IntervalJoinConfig(d1, d2, iS1, iS2, k);
    }

    @Override
    public int[] assign1(Interval k1, IntervalJoinConfig intervalJoinConfig) {

        int i = (int) ((k1.start - intervalJoinConfig.iS1.oStart) / intervalJoinConfig.d1);
        int j = (int) ((k1.end - intervalJoinConfig.iS1.oStart) / intervalJoinConfig.d1);
        int bucketId = 0;
        for(int s = i; s <= j & s < intervalJoinConfig.k; s++) {
            bucketId |= 1 << s;
        }
        /*if (bucketId != 1) {
            System.out.println("bucket ID "+bucketId+":" + k1.start+","+ k1.end);
            System.out.println(i + "," +j);
        }*/
        return new int[] {bucketId};
    }

    @Override
    public boolean match(int b1, int b2) {
        int max = Math.max(b1, b2);
        return max == (b1 | b2);
    }

    @Override
    public boolean verify(int b1, Interval k1, int b2, Interval k2, IntervalJoinConfig c) {
        return verify(k1, k2);
    }

    @Override
    public boolean verify(Interval k1, Interval k2) {
        return k1.start < k2.end && k1.end > k2.start;
    }
}
