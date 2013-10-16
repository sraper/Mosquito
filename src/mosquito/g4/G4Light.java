package mosquito.g4;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;

import mosquito.sim.MoveableLight;

import org.apache.log4j.Logger;

public class G4Light extends MoveableLight {
    private static final double DELTA = 1;

    private static Logger log = Logger.getLogger(G4Light.class);
    
    private ArrayList<Point2D.Double> path;
    private int pathIndex = 0;

    // boolean isClosestToCollector;
    boolean reachedDestination;
    boolean hasDestination;
    int id;

    NextDestination nextDest;
    LightQuadrantTracker tracker;

    private double dest_x;
    private double dest_y;

    private Double collectorLocation;

    private boolean stayPut;
    
    public void printPath(){
    	log.trace("Starts at : ");
    	log.trace("x : " + this.getX() + " y : " + this.getY());
    	double prevx = this.getX();
    	double prevy = this.getY();
    	for (Point2D.Double p : path){
    		if (Math.sqrt(Math.pow(p.x-prevx, 2) + Math.pow(p.y-prevy,2)) > 1.0)
    			log.trace("WARNING, DISTANCE GREATER THAN 1");
    		log.trace("x : " + p.x + " y : " + p.y);
    		prevx = p.x;
    		prevy = p.y;
    	}
    }
    
    public void setPath(ArrayList<Point2D.Double> path){
    	this.path = path;
    	hasDestination = true;
    	reachedDestination = false;
    	pathIndex = 0;
    }
    
    public boolean destinationReached() {
    	if (path != null) {
    	double path_x = path.get(path.size()-1).x;
    	double path_y = path.get(path.size()-1).y;
    	boolean destReached = (this.getX() == path_x && this.getY() == path_y);
    	return destReached;
    	}
    	return true;
    }
    
    public Point2D.Double incrementPath(){
    	if (pathIndex < path.size())
    		return path.get(pathIndex++);
    	else
    		return path.get(path.size()-1);
    }
    
    public Point2D.Double getNextPoint(){
    	if (pathIndex < path.size())
    		return path.get(pathIndex);
    	else
    		return path.get(path.size()-1);
    }

    public G4Light(double x, double y, int id, LightQuadrantTracker tracker) {
        super(x, y, true);
        this.id = id;
        this.tracker = tracker;
        setDestination(x, y, NextDestination.Quadrant);
    }
    
    public G4Light(double x, double y) {
        super(x, y, true);
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
}

enum NextDestination {
    Collector, Quadrant;
}
