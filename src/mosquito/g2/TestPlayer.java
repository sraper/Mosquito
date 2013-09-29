package mosquito.g2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.MoveableLight;



public class TestPlayer extends mosquito.sim.Player {

	private int numLights;
	private Point2D.Double lastLight;
	private Point2D.Double mycollector;
	private Logger log = Logger.getLogger(this.getClass()); // for logging
	
	@Override
	public String getName() {
		return "Random Player";
	}
	
	private Set<Light> lights;
	private Set<Line2D> walls;
	
	/*
	 * This is called when a new game starts. It is passed the set
	 * of lines that comprise the different walls, as well as the 
	 * maximum number of lights you are allowed to use.
	 * 
	 * The return value is a set of lines that you would like to have drawn on the screen.
	 * These lines don't actually affect gameplay, they're just there so you can have some
	 * visual clue as to what's happening in the simulation.
	 */
	@Override
	public ArrayList<Line2D> startNewGame(Set<Line2D> walls, int numLights) {
		this.numLights = numLights;
		this.walls = walls;

		ArrayList<Line2D> lines = new ArrayList<Line2D>();
		Line2D line = new Line2D.Double(30, 30, 80, 80);
//		lines.add(line);
		return lines;
	}


	/*
	 * This is used to determine the initial placement of the lights.
	 * It is called after startNewGame.
	 * The board tells you where the mosquitoes are: board[x][y] tells you the
	 * number of mosquitoes at coordinate (x, y)
	 */
	public Set<Light> getLights(int[][] board) {
		// Initially place the lights randomly, and put the collector next to the last light

		lights = new HashSet<Light>();
		Random r = new Random();
		for(int i = 0; i < numLights; i++) {
			if ((20 + i*60) % 100 < 20) {
				lastLight = new Point2D.Double((40 + i*60) % 100, 0);
			} else {
				lastLight = new Point2D.Double((20 + i*60) % 100, 0);
			}
			
			
			MoveableLight l = new MoveableLight(lastLight.getX(), lastLight.getY(), true);
			
			log.trace("Positioned a light at (" + lastLight.getX() + ", " + lastLight.getY() + ")");
			lights.add(l);
		}
		
		return lights;
	}
	
	/*
	 * This is called at the beginning of each step (before the mosquitoes have moved)
	 * If your Set contains additional lights, an error will occur. 
	 * Also, if a light moves more than one space in any direction, an error will occur.
	 * The board tells you where the mosquitoes are: board[x][y] tells you the
	 * number of mosquitoes at coordinate (x, y)
	 */
	public Set<Light> updateLights(int[][] board) {
		
		Random r = new Random();
		
		for(Light l : lights) {
			MoveableLight ml = (MoveableLight)l;
			
//			if (!findMosquito(ml, board)){
				if (ml.getY() < 100) {
					ml.moveDown();
				} else {
					moveToCollector(ml);
				}
//			}
		}
		
		return lights;
	}

	/*
	 * Currently this is only called once (after getLights), so you cannot
	 * move the Collector.
	 */
	@Override
	public Collector getCollector() {
		Collector c = new Collector(50, 100);
		mycollector = new Point2D.Double(50, 100);
		log.debug("Positioned a Collector at (" + c.getX() + ", " + c.getY() + ")");
		return c;
	}
	
	
	
	private boolean moveToCollector (MoveableLight inlight) {
		double collectx = mycollector.getX();
		double collecty = mycollector.getY();
		double inlightx = inlight.getX();
		double inlighty = inlight.getY();
		if (inlightx < collectx) {
			inlight.moveRight();
			return true;
		} else if (inlightx > collectx) {
			inlight.moveLeft();
			return true;
		}
		
		if (inlighty < collecty) {
			inlight.moveUp();
			return true;
		} else if (inlighty > collecty) {
			inlight.moveDown();
			return true;
		}
		return false;
	}
	
	// this doesn't work like whatsoever
	private boolean findMosquito (MoveableLight inlight, int[][] board) {
		for(int j = -5; j < 5; j++) {
			for(int i = (int)inlight.getY(); i <= 85; i++) {
				if (board[(int) inlight.getX() + j][i + 15] != 0) {
					inlight.moveLeft();
					return true;
				}
			}
		}
		return false;
	}
}
