//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package geometryjoin_rt;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.asterix.external.cartilage.base.Configuration;
import org.apache.asterix.external.cartilage.base.FlexibleJoin;
import org.apache.asterix.external.cartilage.base.Summary;

class GeometryJoinConfiguration implements Configuration {
    public double[][] Grid;
    public int n;

    GeometryJoinConfiguration(double[][] MBR, int n) {
        this.Grid = MBR;
        this.n = n;
    }
}

class computeMBR implements Summary<OGCGeometry> {
    public double[][] MBR = new double[2][2];
    Envelope env = new Envelope();

    computeMBR() {
    }

    public void add(OGCGeometry k) {
        k.getEsriGeometry().queryEnvelope(this.env);
        if (this.env.getXMin() < this.MBR[0][0]) {
            this.MBR[0][0] = this.env.getXMin();
        }

        if (this.env.getXMax() > this.MBR[1][0]) {
            this.MBR[1][0] = this.env.getXMax();
        }

        if (this.env.getYMin() < this.MBR[0][1]) {
            this.MBR[0][1] = this.env.getYMin();
        }

        if (this.env.getYMax() > this.MBR[1][1]) {
            this.MBR[1][1] = this.env.getYMax();
        }

    }

    public void add(Summary<OGCGeometry> s) {
        computeMBR c = (computeMBR)s;
        if (c.MBR[0][0] < this.MBR[0][0]) {
            this.MBR[0][0] = c.MBR[0][0];
        }

        if (c.MBR[1][0] > this.MBR[1][0]) {
            this.MBR[1][0] = c.MBR[1][0];
        }

        if (c.MBR[0][1] < this.MBR[0][1]) {
            this.MBR[0][1] = c.MBR[0][1];
        }

        if (c.MBR[1][1] > this.MBR[1][1]) {
            this.MBR[1][1] = c.MBR[1][1];
        }

    }
}

public class GeometryJoin implements FlexibleJoin<OGCGeometry, GeometryJoinConfiguration> {
    public int n;
    Envelope env1 = new Envelope();

    public GeometryJoin(int n) {
        this.n = n;
    }

    public Summary<OGCGeometry> createSummarizer1() {
        return new computeMBR();
    }

    public GeometryJoinConfiguration divide(Summary<OGCGeometry> s1, Summary<OGCGeometry> s2) {
        computeMBR c1 = (computeMBR)s1;
        computeMBR c2 = (computeMBR)s2;
        if (c1.MBR[0][0] < c2.MBR[0][0]) {
            c1.MBR[0][0] = c2.MBR[0][0];
        }

        if (c1.MBR[1][0] > c2.MBR[1][0]) {
            c1.MBR[1][0] = c2.MBR[1][0];
        }

        if (c1.MBR[0][1] < c2.MBR[0][1]) {
            c1.MBR[0][1] = c2.MBR[0][1];
        }

        if (c1.MBR[1][1] > c2.MBR[1][1]) {
            c1.MBR[1][1] = c2.MBR[1][1];
        }

        return new GeometryJoinConfiguration(c1.MBR, this.n);
    }

    public int[] assign1(OGCGeometry k1, GeometryJoinConfiguration geometryJoinConfiguration) {
        double minX = geometryJoinConfiguration.Grid[0][0];
        double minY = geometryJoinConfiguration.Grid[0][1];
        double maxX = geometryJoinConfiguration.Grid[1][0];
        double maxY = geometryJoinConfiguration.Grid[1][1];

        int rows = geometryJoinConfiguration.n;
        int columns = geometryJoinConfiguration.n;

        k1.getEsriGeometry().queryEnvelope(this.env1);

        int row1 = (int)Math.ceil((this.env1.getYMin() - minY) * (double)rows / (maxY - minY));
        int col1 = (int)Math.ceil((this.env1.getXMin() - minX) * (double)columns / (maxX - minX));
        int row2 = (int)Math.ceil((this.env1.getYMax() - minY) * (double)rows / (maxY - minY));
        int col2 = (int)Math.ceil((this.env1.getXMax() - minX) * (double)columns / (maxX - minX));

        row1 = Math.min(Math.max(1, row1), rows);
        col1 = Math.min(Math.max(1, col1), columns);
        row2 = Math.min(Math.max(1, row2), rows);
        col2 = Math.min(Math.max(1, col2), columns);

        int minRow = Math.min(row1, row2);
        int maxRow = Math.max(row1, row2);
        int minCol = Math.min(col1, col2);
        int maxCol = Math.max(col1, col2);

        int size = (maxRow - minRow + 1) * (maxCol - minCol + 1);
        int[] returnArray = new int[size];
        int idx = 0;

        for(int i = minRow; i <= maxRow; ++i) {
            for(int j = minCol; j <= maxCol; ++j) {
                int tileId = (i - 1) * this.n + j;
                returnArray[idx] = tileId;
                ++idx;
            }
        }

        return returnArray;
    }

    public boolean verify(int b1, OGCGeometry k1, int b2, OGCGeometry k2, GeometryJoinConfiguration c) {
        if (this.verify(k1, k2)) {
            Envelope env1 = new Envelope();
            Envelope env2 = new Envelope();

            k1.getEsriGeometry().queryEnvelope(env1);
            k2.getEsriGeometry().queryEnvelope(env2);

            double k1x1 = env1.getXMin();
            double k1y1 = env1.getYMin();
            double k2x1 = env2.getXMin();
            double k2y1 = env2.getYMin();

            k1x1 = Math.max(k1x1, k2x1);
            k1y1 = Math.max(k1y1, k2y1);

            double minX = c.Grid[0][0];
            double minY = c.Grid[0][1];
            double maxX = c.Grid[1][0];
            double maxY = c.Grid[1][1];

            int row = (int) Math.ceil((k1y1 - minY) * this.n / (maxY - minY));
            int col = (int) Math.ceil((k1x1 - minX) * this.n / (maxX - minX));

            row = Math.min(Math.max(1, row), this.n * this.n);
            col = Math.min(Math.max(1, col), this.n * this.n);

            int tileId = (row - 1) * this.n + col;
            return tileId == b2;
        }

        return false;
    }

    public boolean verify(OGCGeometry k1, OGCGeometry k2) {
        return k1.contains(k2);
    }
}
