package mosquito.g4.voronoi;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mosquito.g4.utils.GeometryUtils;
import mosquito.g4.utils.Utils;

/**
 * Creates partitions of an n x n board so that each partition contains no
 * obstacles and is easily 'sweepable'. Each of these sections are convex in
 * nature to simplify the sweeping algorithm. The class stores the results in an
 * Sections object that orders each Section based on population.
 * 
 * @author Hari
 * 
 */
// TODO: if an obstacle is close to the wall, then the point associated with
// that obstacle might be 'out of bounds'. If no other point can reach the set
// of points behind the obstacle, then these points get a section id of '-1'

public class Voronoi {
    private static final double MIN_DISTANCE = .25;
    private Sections sections;
    private List<Section> sectionsAsList;

    private int boardSize;

    private int minSections;
    private int numSections;

    private Collection<Line2D> walls;
    private Collection<Point2D> voronoiPoints;

    int[][] sectionIdBoard;
    double[][] scoreBoard;
    public boolean debug = true;
    private PointToSectionConverter converter;

    public Voronoi(int minSections, int boardSize, Collection<Line2D> walls) {
        this.minSections = minSections;
        this.boardSize = boardSize;
        setupWalls(walls);
    }

    public void setupWalls(Collection<Line2D> walls) {
        this.walls = walls;
    }

    public void doVoronoi() {
        createVornoiPoints();
        setupBoards();
        calculateDistances();
        createSections();
        pruneEmptySections();
        System.err.println("Created " + getNumSections() + " sections");
        printSectionBoard();
    }

    private void pruneEmptySections() {
        List<Integer> empties = new LinkedList<Integer>();
        for (Section s : sections.getSections()) {
            if (s.getPoints().isEmpty()) {
                empties.add(s.getId());
            }
        }

        int numRemoved = 0;

        for (Integer i : empties) {
            sectionsAsList.remove(i - numRemoved);
            ++numRemoved;
        }

        int i = 0;

        for (Section s : sectionsAsList) {
            s.setId(i);
            ++i;
        }

        this.numSections = sectionsAsList.size();

        sections.getSections().clear();
        sections.getSections().addAll(sectionsAsList);

        redoSectionIdBoard();
    }

    private void redoSectionIdBoard() {
        for (Section s : sectionsAsList) {
            for (Point2D p : s.getPoints()) {
                int x = (int) p.getX();
                int y = (int) p.getY();

                sectionIdBoard[x][y] = s.getId();
            }
        }
    }

    private void createVornoiPoints() {
        LinkedList<Point2D> list = new LinkedList<Point2D>();

        for (Line2D wall : walls) {
            java.awt.geom.Line2D line = Utils.getPerpendicularLine(wall,
                    MIN_DISTANCE);

            Point2D p1 = line.getP1();
            Point2D p2 = line.getP2();

            Voronoi.conditionallyAddPoint(list, p1, 0, boardSize);
            Voronoi.conditionallyAddPoint(list, p2, 0, boardSize);
        }

        List<Point2D> intersectionPoints = createIntersectionVoronoiPoints();

        list.addAll(intersectionPoints);

        if (list.isEmpty()) {
            list.add(new Point2D.Double(0, 0));
        }

        removeUnneededPoints(list);
        setVoronoiPoints(list);
        System.err.println(list);
    }

    private List<Point2D> createIntersectionVoronoiPoints() {
        Set<Point2D> intersectedPoints = new HashSet<Point2D>();

        for (Line2D wall : walls) {
            for (Line2D wall2 : walls) {
                if (wall == wall2)
                    continue;

                Point2D[] intersect = new Point2D[1];

                boolean isIntersecting = GeometryUtils
                        .findLineSegmentIntersection(wall, wall2, intersect);

                if (isIntersecting) {
                    intersectedPoints.add(intersect[0]);
                    System.err.println("" + "intersection at " + intersect[0]);
                }
            }
        }

        List<Point2D> newPoints = new LinkedList<Point2D>();

        for (Point2D p : intersectedPoints) {
            makePointsFromIntersectionPoint(newPoints, p);
        }

        return newPoints;
    }

    public static void makePointsFromIntersectionPoint(List<Point2D> newPoints,
            Point2D p) {
        newPoints.add(new Point2D.Double(p.getX() + MIN_DISTANCE, p.getY()
                + MIN_DISTANCE));
        newPoints.add(new Point2D.Double(p.getX() - MIN_DISTANCE, p.getY()
                + MIN_DISTANCE));
        newPoints.add(new Point2D.Double(p.getX() + MIN_DISTANCE, p.getY()
                - MIN_DISTANCE));
        newPoints.add(new Point2D.Double(p.getX() - MIN_DISTANCE, p.getY()
                - MIN_DISTANCE));
    }

