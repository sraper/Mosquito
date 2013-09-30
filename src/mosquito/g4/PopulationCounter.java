package mosquito.g4;

import java.util.PriorityQueue;

public class PopulationCounter {
    PriorityQueue<Population> populations;

    public PopulationCounter() {
        this.populations = new PriorityQueue<Population>(Quadrant.NUM_QUADRANTS);
    }

    void updatePopulation(int[][] population) {
        int[] populationPerQuadrant = new int[Quadrant.NUM_QUADRANTS];

        int rows = population.length;
        int columns = population[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                populationPerQuadrant[Quadrant.getQuadrant(i, j)] += population[i][j];
            }
        }

        populations.clear();

        for (int i = 0; i < populationPerQuadrant.length; i++) {
            populations.add(new Population(i, populationPerQuadrant[i]));
        }
    }

    Population peekTop() {
        return populations.peek();
    }

    Population popTop() {
        return populations.poll();
    }
}

class Population implements Comparable<Population> {
    int population, quadrant;

    public Population(int quadrant, int population) {
        this.population = population;
        this.quadrant = quadrant;
    }

    @Override
    public int compareTo(Population o) {
        return o.population - this.population;
    }

    public String toString() {
        return String.format("Pop = %d. Quadrant = %d", population, quadrant);
    }

}
