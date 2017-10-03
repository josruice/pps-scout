package scout.g5_enemymap;

import scout.sim.Point;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Random;

public class EnemyMapper extends scout.sim.EnemyMapper {
    @Override
    public Set<Point> getLocations(int n, int num, List<Point> landmarkLocation, Random gen) {
        Set<Point> locations = new HashSet<>();
              
        int closestUpperSquare = -1;
        int i = 1;
        for(i=1; i*i+1<num;i++) {}


        closestUpperSquare = i;

        int x = n/2 - closestUpperSquare/2;
        int y = n/2 - closestUpperSquare/2; 
        int xIncrement = 0;
        int yIncrement = 0;

        while(locations.size() < num) {
            locations.add(new Point(x+xIncrement, y+yIncrement));
            
            yIncrement += 1;
            if(yIncrement % closestUpperSquare == 0 && yIncrement>0) {
                yIncrement = 0;
                xIncrement += 1;
            }

        }
        return locations;
    }
}
