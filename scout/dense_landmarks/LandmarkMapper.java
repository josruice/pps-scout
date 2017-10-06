package scout.dense_landmarks;

import scout.sim.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

public class LandmarkMapper extends scout.sim.LandmarkMapper {
    @Override
    public List<Point> getLocations(int n) {
        // "Random"
        Random gen = new Random(222);
        Set<Point> locations = new HashSet<>();
        while(locations.size() < 4*n) {
            locations.add(new Point((gen.nextInt(n)) + 1, (gen.nextInt(n)) + 1) );
        }
        return new ArrayList(locations);
    }

    @Override
    public int getCount(int n) {
        return 4 * n;
    }
}
