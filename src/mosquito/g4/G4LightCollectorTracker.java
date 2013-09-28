package mosquito.g4;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import mosquito.sim.Light;

/**
 * Maintains the state of each light and figures out which light should stay
 * with the collector
 * 
 * @author Hari
 * 
 */
public class G4LightCollectorTracker {
    private Double collectorLocation;
    private static double MIN_DISTANCE = 2;

    G4LightCollectorTracker(Point2D.Double collectorLocation) {
        this.collectorLocation = collectorLocation;
    }

    void updateLights(Iterable<Light> lights) {
        boolean foundOneStayPutLight = false;
        for (Light l : lights) {
            Point2D.Double lightLocation = new Point2D.Double(l.getX(),
                    l.getY());

            G4Light g4l = (G4Light) l;

            boolean shouldStayPut = g4l.isOn()
                    && isWithinDistance(lightLocation, collectorLocation);

            if (foundOneStayPutLight) {
                g4l.stayPut(false);
            } else {
                g4l.stayPut(shouldStayPut);
                foundOneStayPutLight = true;
            }
        }
    }

    private boolean isWithinDistance(Double lightLocation,
            Double collectorLocation) {
        boolean distance = lightLocation.distance(collectorLocation) <= MIN_DISTANCE;
        return distance;
    }
}
