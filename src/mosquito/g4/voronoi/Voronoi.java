package mosquito.g4.voronoi;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mosquito.g4.utils.Utils;

/**
 * Creates partitions of an n x n board so that each partition contains no
 * obstacles and is easily 'sweepable'. Each of these sections are convex in
 * nature to simplify the sweeping algorithm. The class stores the results in an
 * Sections object that orders each Section.
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
    private int boardSize;
    private int minSections;
    private Iterable<Line2D> walls;
    private Iterable<Point2D> voronoiPoints;

    int[][] sectionIdBoard;
    double[][] scoreBoard;
    private boolean debug;
    private int numSections;

    public Voronoi(int minSections, int boardSize, Iterable<Line2D> walls) {
        this.minSections = minSections;
        this.boardSize = boardSize;
        this.walls = walls;
    }

    public void doVoronoi() {
        createVornoiPoints();
        setupBoards();
        calculateDistances();
        printSectionBoard();
        createSections();
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

        this.setNumSections(i - 1);
    }

    private void calculateDistance(Point2D point, int sectionId) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {

                Point2D.Double target = new Point2D.Double(i, j);

                if (Utils.hasStraightPath(point, target, walls)) {
                    double distance = point.distance(i, j);

                    if (sectionIdBoard[i][j] == -1
                            || distance < scoreBoard[i][j]) {
                        sectionIdBoard[i][j] = sectionId;
                        scoreBoard[i][j] = distance;
                    }
                }
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
        Section[] sections = new Section[getNumSections()];

        for (int i = 0; i < sections.length; i++) {
            Section section = new Section(i);
            sections[i] = section;
            this.sections.addSection(section);
        }

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                int sectionId = this.sectionIdBoard[i][j];
                sections[sectionId].addPoint(new Point2D.Double(i, j));
            }
        }

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
        if (withinBounds(minDim, maxDim, point.getX())
                && withinBounds(minDim, maxDim, point.getY())) {
            list.add(point);
        }
    }

    public static boolean withinBounds(int minDim, int maxDim, double d) {
        return d >= minDim && d <= maxDim;
    }

    public static void main(String[] args) {
        Set<Line2D> walls = new HashSet<Line2D>();
        walls.add(new Line2D.Double(25.0, 0, 25.0, 98.9));
        Voronoi v = new Voronoi(5, 100, walls);
        v.debug = true;
        v.doVoronoi();

        // System.out.println(new Line2D.Double(25, 0, 25, 99)
        // .intersectsLine(new Line2D.Double(20, 49.5, 25, 100)));

    }
}
