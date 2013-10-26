package mosquito.g4;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.LinkedList;

import mosquito.sim.MoveableLight;

import org.apache.log4j.Logger;

public class G4Light extends MoveableLight {
    private static final double DELTA = 1;

    private static Logger log = Logger.getLogger(G4Light.class);

    private ArrayList<Point2D.Double> path;
    private int pathIndex = 0;

    private int waitturns = 0;

    // boolean isClosestToCollector;
    boolean reachedDestination;
    boolean hasDestination;
    public int id;

    private boolean isDispatched;
    public int dispatchedSection;

    NextDestination nextDest;

    private double dest_x;
    private double dest_y;

    private int end_wait = 3;

    private Double collectorLocation;

    private boolean stayPut;

    private LinkedList<Point2D> pastPoints;

    public Point2D getDestination() {
        return new Point2D.Double(dest_x, dest_y);
    }

    public void printPath() {
  //      log.trace("Starts at : ");
   //     log.trace("x : " + this.getX() + " y : " + this.getY());
        double prevx = this.getX();
        double prevy = this.getY();
        for (Point2D.Double p : path) {
    //        if (Math.sqrt(Math.pow(p.x - prevx, 2) + Math.pow(p.y - prevy, 2)) > 1.0)
 //               log.trace("WARNING, DISTANCE GREATER THAN 1");
  //          log.trace("x : " + p.x + " y : " + p.y);
            prevx = p.x;
            prevy = p.y;
        }
    }

    public void setPath(ArrayList<Point2D.Double> path) {
        this.path = path;
        hasDestination = true;
        reachedDestination = false;
        pathIndex = 0;
    }

    public boolean hunt() {
        if (end_wait > 0) {
            end_wait--;
            return true;
        } else {

        }
        return true;
    }

    public boolean destinationReached() {
        if (path != null && !path.isEmpty()) {
            double path_x = path.get(path.size() - 1).x;
            double path_y = path.get(path.size() - 1).y;
            boolean destReached = (this.getX() == path_x && this.getY() == path_y);
            return destReached;
        }
        return true;
    }

    public Point2D.Double incrementPath() {
        if (pathIndex < path.size())
            return path.get(pathIndex++);
        else
            return path.get(path.size() - 1);
    }

    public Point2D.Double getNextPoint() {
        if (pathIndex < path.size())
            return path.get(pathIndex);
        else
            return path.get(path.size() - 1);
    }

    public G4Light(double x, double y, int id) {
        super(x, y, true);
        this.id = id;
        isDispatched = true;
        this.dispatchedSection = -1;
        this.pastPoints = new LinkedList<Point2D>();
    }

    public boolean isStuck() {
    //    log.trace(pastPoints.toString());
        if (pastPoints.size() <= 50)
            return false;

        Point2D lastPoint = pastPoints.peek();
        for (Point2D p : pastPoints) {
            if (!p.equals(lastPoint)) {
                return false;
            }
        }
        return true;
    }

    public void clearPastPoints() {
        this.pastPoints = new LinkedList<Point2D>();
    }

    public void addPoint(Point2D p) {
        if (pastPoints.size() > 50)
            pastPoints.pop();

        pastPoints.add(p);
    }

    public void setDestination(double x, double y) {
        // log.debug("Set dest called with " + x + " " + y + " " + dest);
        this.dest_x = x;
        this.dest_y = y;
        // this.nextDest = dest;

        reachedDestination = false;
        hasDestination = true;

        // tracker.setDestination(getX(), getY(), dest_x, dest_y);
    }

    public void step() {
        if (isDestSet() && !stayPut) {
            double prev_x = x;
            double prev_y = y;
            // log.debug(String
            // .format("Stepping towards %f, %f. Currently at %f, %f. Delta %f, %f",
            // dest_x, dest_y, x, y, x - dest_x, y - dest_y));

            if (x - dest_x > 1) {
                x -= DELTA;
            } else if (x - dest_x < -1) {
                x += DELTA;
            } else if (y - dest_y > 1) {
                y -= DELTA;
            } else if (y - dest_y < -1) {
                y += DELTA;
            } else {
                resetDest();
                reachedDestination = true;
                flipNextDestination();
            }
        }
    }

    public void resetDest() {
        dest_x = -1;
        dest_y = -1;
    }

    public boolean isDestSet() {
        return dest_x != -1 && dest_y != -1;
    }

    public void wait(int numturns) {
        waitturns = numturns;
    }

    public boolean waiting() {
        if (waitturns > 0) {
            waitturns--;
            return true;
        }
        return false;
    }

    private void flipNextDestination() {
        nextDest = nextDest == NextDestination.Collector ? NextDestination.Quadrant
                : NextDestination.Collector;

        // if (!isClosestToCollector) {
        if (nextDest == NextDestination.Collector) {
            turnOn();
        } else {
            turnOff();
        }
        // }
    }

    public void setCollectorPoint(Double location) {
        this.collectorLocation = location;

    }

    public void stayPut(boolean stayPut) {
        this.stayPut = stayPut;
    }

    public boolean isDispatched() {
        return isDispatched;
    }

    public void setDispatched(boolean isDispatched) {
        this.isDispatched = isDispatched;
        // if (isDispatched) turnOff();
        // else turnOn();
    }
}

enum NextDestination {
    Collector, Quadrant;
}
