package mosquito.g4.utils;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/*
 * (C) 2004 - Geotechnical Software Services
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA  02111-1307, USA.
 */

/**
 * Collection of geometry utility methods. All methods are static.
 * 
 * @author <a href="mailto:info@geosoft.no">GeoSoft</a>
 */
public final class GeometryUtils {
    /**
     * /** Check if two double precision numbers are "equal", i.e. close enough
     * to a given limit.
     * 
     * @param a
     *            First number to check
     * @param b
     *            Second number to check
     * @param limit
     *            The definition of "equal".
     * @return True if the twho numbers are "equal", false otherwise
     */
    private static boolean equals(double a, double b, double limit) {
        return Math.abs(a - b) < limit;
    }

    /**
     * Check if two double precision numbers are "equal", i.e. close enough to a
     * prespecified limit.
     * 
     * @param a
     *            First number to check
     * @param b
     *            Second number to check
     * @return True if the twho numbers are "equal", false otherwise
     */
    public static boolean equals(double a, double b) {
        return equals(a, b, 1.0e-5);
    }

    /**
     * Return smallest of four numbers.
     * 
     * @param a
     *            First number to find smallest among.
     * @param b
     *            Second number to find smallest among.
     * @param c
     *            Third number to find smallest among.
     * @param d
     *            Fourth number to find smallest among.
     * @return Smallest of a, b, c and d.
     */
    private static double min(double a, double b, double c, double d) {
        return Math.min(Math.min(a, b), Math.min(c, d));
    }

