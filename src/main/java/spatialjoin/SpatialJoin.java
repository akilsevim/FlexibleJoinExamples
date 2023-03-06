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

import java.util.ArrayList;

class SpatialJoinConfiguration implements Configuration {
    public double[][] Grid;
    public int n;

    SpatialJoinConfiguration(double[][] MBR, int n) {
        this.Grid = MBR;
        this.n = n;
    }
}

class computeMBR implements Summary<double[][]> {
    public double[][] MBR = new double[2][2];

//    public ArrayList<double[][]> sample = new ArrayList<>();

    @Override
    public void add(double[][] k) {
//        if(Math.random() < 0.01) sample.add(k);
        if (k[0][0] < MBR[0][0])
            MBR[0][0] = k[0][0];
        if (k[1][0] > MBR[1][0])
            MBR[1][0] = k[1][0];
        if (k[0][1] < MBR[0][1])
            MBR[0][1] = k[0][1];
        if (k[1][1] > MBR[1][1])
            MBR[1][1] = k[1][1];
    }

    @Override
    public void add(Summary<double[][]> s) {
        computeMBR c = (computeMBR) s;
//        sample.addAll(c.sample);
        if (c.MBR[0][0] < MBR[0][0])
            MBR[0][0] = c.MBR[0][0];
        if (c.MBR[1][0] > MBR[1][0])
            MBR[1][0] = c.MBR[1][0];
        if (c.MBR[0][1] < MBR[0][1])
            MBR[0][1] = c.MBR[0][1];
        if (c.MBR[1][1] > MBR[1][1])
            MBR[1][1] = c.MBR[1][1];
    }
}

public class SpatialJoin implements FlexibleJoin<double[][], SpatialJoinConfiguration> {
    private static long matchCounter = 0;
    private static long matchTrueCounter = 0;
    private static long matchFalseCounter = 0;
    private static boolean matchPrinted = false;
    private int n;
    public SpatialJoin(int n) {
        this.n = n;
    }
    @Override
    public Summary<double[][]> createSummarizer1() {
        return new computeMBR();
    }

    @Override
    public SpatialJoinConfiguration divide(Summary<double[][]> s1, Summary<double[][]> s2) {
        computeMBR c1 = (computeMBR) s1;
        computeMBR c2 = (computeMBR) s2;

//        c1.sample.addAll(c2.sample);
//
//        for(int n = 10; n < 1000; n+=10) {
//            for (double[][] r : c1.sample) {
//
//            }
//        }

        if (c1.MBR[0][0] > c2.MBR[0][0])
            c1.MBR[0][0] = c2.MBR[0][0];
        if (c1.MBR[1][0] < c2.MBR[1][0])
            c1.MBR[1][0] = c2.MBR[1][0];
        if (c1.MBR[0][1] > c2.MBR[0][1])
            c1.MBR[0][1] = c2.MBR[0][1];
        if (c1.MBR[1][1] < c2.MBR[1][1])
            c1.MBR[1][1] = c2.MBR[1][1];

        this.matchCounter = 0;
        this.matchTrueCounter = 0;
        this.matchFalseCounter = 0;
        this.matchPrinted = false;

        return new SpatialJoinConfiguration(c1.MBR, n);
    }

    @Override
    public int[] assign1(double[][] k1, SpatialJoinConfiguration spatialJoinConfiguration) {

        double minX = spatialJoinConfiguration.Grid[0][0];
        double minY = spatialJoinConfiguration.Grid[0][1];
        double maxX = spatialJoinConfiguration.Grid[1][0];
        double maxY = spatialJoinConfiguration.Grid[1][1];

        int rows = spatialJoinConfiguration.n;
        int columns = spatialJoinConfiguration.n;
        int row1 = (int) Math.ceil((k1[0][1] - minY) * rows / (maxY - minY));
        int col1 = (int) Math.ceil((k1[0][0] - minX) * columns / (maxX - minX));
        int row2 = (int) Math.ceil((k1[1][1] - minY) * rows / (maxY - minY));
        int col2 = (int) Math.ceil((k1[1][0] - minX) * columns / (maxX - minX));

        row1 = Math.min(Math.max(1, row1), rows * columns);
        col1 = Math.min(Math.max(1, col1), rows * columns);
        row2 = Math.min(Math.max(1, row2), rows * columns);
        col2 = Math.min(Math.max(1, col2), rows * columns);

        int minRow = Math.min(row1, row2);
        int maxRow = Math.max(row1, row2);
        int minCol = Math.min(col1, col2);
        int maxCol = Math.max(col1, col2);

        int size = (maxRow - minRow + 1) * (maxCol - minCol + 1);
        //ArrayList<Integer> tiles = new ArrayList<>();
        int[] tiles = new int[size];
        int idx = 0;
        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                int tileId = (i - 1) * columns + j;
                tiles[idx] = tileId;
                idx++;
            }
        }

        return tiles;
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
    public boolean verify(double[][] k1, double[][] k2) {
//        if(!this.matchPrinted) {
//            System.out.println("match counter: " + this.matchCounter);
//            System.out.println("match true counter: " + this.matchTrueCounter);
//            System.out.println("match False counter: " + this.matchFalseCounter);
//            this.matchPrinted = true;
//        }
//        return true;
        double x1 = Math.max(k1[0][0], k2[0][0]);
        double y1 = Math.max(k1[0][1], k2[0][1]);

        double x2 = Math.min(k1[1][0], k2[1][0]);
        double y2 = Math.min(k1[1][1], k2[1][1]);

        return !(x1 > x2 || y1 > y2);
    }
}

