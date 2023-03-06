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
package jsonsim;


import org.apache.asterix.external.cartilage.base.Configuration;
import org.apache.asterix.external.cartilage.base.FlexibleJoin;
import org.apache.asterix.external.cartilage.base.Summary;
import org.apache.asterix.om.pointables.base.IVisitablePointable;

class JSonConfiguration implements Configuration {
}

class JSonSummary implements Summary<IVisitablePointable> {

    @Override
    public void add(IVisitablePointable k) {
        System.out.println(k.getClass());
    }

    @Override
    public void add(Summary<IVisitablePointable> s) {
    }
}

public class JsonJoin implements FlexibleJoin<IVisitablePointable, JSonConfiguration> {

    @Override
    public Summary<IVisitablePointable> createSummarizer1() {
        return new JSonSummary();
    }

    @Override
    public JSonConfiguration divide(Summary<IVisitablePointable> s1, Summary<IVisitablePointable> s2) {
        return new JSonConfiguration();
    }

    @Override
    public int[] assign1(IVisitablePointable k1, JSonConfiguration jSonConfiguration) {
        return new int[1];
    }

    @Override
    public boolean verify(IVisitablePointable k1, IVisitablePointable k2) {
        return true;
    }
}
