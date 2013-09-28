package mosquito.g4.voronoi;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

public class Section implements Comparable<Section> {
    private int id;
    private Set<Point2D.Double> points;

    public Section(int id) {
        setId(id);
        points = new HashSet<Point2D.Double>();
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public Set<Point2D.Double> getPoints() {
        return points;
    }

    public void addPoint(Point2D.Double point) {
        this.points.add(point);
    }

    public boolean hasPoint(Point2D.Double point) {
        return points.contains(point);
    }

    public Integer getPopulation() {
        return getPoints().size();
    }

    @Override
    public int compareTo(Section o) {
        return getPopulation().compareTo(o.getPopulation());
    }

    public int hashCode() {
        return getId();
    }
}
