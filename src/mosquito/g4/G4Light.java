package mosquito.g4;

import java.awt.geom.Point2D.Double;

import mosquito.sim.MoveableLight;

import org.apache.log4j.Logger;

public class G4Light extends MoveableLight {
    private static Logger log = Logger.getLogger(G4Light.class);

    boolean isClosestToCollector;
    boolean reachedDestination;
    int id;

    NextDestination nextDest;
    LightQuadrantTracker tracker;

    private double dest_x;
    private double dest_y;

    private Double collectorLocation;

    public G4Light(double x, double y, int id, LightQuadrantTracker tracker) {
        super(x, y, true);
        this.id = id;
        this.tracker = tracker;
        setDestination(x, y, NextDestination.Quadrant);
    }

    public void setDestination(double x, double y, NextDestination dest) {
        // log.debug("Set dest called with " + x + " " + y + " " + dest);
        this.dest_x = x;
        this.dest_y = y;
        this.nextDest = dest;

        reachedDestination = false;

        tracker.setDestination(getX(), getY(), dest_x, dest_y);
    }

    public void step() {
        if (isDestSet()) {
            double prev_x = x;
            double prev_y = y;
            // log.debug(String
            // .format("Stepping towards %f, %f. Currently at %f, %f. Delta %f, %f",
            // dest_x, dest_y, x, y, x - dest_x, y - dest_y));

            if (x - dest_x > 1) {
                x--;
            } else if (x - dest_x < -1) {
                x++;
            } else if (y - dest_y > 1) {
                y--;
            } else if (y - dest_y < -1) {
                y++;
            } else {
                resetDest();
                reachedDestination = true;
                flipNextDestination();
            }

            tracker.step(prev_x, prev_y, x, y);
        }
    }

    public void resetDest() {
        dest_x = -1;
        dest_y = -1;
    }

    public boolean isDestSet() {
        return dest_x != -1 && dest_y != -1;
    }

    private void flipNextDestination() {
        nextDest = nextDest == NextDestination.Collector ? NextDestination.Quadrant
                : NextDestination.Collector;

        if (!isClosestToCollector) {
            if (nextDest == NextDestination.Collector) {
                turnOn();
            } else {
                turnOff();
            }
        }
    }

    public void setCollectorPoint(Double location) {
        this.collectorLocation = location;

    }
}

enum NextDestination {
    Collector, Quadrant;
}
