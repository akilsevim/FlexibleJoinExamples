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
package spatialjoin;

import org.apache.asterix.external.cartilage.base.Configuration;
import org.apache.asterix.external.cartilage.base.FlexibleJoin;
import org.apache.asterix.external.cartilage.base.Summary;
import org.apache.asterix.external.cartilage.base.types.Rectangle;

import java.util.ArrayList;

class SpatialJoinConfiguration implements Configuration {
    public Rectangle Grid;
    public int n;

    SpatialJoinConfiguration(Rectangle MBR, int n) {
        this.Grid = MBR;
        this.n = n;
    }
}

class computeMBR implements Summary<Rectangle> {
    public Rectangle MBR = new Rectangle();

    @Override
    public void add(Rectangle k) {
        if (k.x1 < MBR.x1)
            MBR.x1 = k.x1;
        if (k.x2 > MBR.x2)
            MBR.x2 = k.x2;
        if (k.y1 < MBR.y1)
            MBR.y1 = k.y1;
        if (k.y2 > MBR.y2)
            MBR.y2 = k.y2;
    }

    @Override
    public void add(Summary<Rectangle> s) {
        computeMBR c = (computeMBR) s;
        if (c.MBR.x1 < MBR.x1)
            MBR.x1 = c.MBR.x1;
        if (c.MBR.x2 > MBR.x2)
            MBR.x2 = c.MBR.x2;
        if (c.MBR.y1 < MBR.y1)
            MBR.y1 = c.MBR.y1;
        if (c.MBR.y2 > MBR.y2)
            MBR.y2 = c.MBR.y2;
    }
}

public class SpatialJoin implements FlexibleJoin<Rectangle, SpatialJoinConfiguration> {
    private static long matchCounter = 0;
    private static long matchTrueCounter = 0;
    private static long matchFalseCounter = 0;
    private static boolean matchPrinted = false;
    @Override
    public Summary<Rectangle> createSummarizer1() {
        return new computeMBR();
    }

    @Override
    public SpatialJoinConfiguration divide(Summary<Rectangle> s1, Summary<Rectangle> s2) {
        computeMBR c1 = (computeMBR) s1;
        computeMBR c2 = (computeMBR) s2;

        if (c1.MBR.x1 < c2.MBR.x1)
            c1.MBR.x1 = c2.MBR.x1;
        if (c1.MBR.x2 > c2.MBR.x2)
            c1.MBR.x2 = c2.MBR.x2;
        if (c1.MBR.y1 < c2.MBR.y1)
            c1.MBR.y1 = c2.MBR.y1;
        if (c1.MBR.y2 > c2.MBR.y2)
            c1.MBR.y2 = c2.MBR.y2;

        this.matchCounter = 0;
        this.matchTrueCounter = 0;
        this.matchFalseCounter = 0;
        this.matchPrinted = false;

        return new SpatialJoinConfiguration(c1.MBR, 100);
    }

    @Override
    public int[] assign1(Rectangle k1, SpatialJoinConfiguration spatialJoinConfiguration) {
        ArrayList<Integer> tiles = new ArrayList<>();
        double minX = spatialJoinConfiguration.Grid.x1;
        double minY = spatialJoinConfiguration.Grid.y1;
        double maxX = spatialJoinConfiguration.Grid.x2;
        double maxY = spatialJoinConfiguration.Grid.y2;

        int rows = spatialJoinConfiguration.n;
        int columns = spatialJoinConfiguration.n;
        int row1 = (int) Math.ceil((k1.y1 - minY) * rows / (maxY - minY));
        int col1 = (int) Math.ceil((k1.x1 - minX) * columns / (maxX - minX));
        int row2 = (int) Math.ceil((k1.y2 - minY) * rows / (maxY - minY));
        int col2 = (int) Math.ceil((k1.x2 - minX) * columns / (maxX - minX));

        row1 = Math.min(Math.max(1, row1), rows * columns);
        col1 = Math.min(Math.max(1, col1), rows * columns);
        row2 = Math.min(Math.max(1, row2), rows * columns);
        col2 = Math.min(Math.max(1, col2), rows * columns);

        int minRow = Math.min(row1, row2);
        int maxRow = Math.max(row1, row2);
        int minCol = Math.min(col1, col2);
        int maxCol = Math.max(col1, col2);

        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                int tileId = (i - 1) * columns + j;
                tiles.add(tileId);
            }
        }

        return tiles.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public boolean match(int b1, int b2) {
        this.matchCounter++;
        //System.out.println("b1:"+b1+"\tb1Start:"+b1Start+"\tb1End:"+b1End+"b2:"+b2+"\tb2Start:"+b2Start+"\tb2End:"+b2End);
        boolean a = (b1 == b2);
        if(a) matchTrueCounter++;
        else matchFalseCounter++;
        return a;
    }

    @Override
    public boolean verify(Rectangle k1, Rectangle k2) {
        if(!this.matchPrinted) {
            System.out.println("match counter: " + this.matchCounter);
            System.out.println("match true counter: " + this.matchTrueCounter);
            System.out.println("match False counter: " + this.matchFalseCounter);
            this.matchPrinted = true;
        }
        return true;
//        double x1 = Math.max(k1.x1, k2.x1);
//        double y1 = Math.max(k1.y1, k2.y1);
//
//        double x2 = Math.min(k1.x2, k2.x2);
//        double y2 = Math.min(k1.y2, k2.y2);
//
//        return !(x1 > x2 || y1 > y2);
    }
}
