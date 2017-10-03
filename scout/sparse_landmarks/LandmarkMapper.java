package scout.sparse_landmarks;

import scout.sim.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LandmarkMapper extends scout.sim.LandmarkMapper {

    public static int log2(int n){
        if(n <= 0) return 0;
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    @Override
    public List<Point> getLocations(int n) {
        Random gen = new Random(222);
        List<Point> locations = new ArrayList<>();
        for(int i = 0 ; i < 4*log2(n) ; ++ i) {
            locations.add(new Point((gen.nextInt(n)) + 1, (gen.nextInt(n)) + 1) );
        }
        return locations;
    }

    @Override
    public int getCount(int n) {
        return 4 * log2(n);
    }
}
