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
package geometryjoin;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.asterix.external.cartilage.base.Configuration;
import org.apache.asterix.external.cartilage.base.FlexibleJoin;
import org.apache.asterix.external.cartilage.base.Summary;
import org.apache.asterix.external.cartilage.base.types.Rectangle;

import java.util.ArrayList;

class GeometryJoinConfiguration implements Configuration {
    public Rectangle Grid;
    public int n;

    GeometryJoinConfiguration(Rectangle MBR, int n) {
        this.Grid = MBR;
        this.n = n;
    }
}

class computeMBR implements Summary<OGCGeometry> {
    public Rectangle MBR = new Rectangle();

    @Override
    public void add(OGCGeometry k) {
        Envelope env = new Envelope();
        k.getEsriGeometry().queryEnvelope(env);
        if (env.getXMin() < MBR.x1)
            MBR.x1 = env.getXMin();
        if (env.getXMax() > MBR.x2)
            MBR.x2 = env.getXMax();
        if (env.getYMin() < MBR.y1)
            MBR.y1 = env.getYMin();
        if (env.getYMax() > MBR.y2)
            MBR.y2 = env.getYMax();
    }

    @Override
    public void add(Summary<OGCGeometry> s) {
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

public class
GeometryJoin implements FlexibleJoin<OGCGeometry, GeometryJoinConfiguration> {
    private int n;
    public GeometryJoin(int n) {
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

        if (c1.MBR.x1 < c2.MBR.x1)
            c1.MBR.x1 = c2.MBR.x1;
        if (c1.MBR.x2 > c2.MBR.x2)
            c1.MBR.x2 = c2.MBR.x2;
        if (c1.MBR.y1 < c2.MBR.y1)
            c1.MBR.y1 = c2.MBR.y1;
        if (c1.MBR.y2 > c2.MBR.y2)
            c1.MBR.y2 = c2.MBR.y2;

        return new GeometryJoinConfiguration(c1.MBR, n);
    }

    @Override
    public int[] assign1(OGCGeometry k1, GeometryJoinConfiguration geometryJoinConfiguration) {
        ArrayList<Integer> tiles = new ArrayList<>();
        double minX = geometryJoinConfiguration.Grid.x1;
        double minY = geometryJoinConfiguration.Grid.y1;
        double maxX = geometryJoinConfiguration.Grid.x2;
        double maxY = geometryJoinConfiguration.Grid.y2;

        int rows = geometryJoinConfiguration.n;
        int columns = geometryJoinConfiguration.n;

        Envelope env1 = new Envelope();
        k1.getEsriGeometry().queryEnvelope(env1);

        int row1 = (int) Math.ceil((env1.getYMin() - minY) * rows / (maxY - minY));
        int col1 = (int) Math.ceil((env1.getXMin() - minX) * columns / (maxX - minX));
        int row2 = (int) Math.ceil((env1.getYMax() - minY) * rows / (maxY - minY));
        int col2 = (int) Math.ceil((env1.getXMax() - minX) * columns / (maxX - minX));

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
    public boolean verify(OGCGeometry k1, OGCGeometry k2) {
        return k1.intersects(k2);
    }
}