///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package spatialjoin;
//
//        import org.apache.asterix.external.cartilage.base.Configuration;
//        import org.apache.asterix.external.cartilage.base.FlexibleJoin;
//        import org.apache.asterix.external.cartilage.base.Summary;
//
//        import java.util.ArrayList;
//
//class SpatialJoinConfiguration implements Configuration {
//    public double[] Grid;
//    public int n;
//
//    SpatialJoinConfiguration(double[] MBR, int n) {
//        this.Grid = MBR;
//        this.n = n;
//    }
//}
//
//class computeMBR implements Summary<double[]> {
//    public double[] MBR = new double[4];
//
//    @Override
//    public void add(double[] k) {
//        if (k[0] < MBR[0])
//            MBR[0] = k[0];
//        if (k[2] > MBR[2])
//            MBR[2] = k[2];
//        if (k[1] < MBR[1])
//            MBR[1] = k[1];
//        if (k[3] > MBR[3])
//            MBR[3] = k[3];
//    }
//
//    @Override
//    public void add(Summary<double[]> s) {
//        computeMBR c = (computeMBR) s;
//        if (c.MBR[0] < MBR[0])
//            MBR[0] = c.MBR[0];
//        if (c.MBR[1] > MBR[1])
//            MBR[1] = c.MBR[1];
//        if (c.MBR[2] < MBR[2])
//            MBR[2] = c.MBR[2];
//        if (c.MBR[3] > MBR[3])
//            MBR[3] = c.MBR[3];
//    }
//}
//
//public class SpatialJoin implements FlexibleJoin<double[], SpatialJoinConfiguration> {
//    private static long matchCounter = 0;
//    private static long matchTrueCounter = 0;
//    private static long matchFalseCounter = 0;
//    private static boolean matchPrinted = false;
//
//    private int n;
//    public SpatialJoin(int n) {
//        this.n = n;
//    }
//    @Override
//    public Summary<double[]> createSummarizer1() {
//        return new computeMBR();
//    }
//
//    @Override
//    public SpatialJoinConfiguration divide(Summary<double[]> s1, Summary<double[]> s2) {
//        computeMBR c1 = (computeMBR) s1;
//        computeMBR c2 = (computeMBR) s2;
//
//        if (c1.MBR[0] > c2.MBR[0])
//            c1.MBR[0] = c2.MBR[0];
//        if (c1.MBR[1] < c2.MBR[1])
//            c1.MBR[1] = c2.MBR[1];
//        if (c1.MBR[2] > c2.MBR[2])
//            c1.MBR[2] = c2.MBR[2];
//        if (c1.MBR[3] < c2.MBR[3])
//            c1.MBR[3] = c2.MBR[3];
//
//        this.matchCounter = 0;
//        this.matchTrueCounter = 0;
//        this.matchFalseCounter = 0;
//        this.matchPrinted = false;
//
//        return new SpatialJoinConfiguration(c1.MBR, n);
//    }
//
//    @Override
//    public int[] assign1(double[] k1, SpatialJoinConfiguration spatialJoinConfiguration) {
//        ArrayList<Integer> tiles = new ArrayList<>();
//        double minX = spatialJoinConfiguration.Grid[0];
//        double minY = spatialJoinConfiguration.Grid[1];
//        double maxX = spatialJoinConfiguration.Grid[2];
//        double maxY = spatialJoinConfiguration.Grid[3];
//
//        int rows = spatialJoinConfiguration.n;
//        int columns = spatialJoinConfiguration.n;
//        int row1 = (int) Math.ceil((k1[1] - minY) * rows / (maxY - minY));
//        int col1 = (int) Math.ceil((k1[0] - minX) * columns / (maxX - minX));
//        int row2 = (int) Math.ceil((k1[3] - minY) * rows / (maxY - minY));
//        int col2 = (int) Math.ceil((k1[2] - minX) * columns / (maxX - minX));
//
//        row1 = Math.min(Math.max(1, row1), rows);
//        col1 = Math.min(Math.max(1, col1), columns);
//        row2 = Math.min(Math.max(1, row2), rows);
//        col2 = Math.min(Math.max(1, col2), columns);
//
//        int minRow = Math.min(row1, row2);
//        int maxRow = Math.max(row1, row2);
//        int minCol = Math.min(col1, col2);
//        int maxCol = Math.max(col1, col2);
//
//        for (int i = minRow; i <= maxRow; i++) {
//            for (int j = minCol; j <= maxCol; j++) {
//                int tileId = (i - 1) * columns + j;
//                tiles.add(tileId);
//            }
//        }
//
//        return tiles.stream().mapToInt(i -> i).toArray();
//    }
//
//    @Override
//    public boolean match(int b1, int b2) {
//        this.matchCounter++;
//        //System.out.println("b1:"+b1+"\tb1Start:"+b1Start+"\tb1End:"+b1End+"b2:"+b2+"\tb2Start:"+b2Start+"\tb2End:"+b2End);
//        boolean a = (b1 == b2);
//        if(a) matchTrueCounter++;
//        else matchFalseCounter++;
//        return a;
//    }
//
//    @Override
//    public boolean verify(double[] k1, double[] k2) {
////        if(!this.matchPrinted) {
////            System.out.println("match counter: " + this.matchCounter);
////            System.out.println("match true counter: " + this.matchTrueCounter);
////            System.out.println("match False counter: " + this.matchFalseCounter);
////            this.matchPrinted = true;
////        }
////        return true;
//        double x1 = Math.max(k1[0], k2[0]);
//        double y1 = Math.max(k1[2], k2[2]);
//
//        double x2 = Math.min(k1[1], k2[1]);
//        double y2 = Math.min(k1[3], k2[3]);
//
//        return !(x1 > x2 || y1 > y2);
//    }
//}

