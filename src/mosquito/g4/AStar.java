package mosquito.g4;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import mosquito.g4.utils.GeometryUtils;
import mosquito.g4.utils.Utils;

import org.apache.log4j.Logger;

public class AStar {


	private int[][] wallMap;
	private Set<Line2D> walls;
	private static final Logger log = Logger.getLogger(VoronoiPlayer.class); // for

	private double pointExtend(double m){
		double c = 0.05;
		return Math.sqrt(Math.pow(c, 2)/(1+Math.pow(m,2)));
	}
	
	private Line2D extendWall(Line2D wall){
		double x1 = wall.getX1();
		double x2 = wall.getX2();
		double y1 = wall.getY1();
		double y2 = wall.getY2();
		double c = 0.05;
		log.trace("Before : " + wall.getP1() + " " + wall.getP2());
		if (GeometryUtils.equals(x1, x2)){
			if (y1 < y2)
				wall.setLine(new Point2D.Double(x1,y1-c), new Point2D.Double(x2,y2+c));
			else
				wall.setLine(new Point2D.Double(x1,y1+c), new Point2D.Double(x2,y2-c));
			log.trace("After : " + wall.getP1() + " " + wall.getP2());
			return wall;
		}
	/*	if (GeometryUtils.equals(y1, y2)){
			if (x1 < x2)
				wall.setLine(new Point2D.Double(x1-c,y1), new Point2D.Double(x2+c,y2));
			else
				wall.setLine(new Point2D.Double(x1+c,y1), new Point2D.Double(x2-c,y2));
			log.trace("After : " + wall.getP1() + " " + wall.getP2());
			return wall;
		}*/
	
		double m = (y2-y1)/(x2-x1);
		if (x1 < x2 && y1 < y2)
			wall.setLine(new Point2D.Double(x1-pointExtend(m), y1-pointExtend(m)*m), new Point2D.Double(x2+pointExtend(m), y2+pointExtend(m)*m));
		else if (x1 > x2 && y1 < y2)
			wall.setLine(new Point2D.Double(x1+pointExtend(m), y1-pointExtend(m)*m), new Point2D.Double(x2-pointExtend(m), y2+pointExtend(m)*m));
		else if (x1 < x2 && y1 > y2)
			wall.setLine(new Point2D.Double(x1-pointExtend(m), y1+pointExtend(m)*m), new Point2D.Double(x2+pointExtend(m), y2-pointExtend(m)*m));
		else // if (x1 > x2 && y1 > y2)
			wall.setLine(new Point2D.Double(x1+pointExtend(m), y1+pointExtend(m)*m), new Point2D.Double(x2-pointExtend(m), y2-pointExtend(m)*m));
		
		log.trace("After : " + wall.getP1() + " " + wall.getP2());
		return wall; 
	}
	
	public AStar(Set<Line2D> walls){
		for (Line2D wall : walls){
		wall = extendWall(wall);
		}
		this.walls = walls; 
	
	}
	
	public Set<Line2D> getWalls(){
		return walls;
	}

	private ArrayList<Point> getNeighbors(Point2D.Double point){
		ArrayList<Point> neighbors = new ArrayList<Point>();
		if (point.y < 99)
			neighbors.add(new Point(point.x, point.y+1));
		if (point.y > 0)
			neighbors.add(new Point(point.x, point.y-1));
		if (point.x < 99)
			neighbors.add(new Point(point.x+1, point.y));
		if (point.x > 0)
			neighbors.add(new Point(point.x-1, point.y));
		return neighbors;
	}

	public class Point extends Point2D.Double implements Comparable<Point> {
		
		public int score;
		
		public Point(Point2D val){
			this.x = val.getX();
			this.y = val.getY();
			score = Integer.MAX_VALUE;
		}
		
		public Point(double x, double y){
			this.x = x;
			this.y = y;
			score = Integer.MAX_VALUE;
		}
		
		public Point2D to2D(){
			return new Point2D.Double(this.x, this.y);
		}
		
		@Override
		public int compareTo(Point arg0){
			Point other = (Point)arg0;
			if (this.score < other.score)
				return -1;
			else if (this.score == other.score)
				return 0;
			else
				return 1;
		}

	}

	public int maxout(int score, int x){
		if (score + x > 0 && score + x < Integer.MAX_VALUE)
			return score+x;
		else
			return Integer.MAX_VALUE;
	}
	
	public ArrayList<Point2D.Double> getPath(Point2D start, Point2D end){
		HashSet<Point2D.Double> closedSet = new HashSet<Point2D.Double>(); 
		PriorityQueue<Point> openSet = new PriorityQueue<Point>();
		HashMap<Point, Point>cameFrom = new HashMap<Point, Point>(); 
		
		Point min = new Point(start);
		openSet.add(min);
		min.score = 0;
		while (!openSet.isEmpty()){
			Point current = openSet.poll();
			if (current.x == end.getX() && current.y == end.getY()){
				ArrayList<Point2D.Double> finalPath = reconstructPath(cameFrom, current);
				Point2D.Double firstPoint = finalPath.get(0);
				
				for (int i=0; i<5; i++)
					finalPath.add(0, firstPoint);
				int i = 0;
				while (i < finalPath.size()){
					if (i % 18 == 0){
						Point2D.Double random = finalPath.get(i);
						for (int j=0;j<5;j++){
							finalPath.add(i, random);
						}
						i+=6;
					}
					i++;
				}
				return finalPath;
			}
			closedSet.add(current);
			ArrayList<Point> neighbors = getNeighbors(current);
			for (Point neighbor : neighbors){
				int score = 1;
				if (!Utils.hasStraightPath(current, neighbor, walls))
					score = maxout(score, Integer.MAX_VALUE);
				for (Point nb2 : getNeighbors(neighbor)){
					if (!Utils.hasStraightPath(neighbor, nb2, walls))
						score = maxout(score, 10000);
		/*			for (Point nb3 : getNeighbors(nb2)){
						if (!Utils.hasStraightPath(nb3, nb2, walls))
							score = maxout(score, 1000);
						for (Point nb4 : getNeighbors(nb3)){
							if (!Utils.hasStraightPath(nb3, nb4, walls))
								score = maxout(score, 100000);	
						}
						
					} */
				}
				int neighbor_cost = maxout(current.score, score);
				if (closedSet.contains(neighbor))
					continue;
				if (!openSet.contains(neighbor) || neighbor_cost < neighbor.score) {
					cameFrom.put(neighbor, current);
					neighbor.score = neighbor_cost;
					if (!openSet.contains(neighbor))
						openSet.add(neighbor);
				}
			}
		}
		return null;
	}
	
	public ArrayList<Point2D.Double> reconstructPath(HashMap<Point,Point> cameFrom, Point curr){
		if (cameFrom.containsKey(curr)){
			ArrayList<Point2D.Double> tail = reconstructPath(cameFrom, cameFrom.get(curr));
			tail.add(curr);
			return tail;
		} else {
			ArrayList<Point2D.Double> base = new ArrayList<Point2D.Double>();
			base.add(curr);
			return base;
		}
	}

}
