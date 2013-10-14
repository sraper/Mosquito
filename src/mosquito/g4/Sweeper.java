package mosquito.g4;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import mosquito.sim.MoveableLight;

import org.apache.log4j.Logger;

public class Sweeper {

	private static final int CONFIDENCE_AREA = 12;
	
	
    private static final Logger log = Logger.getLogger(G4Player.class); // for
                                                                        // logging
	private int[] counter;
	private boolean[] lasttimeup;
	private int[][] board;
	private int numsections;
	
	private boolean[] donesweep;
	private boolean[] donephaseone;
	private ArrayList<Point2D> leftmostpoint;
	private ArrayList<Point2D> rightmostpoint;
	
	public Sweeper(int numsections, int[][] board) {
		counter = new int[numsections];
		lasttimeup = new boolean[numsections];
		donesweep = new boolean[numsections];
		this.board = board;
		this.numsections = numsections;
		donephaseone = new boolean[numsections];
		findLeftmost();
		findRightmost();
	}
	
	// should enumerate
	public boolean doSweep(MoveableLight ml, int section) {
		double destx = leftmostpoint.get(section).getX();
		double desty = leftmostpoint.get(section).getY();
//		destx = destx > 100 ? 88 : destx;
//		desty = desty > 100 ? 88 : destx;
		if (!donephaseone[section]) {
//			log.trace("ml x: " + ml.getX() + " y: " + ml.getY() + " section: " + section + " ----- lmp x: " + destx + " y: " + desty);

			donephaseone[section] = moveToPoint(ml, destx, desty);
			return true;
		}
		
		
    	int mymove = justGo(section, (int)ml.getX(), (int)ml.getY());
    	switch (mymove){
    		case -2:
    			// done
    			// just go left when done for now for easy visualization
    			log.trace("i'm done");
    			ml.moveLeft();
    			return false;
    		case -1:
    			//not imp: gen error handling
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
	
	public int justGo(int section, int x, int y)  {
		if (section >= numsections) {
			return -1;
		}
		
		// if we see the end
		if (x + CONFIDENCE_AREA > rightmostpoint.get(section).getX()) {
//			log.trace("found end condition");
			counter[section] = 0;
			// "done" in the sense that we still have to go up or down one last time
			donesweep[section] = true;
		}
		
		int move = goUpDown(section, x, y);
		// done our final sweep, officially done the sweep
		if (move == -1 && donesweep[section]) {
//			log.trace("breaking out");
			donesweep[section] = false;
			return -2;
		// sweeping in upward or downward direction
		} else if (counter[section] % 12 == 0 && move != -1) {
			return move;
		// moving right
		} else {
			// end of board gtfo, don't think it ever goes here anymore?
			if(x + 1 == 100) {
				return -1;
			// on a diagonal
			} else if(x + 1 < 100 && board[x + 1][y] != section) {
				move = goUpDown(section, x, y);
				if (move == -1) {
					donesweep[section] = false;
					return -2;
				}
			// otherwise just go right
			} else {
				move = 2;
				// count to twelve steps to the right
				counter[section]++;
			}
			return move;
		}	
	}
	
	private int goUpDown(int section, int x, int y) {
		// if we were going up last time try to keep going that way
		if (lasttimeup[section]) {
			if (y - CONFIDENCE_AREA > 0 && board[x][y - CONFIDENCE_AREA] == section) {
//				log.trace("going up, x: " + x + ", y: " + y + ", section: " + section);
				return 1; //north
			// reached a perimeter, signal that we need to start heading right now
			} else {
				lasttimeup[section] = false;
				return -1;
			}
		} else {
			if (y + CONFIDENCE_AREA < 100 && board[x][y + CONFIDENCE_AREA] == section) {
//				log.trace("going down, x: " + x + ", y: " + y + ", section: " + section);
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
        
//        for (int i = 0; i < 2; i ++) {
//
//        	log.trace(leftmostpoint.toString());
//        	log.trace("section " + i + "leftmost point; x: " + leftmostpoint.get(i).getX() + ", y: " + leftmostpoint.get(i).getY());
//        }
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
        
//        for (int i = 0; i < 2; i ++) {
//        	log.trace(rightmostpoint.toString());
//        	log.trace("section " + i + "rightmost point; x: " + rightmostpoint.get(i).getX() + ", y: " + rightmostpoint.get(i).getY());
//        }
	}
	
	private boolean moveToPoint(MoveableLight inlight, double x, double y) {		
		double difx = x - inlight.getX();
		double dify = y - inlight.getY();
//		log.trace("delx : " + difx + ", dely: " + dify + " --  x: " + inlight.getX() + ", y: " + inlight.getY());
		double delx = difx / (Math.abs(difx) + Math.abs(dify));
		double dely = dify / (Math.abs(difx )+ Math.abs(dify));
		
//		log.trace("delx : " + delx + ", dely: " + dely + " --  x: " + inlight.getX() + ", y: " + inlight.getY());
		
		if(Math.abs(difx) + Math.abs(dify) <= 1) {
			inlight.moveTo(x, y);
			return true;
		} else {
			inlight.moveTo(inlight.getX() + delx, inlight.getY() + dely);
			return false;
		}
	}
	
	public ArrayList<Point2D> getStartingPoints(){
		return leftmostpoint;
	}
}
enum Direction {
	NORTH, EAST, SOUTH, WEST;
}