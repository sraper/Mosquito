package mosquito.g4.voronoi;

import java.util.PriorityQueue;

import mosquito.g4.utils.Utils;

public class Sections {
    private PriorityQueue<Section> sections;
    private int[][] sectionBoard;

    public Sections() {
        sections = new PriorityQueue<Section>();
    }

    public PriorityQueue<Section> getSections() {
        return sections;
    }

    void addSection(Section section) {
        this.sections.add(section);
    }

    public Section peek() {
        return sections.peek();
    }

    public Section pop() {
        return sections.poll();
    }

    public void setSectionBoard(int[][] sectionBoard) {
        this.sectionBoard = sectionBoard;
    }
    
    public int[][] getSectionBoard() {
    	return sectionBoard;
    }

    public int getSection(int x, int y) {
        return Utils.withinBounds(0, 99, x) && Utils.withinBounds(0, 99, y) ? sectionBoard[x][y]
                : -1;
    }
}
