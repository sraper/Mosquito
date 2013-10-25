package mosquito.g4;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.HashMap;
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
	private int numLights;
	private ArrayList<Point2D> startingpoints;
	private HashMap<SectionTuple<Section, Section>, Integer> seen;
	
	public PositionPicker(Voronoi v, Sections sections, AStar star, int numLights, ArrayList<Point2D> startingpoints) {
		this.v = v;
		this.sections = sections;
		this.star = star;
		this.numLights = numLights;
		this.startingpoints = startingpoints;
		this.seen = new HashMap<SectionTuple<Section, Section>, Integer>();
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
		return startingpoints.get(collectorsection.getId());
	}
	
	// find best light positions
	public Set<Light> getLightPosition() {
		
		return null;
	}
	
	// collector position helper
	@SuppressWarnings({ "rawtypes", "unchecked" }) // yolo
	private HashMap<Section, Integer> addDistances(Section candsection, HashMap<Section, Integer> agg) {
		PriorityQueue<Section> distance = sections.getSections();
		while (!distance.isEmpty()) {
			Section distsection = distance.poll();
			if(distsection.getPopulation() < MIN_SECTION_SIZE) return agg;
			
			int mydist;
			if(seen.containsKey(new SectionTuple(candsection, distsection))) {
				mydist = seen.get(new SectionTuple(candsection,distsection));
			} else {
				Point2D p1 = startingpoints.get(distsection.getId());
				Point2D p2 = startingpoints.get(candsection.getId());
				ArrayList<Double> path = star.getPath(p1, p2);
				mydist = path.size();
				seen.put(new SectionTuple(candsection, distsection), path.size());
				seen.put(new SectionTuple(distsection, candsection), path.size());
			}
			
			int newdist = agg.containsKey(candsection) ? agg.get(candsection) : 0;
			newdist += mydist;
			agg.put(candsection, newdist);
		}
		return agg;
	}
}

class SectionTuple<X, Y> { 
	  public final Section x; 
	  public final Section y;
	  
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SectionTuple<Section, Section> other = (SectionTuple<Section, Section>) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (x.getId() != other.x.getId())
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (y.getId() != other.y.getId())
			return false;
		return true;
	}

	public SectionTuple(Section x, Section y) { 
		  this.x = x; 
		  this.y = y; 
	  }
}