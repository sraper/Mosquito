package mosquito.g4;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.log4j.Logger;

import mosquito.g4.voronoi.Section;
import mosquito.g4.voronoi.Sections;
import mosquito.g4.voronoi.Voronoi;
import mosquito.sim.Light;

public class PositionPicker {
	private static final int MIN_SECTION_SIZE = 200;
	
    private static final Logger log = Logger.getLogger(VoronoiPlayer.class);
    
	private Voronoi v;
	private Sections sections;
	private AStar star;
	private ArrayList<Point2D> startingpoints;
	private ArrayList<Point2D> endingpoints;
	private HashMap<SectionTuple<Integer, Integer>, Integer> seen;
	
	public PositionPicker(Voronoi v, Sections sections, AStar star, ArrayList<Point2D> startingpoints, ArrayList<Point2D> endingpoints) {
		this.v = v;
		this.sections = sections;
		this.star = star;
		this.startingpoints = startingpoints;
		this.endingpoints = endingpoints;
		this.seen = new HashMap<SectionTuple<Integer, Integer>, Integer>();
	}
	
	// find collector position
	public Point2D getCollectorPosition() {
		HashMap<Section, Integer> agg = new HashMap<Section, Integer>();
		
		PriorityQueue<Section> candidates = sections.getSections();
		while(!candidates.isEmpty()) {
			Section candsection = candidates.poll();
			if (candsection.getPopulation() < MIN_SECTION_SIZE) break;
			
			agg = addDistances(candsection, agg);
		}
		
		log.trace(agg.toString());
		Section collectorsection = null;
		int min = Integer.valueOf(Integer.MAX_VALUE);
		for (Map.Entry<Section, Integer> e : agg.entrySet()) {
			if (min > e.getValue()) {
				collectorsection = e.getKey();
				min = e.getValue();
			}
		}
		
		Point2D p = endingpoints.get(collectorsection.getId());
		return p;
	}
	
	// TODO: debug light placement. Return too few lights right now I think?
	// find best light positions
	public Set<Light> getLightPosition(int numLights, Point2D collector) {
		Set<Light> lights = new HashSet<Light>();
        HashSet<Integer> seensections = new HashSet<Integer>();
		
        for(int i = 0; i < numLights; i++) {
        	int maxdist = 0;
        	int section = 0;
			for(int j = 0; j < v.getNumSections(); j++) {
				int s1 = sections.getSection((int)collector.getX(), (int)collector.getY());
				int s2 = sections.getSection((int) startingpoints.get(j).getX(), (int) startingpoints.get(j).getY());
				SectionTuple<Integer, Integer> d = new SectionTuple(s1, s2);
				if(seen.containsKey(d) && !seensections.contains(section) && seen.get(d) > maxdist) {
					maxdist = seen.get(d);
					section = s2;
				}
			}
			lights.add(new G4Light(startingpoints.get(section).getX(), startingpoints.get(section).getY(), i));
			seensections.add(section);
		}
		
		return lights;
	}
	
	// collector position helper
	@SuppressWarnings({ "rawtypes", "unchecked" }) // yolo
	private HashMap<Section, Integer> addDistances(Section candsection, HashMap<Section, Integer> agg) {
		PriorityQueue<Section> distance = sections.getSections();
		while (!distance.isEmpty()) {
			Section distsection = distance.poll();
			if(distsection.getPopulation() < MIN_SECTION_SIZE) return agg;
			
			int mydist;
			if(seen.containsKey(new SectionTuple(candsection.getId(), distsection.getId()))) {
				mydist = seen.get(new SectionTuple(candsection.getId(),distsection.getId()));
			} else {
				Point2D p1 = startingpoints.get(distsection.getId());
				Point2D p2 = startingpoints.get(candsection.getId());
				ArrayList<Double> path = star.getPath(p1, p2);
				mydist = path.size();
				seen.put(new SectionTuple(candsection.getId(), distsection.getId()), path.size());
				seen.put(new SectionTuple(distsection.getId(), candsection.getId()), path.size());
			}
			
			int newdist = agg.containsKey(candsection) ? agg.get(candsection) : 0;
			newdist += mydist;
			agg.put(candsection, newdist);
		}
		return agg;
	}
}

class SectionTuple<X, Y> { 
	  public final int x; 
	  public final int y;
	  
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((x == null) ? 0 : x.hashCode());
//		result = prime * result + ((y == null) ? 0 : y.hashCode());
//		return result;
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		SectionTuple other = (SectionTuple) obj;
//		if (x == null) {
//			if (other.x != null)
//				return false;
//		} else if (x != other.x)
//			return false;
//		if (y == null) {
//			if (other.y != null)
//				return false;
//		} else if (y != other.y)
//			return false;
//		return true;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SectionTuple other = (SectionTuple) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	public SectionTuple(int x, int y) { 
		  this.x = x; 
		  this.y = y; 
	  }
}