    /**
     * Return largest of four numbers.
     * 
     * @param a
     *            First number to find largest among.
     * @param b
     *            Second number to find largest among.
     * @param c
     *            Third number to find largest among.
     * @param d
     *            Fourth number to find largest among.
     * @return Largest of a, b, c and d.
     */
    private static double max(double a, double b, double c, double d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    /**
     * Compute the length of the line from (x0,y0) to (x1,y1)
     * 
     * @param x0
     *            , y0 First line end point.
     * @param x1
     *            , y1 Second line end point.
     * @return Length of line from (x0,y0) to (x1,y1).
     */
    private static double length(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;

        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Compute the intersection between two line segments, or two lines of
     * infinite length.
     * 
     * @param x0
     *            X coordinate first end point first line segment.
     * @param y0
     *            Y coordinate first end point first line segment.
     * @param x1
     *            X coordinate second end point first line segment.
     * @param y1
     *            Y coordinate second end point first line segment.
     * @param x2
     *            X coordinate first end point second line segment.
     * @param y2
     *            Y coordinate first end point second line segment.
     * @param x3
     *            X coordinate second end point second line segment.
     * @param y3
     *            Y coordinate second end point second line segment.
     * @param intersection
     *            [2] Preallocated by caller to double[2]
     * @return -1 if lines are parallel (x,y unset), -2 if lines are parallel
     *         and overlapping (x, y center) 0 if intesrection outside segments
     *         (x,y set) +1 if segments intersect (x,y set)
     */
    public static int findLineSegmentIntersection(double x0, double y0,
            double x1, double y1, double x2, double y2, double x3, double y3,
            double[] intersection) {
        // TODO: Make limit depend on input domain
        final double LIMIT = 1e-5;
        final double INFINITY = 1e10;

        double x, y;

        //
        // Convert the lines to the form y = ax + b
        //

        // Slope of the two lines
        double a0 = GeometryUtils.equals(x0, x1, LIMIT) ? INFINITY : (y0 - y1)
                / (x0 - x1);
        double a1 = GeometryUtils.equals(x2, x3, LIMIT) ? INFINITY : (y2 - y3)
                / (x2 - x3);

        double b0 = y0 - a0 * x0;
        double b1 = y2 - a1 * x2;

        // Check if lines are parallel
        if (GeometryUtils.equals(a0, a1)) {
            if (!GeometryUtils.equals(b0, b1))
                return -1; // Parallell non-overlapping

            else {
                if (GeometryUtils.equals(x0, x1)) {
                    if (Math.min(y0, y1) < Math.max(y2, y3)
                            || Math.max(y0, y1) > Math.min(y2, y3)) {
                        double twoMiddle = y0 + y1 + y2 + y3
                                - GeometryUtils.min(y0, y1, y2, y3)
                                - GeometryUtils.max(y0, y1, y2, y3);
                        y = (twoMiddle) / 2.0;
                        x = (y - b0) / a0;
                    } else
                        return -1; // Parallell non-overlapping
                } else {
                    if (Math.min(x0, x1) < Math.max(x2, x3)
                            || Math.max(x0, x1) > Math.min(x2, x3)) {
                        double twoMiddle = x0 + x1 + x2 + x3
                                - GeometryUtils.min(x0, x1, x2, x3)
                                - GeometryUtils.max(x0, x1, x2, x3);
                        x = (twoMiddle) / 2.0;
                        y = a0 * x + b0;
                    } else
                        return -1;
                }

                intersection[0] = x;
                intersection[1] = y;
                return -2;
            }
        }

        // Find correct intersection point
        if (GeometryUtils.equals(a0, INFINITY)) {
            x = x0;
            y = a1 * x + b1;
        } else if (GeometryUtils.equals(a1, INFINITY)) {
            x = x2;
            y = a0 * x + b0;
        } else {
            x = -(b0 - b1) / (a0 - a1);
            y = a0 * x + b0;
        }

        intersection[0] = x;
        intersection[1] = y;

        // Then check if intersection is within line segments
        double distanceFrom1;
        if (GeometryUtils.equals(x0, x1)) {
            if (y0 < y1)
                distanceFrom1 = y < y0 ? GeometryUtils.length(x, y, x0, y0)
                        : y > y1 ? GeometryUtils.length(x, y, x1, y1) : 0.0;
            else
                distanceFrom1 = y < y1 ? GeometryUtils.length(x, y, x1, y1)
                        : y > y0 ? GeometryUtils.length(x, y, x0, y0) : 0.0;
        } else {
            if (x0 < x1)
                distanceFrom1 = x < x0 ? GeometryUtils.length(x, y, x0, y0)
                        : x > x1 ? GeometryUtils.length(x, y, x1, y1) : 0.0;
            else
                distanceFrom1 = x < x1 ? GeometryUtils.length(x, y, x1, y1)
                        : x > x0 ? GeometryUtils.length(x, y, x0, y0) : 0.0;
        }

        double distanceFrom2;
        if (GeometryUtils.equals(x2, x3)) {
            if (y2 < y3)
                distanceFrom2 = y < y2 ? GeometryUtils.length(x, y, x2, y2)
                        : y > y3 ? GeometryUtils.length(x, y, x3, y3) : 0.0;
            else
                distanceFrom2 = y < y3 ? GeometryUtils.length(x, y, x3, y3)
                        : y > y2 ? GeometryUtils.length(x, y, x2, y2) : 0.0;
        } else {
            if (x2 < x3)
                distanceFrom2 = x < x2 ? GeometryUtils.length(x, y, x2, y2)
                        : x > x3 ? GeometryUtils.length(x, y, x3, y3) : 0.0;
            else
                distanceFrom2 = x < x3 ? GeometryUtils.length(x, y, x3, y3)
                        : x > x2 ? GeometryUtils.length(x, y, x2, y2) : 0.0;
        }

        return GeometryUtils.equals(distanceFrom1, 0.0)
                && GeometryUtils.equals(distanceFrom2, 0.0) ? 1 : 0;
    }

    public static boolean findLineSegmentIntersection(Line2D wall,
            Line2D wall2, Point2D[] intersect) {
        double[] intersection = new double[2];

        int ret = findLineSegmentIntersection(wall.getX1(), wall.getY1(),
                wall.getX2(), wall.getY2(), wall2.getX1(), wall2.getY1(),
                wall2.getX2(), wall2.getY2(), intersection);

        intersect[0] = new Point2D.Double(intersection[0], intersection[1]);

        return ret == 1;
    }
}