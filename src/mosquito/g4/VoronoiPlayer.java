package mosquito.g4;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import mosquito.g4.voronoi.SectionLineDrawer;
import mosquito.g4.voronoi.Sections;
import mosquito.g4.voronoi.Voronoi;
import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.Player;

import org.apache.log4j.Logger;

public class VoronoiPlayer extends Player {

    private static final Logger log = Logger.getLogger(VoronoiPlayer.class); // for
                                                                        // logging

    private int numLights;
    private Set<Light> lights;
    private Voronoi v;
    private Sweeper s;
    private Sections sections;
    private AStar star;

    private boolean[] issweeping;

    public VoronoiPlayer() {
        lights = new HashSet<Light>();
    }

    @Override
    public String getName() {
        return "Voronoi Player";
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
        this.v = new Voronoi(numLights, 100, walls);
        this.star = new AStar(walls);
        v.doVoronoi();

        issweeping = new boolean[v.getNumSections()];
        if (numLights == v.getNumSections()) {
            Arrays.fill(issweeping, true);
        }

        sections = v.getSections();
        s = new Sweeper(star, v.getNumSections(), sections.getSectionBoard());

        return new SectionLineDrawer(v.getSectionIdBoard()).createLines();
    }

    /*
     * This is used to determine the initial placement of the lights. It is
     * called after startNewGame. The board tells you where the mosquitoes are:
     * board[x][y] tells you the number of mosquitoes at coordinate (x, y)
     */
    public Set<Light> getLights(int[][] board) {

        for (int i = 0; i < v.getNumSections() && lights.size() < numLights; i++) {
            lights.add(new G4Light(s.getStartingPoints().get(i).getX(), s
                    .getStartingPoints().get(i).getY(), i));
        }

        return lights;
    }

    /*
     * This is called at the beginning of each step (before the mosquitoes have
     * moved) If your Set contains additional lights, an error will occur. Also,
     * if a light moves more than one space in any direction, an error will
     * occur. The board tells you where the mosquitoes are: board[x][y] tells
     * you the number of mosquitoes at coordinate (x, y)
     */
    public Set<Light> updateLights(int[][] board) {
    	boolean isAtCollector = false;
        for (Light l : lights) {
            G4Light ml = (G4Light) l;
//            if (!isAtCollector && ml.getLocation().equals(new Point2D.Double(50, 50))) {
//            	isAtCollector = true;
//            	continue;
//            }
            
            boolean sweeping = s.doSweep(ml, sections.getSection((int) ml.getX(), (int) ml.getY()), board);
            if(!sweeping) {
            	ml.moveLeft();
            }
        }
        return lights;
    }

    public int findNextSection(int[][] mosquitoboard) {
    	int max = 0;
    	int bestsec = -1;
    	for(int i = 0; i < v.getNumSections(); i++) {
    		int count = 0;
    		for(int j = 0; j < 100; j++) {
    			for(int k = 0; k < 100; k++) {
    				if(sections.getSection(j, k) == i) {
    					count += mosquitoboard[j][k];
    				}
    			}
    		}
    		if (count > max) {
    			count = max;
    			bestsec = i;
    		}	
    	}
    	return bestsec;
    }
    
    
    /*
     * Currently this is only called once (after getLights), so you cannot move
     * the Collector.
     */
    @Override
    public Collector getCollector() {
        return new Collector(50, 50);
    }
}
