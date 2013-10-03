package mosquito.g4;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

public class Quadrant {

    static final int NUM_QUADRANTS = 16;
    private static final int NUM_COLUMNS = (int) Math.sqrt(NUM_QUADRANTS);
    private static final int HEIGHT = 100;
    private static final double HEIGHT_PER_QUADRANT = HEIGHT
            / (1.0 * NUM_COLUMNS);

    static int getQuadrant(double x, double y) {
        int column = (int) (x / HEIGHT_PER_QUADRANT);
        int row = (int) (y / HEIGHT_PER_QUADRANT);

        return row * NUM_COLUMNS + column;
    }

    static Point2D getCenter() {
        return new Point2D.Double(HEIGHT / 2, HEIGHT / 2);
    }

    static Point2D getCenterOfQuadrant(int quadrant) {
        int row = quadrant % NUM_COLUMNS;
        int column = quadrant / NUM_COLUMNS;
        return new Point2D.Double((row + .5) * HEIGHT_PER_QUADRANT,
                (column + .5) * HEIGHT_PER_QUADRANT);
    }

    static void getLines(List<Line2D> list) {
        for (int i = 0; i < NUM_COLUMNS; i++) {
            list.add(new Line2D.Double(0, i * HEIGHT_PER_QUADRANT, 100, i
                    * HEIGHT_PER_QUADRANT));
            list.add(new Line2D.Double(i * HEIGHT_PER_QUADRANT, 0, i
                    * HEIGHT_PER_QUADRANT, 100));
        }

    }

    public static void main(String[] args) {
        System.out.println(getQuadrant(0, 0) == 0);
        System.out.println(getQuadrant(24.999, 24.999) == 0);
        System.out.println(getQuadrant(0, 99) == 6);
        System.out.println(getQuadrant(50, 24) == 1);
        System.out.println(getCenterOfQuadrant(6));
    }
}