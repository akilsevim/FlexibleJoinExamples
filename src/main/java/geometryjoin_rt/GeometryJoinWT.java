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
package geometryjoin_rt;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.asterix.external.cartilage.base.FlexibleJoin;
import org.apache.asterix.external.cartilage.base.Summary;

import java.util.ArrayList;

public class GeometryJoinWT implements FlexibleJoin<OGCGeometry, GeometryJoinConfiguration> {
    int n;
    Envelope env1 = new Envelope();
    public GeometryJoinWT(int n) {
        this.n = n;
    }
    @Override
    public Summary<OGCGeometry> createSummarizer1() {
        return new computeMBR();
    }

    @Override
    public GeometryJoinConfiguration divide(Summary<OGCGeometry> s1, Summary<OGCGeometry> s2) {
        computeMBR c1 = (computeMBR) s1;
        computeMBR c2 = (computeMBR) s2;

        if (c1.MBR[0][0] < c2.MBR[0][0])
            c1.MBR[0][0] = c2.MBR[0][0];
        if (c1.MBR[1][0] > c2.MBR[1][0])
            c1.MBR[1][0] = c2.MBR[1][0];
        if (c1.MBR[0][1] < c2.MBR[0][1])
            c1.MBR[0][1] = c2.MBR[0][1];
        if (c1.MBR[1][1] > c2.MBR[1][1])
            c1.MBR[1][1] = c2.MBR[1][1];

        return new GeometryJoinConfiguration(c1.MBR, n);
    }

    @Override
    public int[] assign1(OGCGeometry k1, GeometryJoinConfiguration geometryJoinConfiguration) {
        double minX = geometryJoinConfiguration.Grid[0][0];
        double minY = geometryJoinConfiguration.Grid[0][1];
        double maxX = geometryJoinConfiguration.Grid[1][0];
        double maxY = geometryJoinConfiguration.Grid[1][1];

        int rows = geometryJoinConfiguration.n;
        int columns = geometryJoinConfiguration.n;

        k1.getEsriGeometry().queryEnvelope(env1);

        int row1 = (int) Math.ceil((env1.getYMin() - minY) * rows / (maxY - minY));
        int col1 = (int) Math.ceil((env1.getXMin() - minX) * columns / (maxX - minX));
        int row2 = (int) Math.ceil((env1.getYMax() - minY) * rows / (maxY - minY));
        int col2 = (int) Math.ceil((env1.getXMax() - minX) * columns / (maxX - minX));

        row1 = Math.min(Math.max(1, row1), rows);
        col1 = Math.min(Math.max(1, col1), columns);
        row2 = Math.min(Math.max(1, row2), rows);
        col2 = Math.min(Math.max(1, col2), columns);

        int minRow = Math.min(row1, row2);
        int maxRow = Math.max(row1, row2);
        int minCol = Math.min(col1, col2);
        int maxCol = Math.max(col1, col2);

        int size = ((maxRow-minRow)+1) * ((maxCol-minCol)+1);
        //int[] returnArray = new int[size];
        ArrayList<Integer> tileIds = new ArrayList<>();
        int idx = 0;
        StringBuilder tileIdString = new StringBuilder();
        StringBuilder tileIdStringN = new StringBuilder();
        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                int tileId = (i - 1) * this.n + j;
                if(k1.intersects(getTileEnvelope(i, j, geometryJoinConfiguration, k1.getEsriSpatialReference()))) {
                    tileIds.add(tileId);
                    tileIdString.append(tileId).append(",");

                } else {
                    tileIdStringN.append(tileId).append(",");
                }
            }
        }
        System.out.println("Intersecting:" + tileIdString);
        //System.out.println("Not Intersecting:" + tileIdStringN);
        int[] returnArray = new int[tileIds.size()];
        for(int i = 0; i < tileIds.size(); i++) returnArray[i] = tileIds.get(i);
        return returnArray;
    }

    @Override
    public boolean verify(OGCGeometry k1, OGCGeometry k2) {
        return k1.intersects(k2);
    }

    public OGCGeometry getTileEnvelope(int row, int column, GeometryJoinConfiguration C, SpatialReference spatialReference) {

        double tileLength = (C.Grid[1][0] - C.Grid[0][0]) / C.n;
        double tileHeight = (C.Grid[1][1] - C.Grid[0][1]) / C.n;


        double tileMinX = row * tileLength;
        double tileMinY = column * tileHeight;
        System.out.println("tileMinX:"+tileMinX+", tileMinY:"+tileMinY);
        double tileMaxX = (row + 1) * tileLength;
        double tileMaxY = (column + 1) * tileHeight;
        OGCGeometry ogcGeometry = OGCGeometry.createFromEsriGeometry(new Envelope(tileMinX, tileMinY, tileMaxX, tileMaxY).getBoundary(), spatialReference);
        System.out.println(ogcGeometry.asGeoJson());
        return ogcGeometry;

    }
}
