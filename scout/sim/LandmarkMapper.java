package scout.sim;

import java.util.List;
import java.util.Random;

abstract public class LandmarkMapper {
    abstract public List<Point> getLocations(int n);
    abstract public int getCount(int n);
}
