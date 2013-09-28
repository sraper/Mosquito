package mosquito.g4.voronoi;

import java.util.PriorityQueue;

public class Sections {
    private PriorityQueue<Section> sections;

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
}
