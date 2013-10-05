package mosquito.g4.voronoi;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Section implements Comparable<Section> {
    private int id;
    private List<Point2D> points;

    public Section(int id) {
        setId(id);
        points = new ArrayList<Point2D>();
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public List<Point2D> getPoints() {
        return points;
    }

    public void clearPoints() {
        getPoints().clear();
    }

    public void addPoint(Point2D point) {
        this.points.add(point);
    }

    // FIXME: inefficient, so use it if we need it

    // public boolean hasPoint(Point2D point) {
    // return points.contains(point);
    // }

    public Integer getPopulation() {
        return getPoints().size();
    }

    @Override
    public int compareTo(Section o) {
        return -getPopulation().compareTo(o.getPopulation());
    }

    public int hashCode() {
        return getId();
    }
}
