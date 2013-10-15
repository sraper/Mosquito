package mosquito.g4;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class AStar {


	private int[][] wallMap;

	public AStar(Set<Line2D> walls){
		wallMap = new int[100][100];
		if (walls.isEmpty()){
			for (int i=0; i< 100; i++){
				for (int j=0;j< 100; j++){
					wallMap[i][j] = 1;
				}
			}
		}
		else {
			for (Line2D wall : walls){
				for (int i=0; i< 100; i++){
					for (int j=0;j< 100; j++){
						if (wall.contains(new Point2D.Double(i,j)))
							wallMap[i][j] = Integer.MAX_VALUE;
						else
							wallMap[i][j] = 1;
					}
				}
			}
		}
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
	/*
	public void star(){
		HashSet<Point> closed = new HashSet<Point>();
		PriorityQueue<> queue = new PriorityQueue<double, Path<Node>>();
		queue.Enqueue(0, new Path<Node>(start));
		while (!queue.IsEmpty)
		{
		    var path = queue.Dequeue();
		    if (closed.Contains(path.LastStep)) continue;
		    if (path.LastStep.Equals(destination)) return path;
		    closed.Add(path.LastStep);
		    foreach(Node n in path.LastStep.Neighbours)
		    {
		        double d = distance(path.LastStep, n);
		        var newPath = path.AddStep(n, d);
		        queue.Enqueue(newPath.TotalCost + estimate(n), newPath);
		    }
		}
	} */
	
	public ArrayList<Point2D.Double> getPath(Point2D.Double start, Point2D.Double end){
		HashSet<Point2D.Double> closedSet = new HashSet<Point2D.Double>(); 
		PriorityQueue<Point> openSet = new PriorityQueue<Point>();
		HashMap<Point, Point>cameFrom = new HashMap<Point, Point>(); 
		
		Point min = new Point(start);
		openSet.add(min);
		min.score = 0;
		while (!openSet.isEmpty()){
			Point current = openSet.poll();
			if (current.x == end.x && current.y == end.y){
				ArrayList<Point2D.Double> finalPath = reconstructPath(cameFrom, current);
				finalPath.remove(0);
				return finalPath;
			}
			closedSet.add(current);
			ArrayList<Point> neighbors = getNeighbors(current);
			for (Point neighbor : neighbors){
				int neighbor_cost = current.score + wallMap[(int)neighbor.x][(int)neighbor.y];
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
