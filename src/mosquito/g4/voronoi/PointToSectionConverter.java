package mosquito.g4.voronoi;

import mosquito.g4.utils.Utils;

public class PointToSectionConverter {

    private int[][] sectionBoard;

    public PointToSectionConverter(int[][] sectionBoard) {
        this.sectionBoard = sectionBoard;
    }

    public int getSection(int x, int y) {

        return Utils.withinBounds(0, 99, x) && Utils.withinBounds(0, 99, y) ? sectionBoard[x][y]
                : -1;
    }
}
