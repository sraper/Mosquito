package mosquito.g4;

import org.apache.log4j.Logger;

public class LightQuadrantTracker {

    private static final Logger log = Logger
            .getLogger(LightQuadrantTracker.class);
    private boolean[] quadrantsTaken;
    private boolean[] isDestinationSet;

    public LightQuadrantTracker() {
        this.quadrantsTaken = new boolean[Quadrant.NUM_QUADRANTS];
        this.isDestinationSet = new boolean[Quadrant.NUM_QUADRANTS];
    }

    public boolean isQuadrantTaken(int quadrant) {
        return quadrantsTaken[quadrant] || isDestinationSet[quadrant];
    }

    public void setDestination(double prev_x, double prev_y, double dest_x,
            double dest_y) {
        isDestinationSet[Quadrant.getQuadrant(prev_x, prev_y)] = false;
        isDestinationSet[Quadrant.getQuadrant(dest_x, dest_y)] = true;
    }

    public void step(double prev_x, double prev_y, double next_x, double next_y) {
        int prev_quadrant = Quadrant.getQuadrant(prev_x, prev_y);
        int next_quadrant = Quadrant.getQuadrant(next_x, next_y);

        setNotTaken(prev_quadrant);
        setTaken(next_quadrant);
    }

    private void setNotTaken(int quadrant) {
        quadrantsTaken[quadrant] = false;
    }

    private void setTaken(int quadrant) {
        quadrantsTaken[quadrant] = true;

    }
}