package mosquito.g4.utils;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.PrintStream;

public class Utils {

    public static boolean hasStraightPath(Point2D p1, Point2D p2,
            Iterable<Line2D> walls) {
        Line2D.Double path = new Line2D.Double(p1, p2);

        for (Line2D wall : walls) {
            if (path.intersectsLine(wall)) {
                return false;
            }
        }

        return true;
    }

    public static Point2D getCenter(Line2D l) {
        return new Point2D.Double(.5 * (l.getX1() + l.getX2()),
                .5 * (l.getY1() + l.getY2()));
    }

    public static Line2D getPerpendicularLine(Line2D l, double lineDistance) {
        double x1 = l.getX1(), x2 = l.getX2(), y1 = l.getY1(), y2 = l.getY2();

        double dx = x1 - x2;
        double dy = y1 - y2;
        double dist = Math.sqrt(dx * dx + dy * dy);
        dx /= dist;
        dy /= dist;

        double centerX = (x1 + x2) / 2;
        double centerY = (y1 + y2) / 2;

        double x3 = centerX + (lineDistance / 2) * dy;
        double y3 = centerY - (lineDistance / 2) * dx;
        double x4 = centerX - (lineDistance / 2) * dy;
        double y4 = centerY + (lineDistance / 2) * dx;

        return new Line2D.Double(x3, y3, x4, y4);
    }

    public static void print(PrintStream stream, int[][] array) {
        // for (int i = array.length - 1; i >= 0; i--) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                stream.print(String.format("%d ", array[j][i]));
            }
            stream.println();
        }
    }
}
