package mosquito.g4.voronoi;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import mosquito.g4.utils.Utils;

public class SectionLineDrawer {
    private Map<Integer, List<Point2D>> pointsForSection;
    private int[][] sectionIdBoard;

    public SectionLineDrawer(int[][] sectionIdBoard) {
        pointsForSection = new HashMap<Integer, List<Point2D>>();
        this.sectionIdBoard = sectionIdBoard;
    }

    public ArrayList<Line2D> createLines() {
        Stack<LineParam> tempStack = new Stack<LineParam>();
        LinkedList<LineParam> tempList = new LinkedList<LineParam>();

        for (int x = 0; x < sectionIdBoard.length; x++) {

            Collection<LineParam> temp = x % 2 == 0 ? tempList : tempStack;
            temp.clear();

            for (int y = 0; y < sectionIdBoard[0].length; y++) {
                int section = sectionIdBoard[x][y];

                // look right:
                int rightX = x + 1;
                if (Utils.withinBounds(0, sectionIdBoard.length, rightX)) {
                    if (sectionIdBoard[rightX][y] != section) {
                        temp.add(new LineParam(section, new Point2D.Double(
                                x + .5, y)));
                    }
                }

                // look down:
                int downY = y + 1;
                if (Utils.withinBounds(0, sectionIdBoard.length, downY)) {
                    if (sectionIdBoard[x][downY] != section) {
                        temp.add(new LineParam(section, new Point2D.Double(x,
                                y + .5)));
                    }
                }
            }

            if (x % 2 == 1) {
                Utils.addAll(pointsForSection, (Stack<LineParam>) temp);
            } else {
                Utils.addAll(pointsForSection, (List<LineParam>) temp);
            }
        }

        return getLines();
    }

    private ArrayList<Line2D> getLines() {
        ArrayList<Line2D> lines = new ArrayList<Line2D>();
        for (Integer key : pointsForSection.keySet()) {
            lines.addAll(makeLinesFromList(pointsForSection.get(key)));
        }
        return lines;
    }

    public ArrayList<Line2D> makeLinesFromList(List<Point2D> perimeterPoints) {
        ArrayList<Line2D> lines = new ArrayList<Line2D>(perimeterPoints.size());
        Point2D prev = null;
        Point2D current = null;
        Point2D head = null;

        for (Point2D point : perimeterPoints) {
            if (head == null) {
                head = point;
            }
            if (prev == null) {
                prev = point;
            } else {
                current = point;

                // FIXME: this is a hack
                if (prev.distance(current) < 2) {
                    Line2D.Double line = new Line2D.Double(prev, current);
                    lines.add(line);
                }
                prev = current;
            }
        }

        return lines;
    }

    public static void main(String[] args) {
        Set<Line2D> walls = new HashSet<Line2D>();
        walls.add(new Line2D.Double(0, .5, 5, .5));

        Voronoi v = new Voronoi(3, 5, walls);
        v.debug = true;
        v.doVoronoi();

        SectionLineDrawer drawer = new SectionLineDrawer(v.getSectionIdBoard());
        System.out.println(drawer.createLines());

    }
}