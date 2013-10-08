package mosquito.g4.voronoi;

import java.awt.geom.Point2D;

public class LineParam {
    private int section;
    private Point2D point;

    public LineParam(int section, Point2D point) {
        this.section = section;
        this.point = point;
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public Point2D getPoint() {
        return point;
    }

    public void setPoint(Point2D point) {
        this.point = point;
    }
}
