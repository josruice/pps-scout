package scout.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScoutMapper {

    public List<Point> getLocations(int n, int num, Random gen) {
        List<Point> locations = new ArrayList<>();
        for(int i = 0; i < num; ++ i) {
            locations.add(//range 1 to n
                new Point(1 + gen.nextInt(n), 1 + gen.nextInt(n))
            );
        }
        return locations;
    }
}
