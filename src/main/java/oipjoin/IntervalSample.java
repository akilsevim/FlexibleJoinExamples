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


import org.apache.asterix.external.cartilage.base.Summary;

import java.util.ArrayList;

public class IntervalSample {
    public long oStart = Long.MAX_VALUE;
    public long oEnd = Long.MIN_VALUE;

    ArrayList<Long[]> sample1 = new ArrayList<>();
    ArrayList<Long[]> sample2 = new ArrayList<>();

    public void add(long[] k) {
        if (k[0] < this.oStart)
            this.oStart = k[0];
        if (k[1] > this.oEnd)
            this.oEnd = k[1];
    }

    public void add(Summary<long[]> s) {
        IntervalSample iS = (IntervalSample) s;

        if (iS.oStart < this.oStart)
            this.oStart = iS.oStart;
        if (iS.oEnd > this.oEnd)
            this.oEnd = iS.oEnd;
    }
}
