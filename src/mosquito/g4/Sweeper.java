package mosquito.g4;

import java.util.List;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import mosquito.g4.utils.Utils;
import mosquito.g4.voronoi.Section;
import mosquito.g4.voronoi.Sections;
import mosquito.g4.voronoi.Voronoi;

import org.apache.log4j.Logger;

public class Sweeper {

	private static final int CONFIDENCE_AREA = 12;

	private static final Logger log = Logger.getLogger(VoronoiPlayer.class); // for
	// logging
	private int[] counter; // for counting steps right
	private boolean[] lasttimeup; // for continuing in same dir
	private int[][] board;
	private int numsections;
	
    private Set<Line2D> walls;
	
	private Point2D collectorpos;

	private boolean[] donesweep; // to signal that we're doing one last updown
									// check
	private boolean[] donephaseone; // to signal that we've arrived at the
									// starting point
	
	private boolean[] isondiagonal;

	private G4Light[] claimed;
	private ArrayList<Point2D> leftmostpoint;

	private ArrayList<Point2D> rightmostpoint;
	private AStar star;
	private Sections s;
	private Voronoi v;

	public Sweeper(AStar star, int numsections, int[][] board, Sections sections, Voronoi v, Set<Line2D> walls) {
		this.v = v;
		this.walls = walls;
		s = sections;
		counter = new int[numsections];
		lasttimeup = new boolean[numsections];
		donesweep = new boolean[numsections];
		isondiagonal = new boolean[numsections];
		claimed = new G4Light[numsections];
		this.star = star;
		this.board = board;
		this.numsections = numsections;
		donephaseone = new boolean[numsections];
		findLeftmost();
		findRightmost();
	}

