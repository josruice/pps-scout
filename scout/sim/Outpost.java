package scout.sim;

import java.util.ArrayList;
import java.util.List;

public class Outpost extends CellObject {
    private final Point location;
    private Object data;
    private final List<List<Integer>> enemyMap;

    public Outpost(int id, int n, int x, int y) {
        super("O" + id);
        data = null;
        enemyMap = new ArrayList<>();
        for(int i = 0; i < n+2; ++i) {
            List<Integer> row = new ArrayList<>();;
            for(int j = 0 ; j < n + 2; ++ j) {
                row.add(0);
            }
            enemyMap.add(row);
        }
        location = new Point(x,y);
    }
    
    /**
    * Store anything you want!
    */
    public void setData(Object ob) {
        data = ob;
    }

    public Point getLocation() {
        return location;
    }

    public Object getData() {
        try {
            return ObjectCloner.deepCopy(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void addEnemyLocation(Point p) {
        enemyMap.get(p.x).set(p.y, 1);
    }

    public void addSafeLocation(Point p) {
        enemyMap.get(p.x).set(p.y, 2);
    }

    public List<List<Integer>> getEnemyMap() {
        return enemyMap;
    }
}
