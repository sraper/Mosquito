package mosquito.g4;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.MoveableLight;

import org.apache.log4j.Logger;

public class SweepPlayer extends mosquito.sim.Player {
    private static final Logger log = Logger.getLogger(G4Player.class); // for
                                                                        // logging

    private int numLights;
    private Set<Light> lights;
    private PopulationCounter populationCounter;
    private Population collectorPopulation;
    private LightQuadrantTracker tracker;
    private G4LightCollectorTracker lightCollectorTracker;

    private Point2D.Double collectorLocation;
    
    private int board[][];
    private Sweeper s;

    public SweepPlayer() {
        this.lights = new HashSet<Light>();
        this.populationCounter = new PopulationCounter();


        
        initCollector();
        this.lightCollectorTracker = new G4LightCollectorTracker(collectorLocation);
        tracker = new LightQuadrantTracker();
    }

    @Override
    public String getName() {
        return "G4 Player";
    }

    /*
     * This is called when a new game starts. It is passed the set of lines that
     * comprise the different walls, as well as the maximum number of lights you
     * are allowed to use.
     * 
     * The return value is a set of lines that you would like to have drawn on
     * the screen. These lines don't actually affect gameplay, they're just
     * there so you can have some visual clue as to what's happening in the
     * simulation.
     */
    @Override
    public ArrayList<Line2D> startNewGame(Set<Line2D> walls, int numLights) {
        this.numLights = numLights;
        
        // set up test board
        board = new int [100][100];
        for (int i = 0; i < 100; i ++) {
        	for (int j = 0; j < 100; j++) {
        		// diagonal
        		if (i + j < 100) {
        			board[i][j] = 0;
        		} else {
        			board[i][j] = 1;
        		}
        		
        		// middle
//        		if (i < 50) {
//        			board[i][j] = 0;
//        		} else {
//        			board[i][j] = 1;
//        		}
        	}
        }
        s = new Sweeper(2, board);


		ArrayList<Line2D> lines = new ArrayList<Line2D>();
		Line2D line = new Line2D.Double(0, 100, 100, 0);
		lines.add(line);
		return lines;
    }

    /*
     * This is used to determine the initial placement of the lights. It is
     * called after startNewGame. The board tells you where the mosquitoes are:
     * board[x][y] tells you the number of mosquitoes at coordinate (x, y)
     */
    public Set<Light> getLights(int[][] board) {        
        for (int i = 0; i < numLights; i++) {
			MoveableLight l = new MoveableLight(50, 51, true);

        	log.trace("creating x " + l.getX() + " y " + l.getY() + " lightnum " + i);
			lights.add(l);
        }

        return lights;
    }

    private G4Light getNextLight(int i) {
        Population population = populationCounter.popTop();
        Point2D center = Quadrant.getCenterOfQuadrant(population.quadrant);

        // log.debug(String.format(
        // "Population is %d at quadrant %d. Light at <%f, %f>",
        // population.population, population.quadrant, center.getX(),
        // center.getY()));

        G4Light light = new G4Light(center.getX(), center.getY(), i, tracker);
        light.setCollectorPoint(this.collectorLocation);
        return light;
    }

    /*
     * This is called at the beginning of each step (before the mosquitoes have
     * moved) If your Set contains additional lights, an error will occur. Also,
     * if a light moves more than one space in any direction, an error will
     * occur. The board tells you where the mosquitoes are: board[x][y] tells
     * you the number of mosquitoes at coordinate (x, y)
     */
    public Set<Light> updateLights(int[][] board) {
        int cnt = 0;
        for (Light l : lights) {
        	MoveableLight ml = (MoveableLight) l;
        	log.trace("ml x " + ml.getX() + " y " + ml.getY() + " section " + cnt);
        	boolean isdonesweeping = s.doSweep(ml, cnt);
        	if(isdonesweeping) {
        		// go do something else
        	}
        	cnt++;
        }
        return lights;
    }

    public int getNextQuadrant() {
        int quadrant;
        do {
            Population top = populationCounter.popTop();
            if (top == null) {
                return 0;
            }
            quadrant = top.quadrant;
        } while (quadrant == collectorPopulation.quadrant
                || tracker.isQuadrantTaken(quadrant));
        return quadrant;
    }

    /*
     * Currently this is only called once (after getLights), so you cannot move
     * the Collector.
     */
    @Override
    public Collector getCollector() {
        return new Collector(collectorLocation.getX(), collectorLocation.getY());
    }

    private void initCollector() {
        // int quadrant = collectorPopulation.quadrant;
        // Point2D coordinate = Quadrant.getCenterOfQuadrant(quadrant);
        //
        // double x = coordinate.getX();
        // double y = coordinate.getY();
        //
        this.collectorLocation = Quadrant.getCenter();// new Point2D.Double(x, y);

        // log.debug(String.format(
        // "Most population at quadrant %d. Collector at <%f, %f>",
        // quadrant, x, y));
        // return new Collector(x + 1, y);
    }
}
