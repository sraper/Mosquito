package mosquito.g4.voronoi;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class Sections {
    private PriorityQueue<Section> sections;
    private List<Section> sectionsList;
    private PointToSectionConverter converter;

    public Sections(PointToSectionConverter converter) {
        sections = new PriorityQueue<Section>();
        this.sectionsList = new LinkedList<Section>();
        this.converter = converter;
    }

    public PriorityQueue<Section> getSections() {
        return sections;
    }

    void addSection(Section section) {
        this.sections.add(section);
        sectionsList.add(section);
    }

    public Section peek() {
        return sections.peek();
    }

    public Section pop() {
        return sections.poll();
    }

    public int getSection(int x, int y) {
        return this.converter.getSection(x, y);
    }

    public int[][] getSectionBoard() {
        return converter.getSectionBoard();
    }

    public void finished() {

    }
}