	// should enumerate but lazy
	public boolean doSweep(G4Light ml, int section, int[][] mosquitoboard) {
		if (ml.isStuck() && ml.getX() != collectorpos.getX() && ml.getY() != collectorpos.getY()) {
			log.trace("what");
			ml.clearPastPoints();
			ml.setDispatched(false);
			section = findUnclaimedSection(ml);
			if (section == -1) {
				
				ArrayList<Point2D.Double> starPath = star.getPath(
						new Point2D.Double(ml.getX(), ml.getY()),
						new Point2D.Double(collectorpos.getX(), collectorpos.getY()));
				ml.setPath(starPath);
			}
			// hmm not sure about this
//			if (section != ml.dispatchedSection) {
//				claimed[ml.dispatchedSection] = null;
//			}
			
			
//			ml.setDispatched(false);
//			ArrayList<Point2D.Double> starPath = star.getPath(
//					new Point2D.Double(ml.getX(), ml.getY()),
//					new Point2D.Double(50, 50));
//			ml.setPath(starPath);
//			section = -1;
		}
		
		
		log.trace(Utils.toString(claimed));
		boolean done = ml.hasDestination && ml.destinationReached();
		if (section != -1 && claimed[section] == null) {
			claimed[section] = ml;
			if (ml.isDispatched()) {
			log.trace("claiming " + section + " unclaiming " + ml.dispatchedSection);
			if (ml.dispatchedSection != -1) claimed[ml.dispatchedSection] = null;
			ml.dispatchedSection = section;
			}
		}
		if (done && !ml.isDispatched()) {
			return true;
//			return ml.hunt();
		}
		if (ml.isDispatched())
			section = ml.dispatchedSection;
		// if (ml.waiting()) {
		// return true;
		// }
		// move to start point

		if (section != -1 && !donephaseone[section] && claimed[section] == ml && !abortEarly(mosquitoboard, section,(int) ml.getY(),(int) ml.getY())) {
			Point2D destination = ml.getDestination();
			Point2D location = ml.getLocation();
			double sectionX = leftmostpoint.get(section).getX();
			double sectionY = leftmostpoint.get(section).getY();
			if (location.getX() != sectionX || location.getY() != sectionY) {
				if (!(destination.getX() == sectionX && destination.getY() == sectionY)) {
					ArrayList<Point2D.Double> starPath = star.getPath(
							new Point2D.Double(ml.getX(), ml.getY()),
							new Point2D.Double(sectionX, sectionY));
					ml.setPath(starPath);
					ml.setDestination(sectionX, sectionY);
				}

				Point2D.Double np = ml.getNextPoint();
				ml.moveTo(np.x, np.y);
				if (ml.getX() == np.x && ml.getY() == np.y)
					ml.incrementPath();

				// donephaseone[section] = moveToPoint(ml,
				// leftmostpoint.get(section).getX(),
				// leftmostpoint.get(section).getY());

				return true;
			} else {
				donephaseone[section] = true;
				ml.hasDestination = false;
			}
		}

		if (!ml.destinationReached() && ml.hasDestination) {
			Point2D.Double np = ml.getNextPoint();
			Point2D mlPoint = new Point2D.Double(ml.getX(), ml.getY());

			/*
			 * log.trace(np.distance(mlPoint)); log.trace("currently at:" +
			 * ml.getX() + ", " + ml.getY()); log.trace("moving to:" + np.x +
			 * ", " + np.y); log.trace("distance : " + (np.distance(mlPoint)));
			 */// Debugging distance error
			ml.moveTo(np.x, np.y);
			if (ml.getX() == np.x && ml.getY() == np.y)
				ml.incrementPath();
			return true;
		} else {
			int mymove = justGo(section, (int) ml.getX(), (int) ml.getY(), mosquitoboard);
			switch (mymove) {
			case -2:
				// done sweepin or at collector
				// done
				// just go left when done for now for easy visualization
				ml.setDispatched(false);
				section = findUnclaimedSection(ml);
				if (section == -1) {
					
					ArrayList<Point2D.Double> starPath = star.getPath(
							new Point2D.Double(ml.getX(), ml.getY()),
							new Point2D.Double(collectorpos.getX(), collectorpos.getY()));
					ml.setPath(starPath);
				}
				// log.trace("Printing path:");
				// ml.printPath();
				return true;
			case -1:
				// not imp: gen error handling whatever
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

	private int findUnclaimedSection(G4Light ml) {
		int section = -1;
		for (int i = 0; i < claimed.length; i++) {
			if (claimed[i] == null) {
				claimed[i] = ml;
				section = i;

				ml.setDispatched(true);
				ml.dispatchedSection = i;
				break;
			}
		}
		return section;
	}

	public int justGo(int section, int x, int y, int[][] mboard) {
		if (abortEarly(mboard, section, x, y)) {
			return -2;
		}
		
		
		
		if (section >= numsections) {
			return -1;
		}

		// if we see the end
		if (x + CONFIDENCE_AREA > rightmostpoint.get(section).getX()) {
			// log.trace("found end condition");
			counter[section] = 0;
			// "done" in the sense that we still have to go up or down one last
			// time
			donesweep[section] = true;
		}

		int move = goUpDown(section, x, y, false);
		// done our final sweep, officially done the sweep
		if (move == -1 && donesweep[section]) {
			// log.trace("breaking out");
			donesweep[section] = false;

			return -2;
			// sweeping in upward or downward direction
		} else if (counter[section] % 12 == 0 && move != -1) {
	//		isondiagonal[section] = true;
			return move;
			// moving right
		} else {
	//		isondiagonal[section] = true;
			// end of board gtfo, don't think it ever goes here anymore?
			if (x + 2 == 100) {
				return -1;
				// on a diagonal
			} else if (x + 2 < 100 && board[x + 2][y] != section) {
				log.trace("lasttimeup[" + section + "]: " + lasttimeup[section]);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < lasttimeup.length; i++) {
					sb.append(lasttimeup[i] + ", ");
				}
				log.trace(sb);
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
			if(counter[section] % 12 == 0) {
				lasttimeup[section] = !lasttimeup[section];
			}
			return move;
		}
	}

	private int goUpDown(int section, int x, int y, boolean isondiag) {
		int padding = isondiag ? 2 : 12;
//		if (isondiagonal) {
//			padding = 2;
//		}
		// if we were going up last time try to keep going that way
		if (lasttimeup[section]) {
			if (y - padding > 0 && board[x][y - padding] == section) {
				// log.trace("going up, x: " + x + ", y: " + y + ", section: " +
				// section);
				return 1; // north
				// reached a perimeter, signal that we need to start heading
				// right now
			} else {
		//		if (!isondiagonal[section]) {
		//			lasttimeup[section] = false;
		//		}
				return -1;
			}
		} else {
			if (y + padding < 100 && board[x][y + padding] == section) {
				// log.trace("going down, x: " + x + ", y: " + y + ", section: "
				// + section);
				return 3; // south
			} else {
		//		if (!isondiagonal[section]) {
		//			lasttimeup[section] = true;
		//		}
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

		// be warned this sucks and is confusing but it's trying to find an
		// optimal starting point (and kinda succeeding..)
		for (int thissection = 0; thissection < numsections; thissection++) {
			double myx = leftmostpoint.get(thissection).getX();
			double myy = leftmostpoint.get(thissection).getY();
			boolean foundbetter = false;
			for (int i = 0; i < CONFIDENCE_AREA; i++) {
				for (int j = -12; j < CONFIDENCE_AREA; j++) {
					if (myx + i < 100
							&& myy + j < 100
							&& myx + i > 0
							&& myy + j > 0
							&& board[(int) (myx + i)][(int) (myy + j)] == thissection && !intersectsWall ((int)myx + i, (int)myy + j)) {
						if ((myx + i <= 88 && myx + i >= 12)
								&& (myy + j <= 88 && myy + j >= 12)) {
							leftmostpoint.set(thissection, new Point(
									(int) (myx + i), (int) (myy + j)));
							foundbetter = true;
						} else if (!foundbetter) {
							leftmostpoint.set(thissection, new Point(
									(int) (myx + i), (int) (myy + j)));
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
				if (!intersectsWall((int)i, (int)j)) {
					rightmostpoint.set(board[i][j], new Point(i, j));
				}
			}
		}

	}

	private boolean abortEarly(int[][] mboard, int section, int x, int y) {
		/*
		 * for every point in the section
		 * if there's a mosquito
		 * return false
		 * }}
		 * return true;
		 */
//		HashSet<Integer> seen = new HashSet<Integer>();
//		PriorityQueue<Section> pq = s.getSections();
//		while(!pq.isEmpty()) {
//			Section mysec = pq.poll();
//			List<Point2D> secpoints = mysec.getPoints();
//			for (Point2D p : secpoints) {
//				double px = p.getX();
//				double py = p.getY();
//				
//				if(mboard[px][py] > 0 && !lightwithin(5)) {
//					
//				}
//			}
//		}
		if (section == -1) {
			return true;
		}
		
		
		List<Section> l = v.getSectionList();
		Section thissec = null;
		for (Section s : l) {
			if(s.getId() == section) {
				thissec = s;
				break;
			}
		}
		List<Point2D> secpoints = thissec.getPoints();
		for(Point2D p : secpoints) {
			int px = (int) p.getX();
			int py = (int) p.getY();
			if (mboard[px][py] > 0 && distance(x, y, px, py) > 5) {
				return false;
			}
		}
//		for (int i = 0; i < 100; i++) {
//			for (int j = 0; j < 100; j++) {
//				if (board[i][j] == section && mboard[i][j] > 0 && distance(x, y, i, j) > 5) {
//					return false;
//				}
//			}
//		}
		return true;
	}
	
//	private boolean lightwithin(int radius) {
//		for(int i = -5; i < radius; i++) {
//			for (int j = -5; j < radius; j++) {
//				if(i >= 0 &&)
//			}
//		}
//	}
	
	private double distance(int x1, int y1, int x2, int y2) {
		int delx = Math.abs(x2 - x1);
		int dely = Math.abs(y2 - y1);
		
		return Math.pow( (delx*delx) + (dely*dely), .5);
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

	public ArrayList<Point2D> getStartingPoints() {
		return leftmostpoint;
	}
	
	public ArrayList<Point2D> getEndingPoints() {
		return rightmostpoint;
	}
	
	public void setCollector(Point2D collectorpoint) {
		this.collectorpos = collectorpoint;
	}
	
	private boolean intersectsWall(int x, int y) {
		for(Line2D wall : walls) {
			if(wall.intersects(x, y, 2, 1)) {
				return true;
			}
		}
		return false;
    }
}