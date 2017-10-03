package scout.sim;

import java.util.Set;
import java.util.Random;

import java.util.List;
abstract public class EnemyMapper {
    abstract public Set<Point> getLocations(int n, int num, List<Point> landmarkLocations, Random gen);
}
