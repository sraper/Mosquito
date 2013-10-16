package mosquito.g4;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import mosquito.g4.utils.Utils;

import org.apache.log4j.Logger;

public class Sweeper {

	private static final int CONFIDENCE_AREA = 12;


	private static final Logger log = Logger.getLogger(VoronoiPlayer.class); // for
	// logging
	private int[] counter; // for counting steps right
	private boolean[] lasttimeup; // for continuing in same dir
	private int[][] board;
	private int numsections;

	private boolean[] donesweep; // to signal that we're doing one last updown check
	private boolean[] donephaseone; // to signal that we've arrived at the starting point
	private ArrayList<Point2D> leftmostpoint;
	private ArrayList<Point2D> rightmostpoint;
	private AStar star;

	public Sweeper(AStar star, int numsections, int[][] board) {
		counter = new int[numsections];
		lasttimeup = new boolean[numsections];
		donesweep = new boolean[numsections];
		this.star = star;
		this.board = board;
		this.numsections = numsections;
		donephaseone = new boolean[numsections];
		findLeftmost();
		findRightmost();
	}

	// should enumerate but lazy
	public boolean doSweep(G4Light ml, int section) {
		//move to start point
		if (!donephaseone[section]) {
			donephaseone[section] = moveToPoint(ml, leftmostpoint.get(section).getX(), leftmostpoint.get(section).getY());
			return true;
		}

		if (!ml.destinationReached() && ml.hasDestination){
			Point2D.Double np = ml.getNextPoint();
			Point2D mlPoint = new Point2D.Double(ml.getX(), ml.getY());

		/*	log.trace(np.distance(mlPoint));
			log.trace("currently at:" + ml.getX() + ", " + ml.getY());
			log.trace("moving to:" + np.x + ", " + np.y);
			log.trace("distance : " + (np.distance(mlPoint))); */ // Debugging distance error
			ml.moveTo(np.x, np.y);
			if (ml.getX() == np.x && ml.getY() == np.y)
				ml.incrementPath();
			return true;
		}
		else if (ml.hasDestination && ml.destinationReached())
			return true;
		else {
			int mymove = justGo(section, (int)ml.getX(), (int)ml.getY());
			switch (mymove){
			case -2:
				// done
				// just go left when done for now for easy visualization
				ArrayList<Point2D.Double> starPath = star.getPath(new Point2D.Double(ml.getX(), ml.getY()), new Point2D.Double(50,50));
				ml.setPath(starPath);
				log.trace("Printing path:");
				ml.printPath();
				return false;
			case -1:
				//not imp: gen error handling whatever
				return true;
			case 1:
				ml.moveUp();
				return true;
			case 2:
				ml.moveRight();
				return true;
			case 3:
				ml.moveDown();
				return true;
			default:
				return false;
			}
		}
	}

	public int justGo(int section, int x, int y)  {
		if (section >= numsections) {
			return -1;
		}

		// if we see the end
		if (x + CONFIDENCE_AREA > rightmostpoint.get(section).getX()) {
			//	log.trace("found end condition");
			counter[section] = 0;
			// "done" in the sense that we still have to go up or down one last time
			donesweep[section] = true;
		}


		int move = goUpDown(section, x, y, false);
		// done our final sweep, officially done the sweep
		if (move == -1 && donesweep[section]) {
			//		log.trace("breaking out");
			donesweep[section] = false;

			return -2;
			// sweeping in upward or downward direction
		} else if (counter[section] % 12 == 0 && move != -1) {
			return move;
			// moving right
		} else {
			// end of board gtfo, don't think it ever goes here anymore?
			if(x + 2 == 100) {
				return -1;
				// on a diagonal
			} else if(x + 2 < 100 && board[x + 2][y] != section) {
				move = goUpDown(section, x, y, true);
				if (move == -1) {
					lasttimeup[section] = !lasttimeup[section];
					return -1;
				}
				// otherwise just go right
			} else {
				move = 2;
				counter[section]++;
			}
			return move;
		}	
	}

	private int goUpDown(int section, int x, int y, boolean isondiagonal) {
		int padding = CONFIDENCE_AREA;
		if (isondiagonal) {
			padding = 2;
		}
		// if we were going up last time try to keep going that way
		if (lasttimeup[section]) {
			if (y - padding > 0 && board[x][y - padding] == section) {
				//			log.trace("going up, x: " + x + ", y: " + y + ", section: " + section);
				return 1; //north
				// reached a perimeter, signal that we need to start heading right now
			} else {
				lasttimeup[section] = false;
				return -1;
			}
		} else {
			if (y + padding < 100 && board[x][y + padding] == section) {
				//			log.trace("going down, x: " + x + ", y: " + y + ", section: " + section);
				return 3; // south
			} else {
				lasttimeup[section] = true;
				return -1;
			}
		}
	}	

	// find our start point
	private void findLeftmost() {
		leftmostpoint = new ArrayList<Point2D>();
		for (int i = 0; i < numsections; i++) {
			leftmostpoint.add(i, null);
		}

		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				if (leftmostpoint.get(board[i][j]) == null) {
					leftmostpoint.set(board[i][j], new Point(i, j));
				}
			}
		}

		// be warned this sucks and is confusing but it's trying to find an optimal starting point (and kinda succeeding..)
		for (int thissection = 0; thissection < numsections; thissection++) {
			double myx = leftmostpoint.get(thissection).getX();
			double myy = leftmostpoint.get(thissection).getY();
			boolean foundbetter = false;
			for (int i = -12; i < CONFIDENCE_AREA; i++) {
				for (int j = 0; j < CONFIDENCE_AREA; j++) {
					if (myx + i < 100 && myy + j < 100 && myx + i > 0 && myy + j > 0 && board[(int) (myx + i)][(int) (myy + j)] == thissection) {
						if((myx + i < 88 && myx + i > 12) && (myy + j < 88 && myy + j > 12)) {
							leftmostpoint.set(thissection, new Point( (int)(myx + i), (int) (myy + j)) );
							foundbetter = true;
						} else if(!foundbetter) {
							leftmostpoint.set(thissection, new Point( (int) (myx + i), (int)(myy + j)) );
						}
					}
				}
			}
		}

	}

	// find our end point
	private void findRightmost() {
		rightmostpoint = new ArrayList<Point2D>();
		for (int i = 0; i < numsections; i++) {
			rightmostpoint.add(i, new Point(0, 0));
		}
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				rightmostpoint.set(board[i][j], new Point(i, j));
			}
		}

	}

	public boolean moveToPoint(G4Light inlight, double x, double y) {	
		log.trace(inlight.getX() + " " + inlight.getY() + " " + x + " " + y);
		Point2D current = new Point2D.Double(inlight.getX(), inlight.getY());
		Point2D dest = new Point2D.Double(x, y);

		Point2D step = Utils.getNextStep(current, dest);

		inlight.moveTo(step.getX(), step.getY());

		log.trace(String.format("src %s dest %s step %s", current.toString(),
				dest.toString(), step.toString()));

		return (step.distance(dest) == 0);
	}



	public ArrayList<Point2D> getStartingPoints(){
		return leftmostpoint;
	}
}