package mosquito.g4.voronoi;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
    private static final double MIN_DISTANCE = 10;
    private Sections sections;
    private List<Section> sectionsAsList;

    private int boardSize;

    private int minSections;
    private int numSections;

    private Iterable<Line2D> walls;
    private Iterable<Point2D> voronoiPoints;

    int[][] sectionIdBoard;
    double[][] scoreBoard;
    private boolean debug;

    public Voronoi(int minSections, int boardSize, Iterable<Line2D> walls) {
        this.minSections = minSections;
        this.boardSize = boardSize;
        this.walls = walls;
    }

    public void doVoronoi() {
        createVornoiPoints();
        setupBoards();
        calculateDistances();
        createSections();
        printSectionBoard();
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
        setVoronoiPoints(list);
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
            calculateDistance(point, i);
            ++i;
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

            if (sectionIdBoard[i][j] == -1 || distance < scoreBoard[i][j]) {
                sectionIdBoard[i][j] = sectionId;
                scoreBoard[i][j] = distance;
            }
        }
    }

    private void printSectionBoard() {
        if (debug) {
            Utils.print(System.err, sectionIdBoard);
        }
    }

    private void createSections() {
        this.sections = new Sections();
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

        this.sections.setSectionBoard(sectionIdBoard);

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

    public Iterable<Point2D> getVoronoiPoints() {
        return voronoiPoints;
    }

    private void setVoronoiPoints(Iterable<Point2D> voronoiPoints) {
        this.voronoiPoints = voronoiPoints;
    }

    private int getNumSections() {
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

    public static void main(String[] args) {
        Set<Line2D> walls = new HashSet<Line2D>();
        walls.add(new Line2D.Double(50.1, 0, 49.9, 98.9));
        Voronoi v = new Voronoi(5, 100, walls);
        v.debug = true;
        v.doVoronoi();
    }
}