    public void removeUnneededPoints(LinkedList<Point2D> list) {
        Collections.sort(list, new Comparator<Point2D>() {

            @Override
            public int compare(Point2D o1, Point2D o2) {
                return o1.getX() > o2.getX() ? 1 : -1;
            }
        });

        for (int i = 0; i < list.size(); i++) {
            Point2D p1 = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                Point2D p2 = list.get(j);

                Line2D line = new Line2D.Double(p1, p2);

                boolean intersects = false;
                for (Line2D wall : walls) {
                    if (line.intersectsLine(wall)) {
                        intersects = true;
                        break;
                    }
                }

                if (!intersects) {
                    list.remove(j);
                    j--;
                }
            }
        }
    }

    private void setupBoards() {
        sectionIdBoard = new int[boardSize][boardSize];
        scoreBoard = new double[boardSize][boardSize];

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                sectionIdBoard[i][j] = -1;
                scoreBoard[i][j] = Double.MAX_VALUE;
            }
        }
    }

    private void calculateDistances() {
        int i = 0;

        for (Point2D point : voronoiPoints) {
            System.err.println("Sectioning using " + Utils.itoa(i)
                    + " at point " + point);
            calculateDistance(point, i);
            ++i;
            printSectionBoard();
        }

        this.setNumSections(i);
    }

    private void calculateDistance(Point2D point, int sectionId) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                calculateDistance(point, sectionId, i, j);
            }
        }
    }

    private void calculateDistance(Point2D point, int sectionId,
            List<Point2D> points) {
        for (Point2D target : points) {
            calculateDistance(point, sectionId, (int) target.getX(),
                    (int) target.getY());
        }
    }

    private void calculateDistance(Point2D point, int sectionId, int i, int j) {
        Point2D target = new Point2D.Double(i, j);

        if (Utils.hasStraightPath(point, target, walls)) {
            double distance = point.distance(target);

            boolean unoccupied = sectionIdBoard[i][j] == -1;
            if (unoccupied || distance < scoreBoard[i][j]) {
                sectionIdBoard[i][j] = sectionId;
                scoreBoard[i][j] = distance;
            }
        }
    }

    private void printSectionBoard() {
        Utils.print(System.err, sectionIdBoard, true);
        Utils.print(System.err, sectionIdBoard, false);
    }

    private void createSections() {
        this.converter = new PointToSectionConverter(sectionIdBoard);
        this.sections = new Sections(this.converter);
        sectionsAsList = new ArrayList<Section>(getNumSections());

        // populate sections array
        for (int i = 0; i < getNumSections(); i++) {
            Section section = new Section(i);
            sectionsAsList.add(section);
        }

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                putPointInSection(i, j);
            }
        }

        // add each sections to heap
        for (int i = 0; i < sectionsAsList.size(); i++) {
            this.sections.addSection(sectionsAsList.get(i));
        }

        while (getNumSections() < minSections) {
            splitSection();
        }

        assignMissedElements();
    }

    private void assignMissedElements() {

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (sectionIdBoard[i][j] == -1) {
                    Section s = new Section(numSections);
                    sectionsAsList.add(s);
                    s.addPoint(new Point2D.Double(i, j));
                    ++numSections;
                }
            }
        }
    }

    public void putPointInSection(int i, int j) {
        int sectionId = this.sectionIdBoard[i][j];
        if (sectionId >= 0) {
            sectionsAsList.get(sectionId).addPoint(new Point2D.Double(i, j));
        }
    }

    private void splitSection() {
        Section poppedSection = sections.pop();

        List<Point2D> src = poppedSection.getPoints();
        List<Point2D> points = new ArrayList<Point2D>(src);

        Point2D nextVoronoiPoint = Utils.getRandomElement(points);
        int nextSectionId = getNumSections();
        sectionsAsList.add(new Section(nextSectionId));

        calculateDistance(nextVoronoiPoint, nextSectionId, points);
        poppedSection.clearPoints();

        for (Point2D p : points) {
            putPointInSection((int) p.getX(), (int) p.getY());
        }

        sections.addSection(sectionsAsList.get(getNumSections()));
        sections.addSection(poppedSection);
        setNumSections(getNumSections() + 1);
    }

    private void makeConvex() {
        for (int x = 1; x < sectionIdBoard.length; x++) {
            for (int y = 0; y < sectionIdBoard[0].length; y++) {
                int newX = x + 2;
                int newY = y - 1;
                int section = sectionIdBoard[x][y];

                if (Utils.withinBounds(0, sectionIdBoard.length, newX)
                        && Utils.withinBounds(0, sectionIdBoard[0].length, newY)) {
                    if (sectionIdBoard[newX][newY] == section) {
                        sectionIdBoard[x + 1][y] = section;
                    }
                }
            }
        }
    }

    public int[][] getSectionIdBoard() {
        return this.sectionIdBoard;
    }

    public Collection<Point2D> getVoronoiPoints() {
        return voronoiPoints;
    }

    private void setVoronoiPoints(Collection<Point2D> voronoiPoints) {
        this.voronoiPoints = voronoiPoints;
    }

    public int getNumSections() {
        return numSections;
    }

    private void setNumSections(int numSections) {
        this.numSections = numSections;
    }

    private static void conditionallyAddPoint(List<java.awt.geom.Point2D> list,
            java.awt.geom.Point2D point, int minDim, int maxDim) {
        if (Utils.withinBounds(minDim, maxDim, point.getX())
                && Utils.withinBounds(minDim, maxDim, point.getY())) {
            list.add(point);
        }
    }

    public Sections getSections() {
        return sections;
    }

    public List<Section> getSectionList() {
        return sectionsAsList;
    }

    public static void main(String[] args) {
        Set<Line2D> walls = new HashSet<Line2D>();
        walls.add(new Line2D.Double(5, 0, 49.9, 98.9));
        Voronoi v = new Voronoi(2, 100, walls);
        v.debug = true;
        v.doVoronoi();
    }
}
