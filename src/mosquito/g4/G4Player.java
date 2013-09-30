package mosquito.g4;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mosquito.sim.Collector;
import mosquito.sim.Light;

import org.apache.log4j.Logger;

public class G4Player extends mosquito.sim.Player {
    private static final Logger log = Logger.getLogger(G4Player.class); // for
                                                                        // logging

    private int numLights;
    private Set<Light> lights;
    private PopulationCounter populationCounter;
    private Population collectorPopulation;
    private LightQuadrantTracker tracker;

    // NOT NEEDED, so far private int time;
    private Double collector;

    public G4Player() {
        this.lights = new HashSet<Light>();
        this.populationCounter = new PopulationCounter();
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

        ArrayList<Line2D> lines = new ArrayList<Line2D>();
        Quadrant.getLines(lines);
        return lines;
    }

    /*
     * This is used to determine the initial placement of the lights. It is
     * called after startNewGame. The board tells you where the mosquitoes are:
     * board[x][y] tells you the number of mosquitoes at coordinate (x, y)
     */
    public Set<Light> getLights(int[][] board) {
        populationCounter.updatePopulation(board);

        collectorPopulation = populationCounter.peekTop();

        for (int i = 0; i < numLights; i++) {
            G4Light nextLight = getNextLight(i);
            nextLight.isClosestToCollector = i == 0 && numLights > 1;
            lights.add(nextLight);
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

        G4Light light = new G4Light(center.getX(), center.getY(), true, i,
                tracker);
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
        populationCounter.updatePopulation(board);

        for (Light l : lights) {
            G4Light gl = (G4Light) l;

            if (gl.reachedDestination) {

                if (gl.nextDest == NextDestination.Collector) {

                    gl.setDestination(collector.getX(), collector.getY(),
                            NextDestination.Collector);

                } else if (!gl.isClosestToCollector
                        && gl.nextDest == NextDestination.Quadrant) {

                    int quadrant = getNextQuadrant();

                    Point2D dest = Quadrant.getCenterOfQuadrant(quadrant);
                    gl.setDestination(dest.getX(), dest.getY(),
                            NextDestination.Quadrant);
                }
            }

            gl.step();
        }
        return lights;
    }

    public int getNextQuadrant() {
        int quadrant;
        do {
            quadrant = populationCounter.popTop().quadrant;
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
        int quadrant = collectorPopulation.quadrant;
        Point2D coordinate = Quadrant.getCenterOfQuadrant(quadrant);

        double x = coordinate.getX();
        double y = coordinate.getY();

        this.collector = new Point2D.Double(x, y);

        // log.debug(String.format(
        // "Most population at quadrant %d. Collector at <%f, %f>",
        // quadrant, x, y));
        return new Collector(x + 1, y);
    }
}
