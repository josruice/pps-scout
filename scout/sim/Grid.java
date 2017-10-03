package scout.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grid {
    List<List<List<CellObject>>> grid;
    Map<String, Point> location;
    int size;
    public Grid(int n) {
        size = n;
        grid = new ArrayList<List<List<CellObject>>>();
        location = new HashMap<>();
        for(int i = 0 ; i < n + 2 ; ++ i) {
            List<List<CellObject>> row = new ArrayList<>();
            for(int j = 0 ; j < n + 2; ++ j) {
                List<CellObject> cell = new ArrayList<>();
                row.add(cell);
            }
            grid.add(row);
        }
    }

    public List<CellObject> getCell(int x, int y) {
        if(x < 0 || x > size  + 1 || y < 0 || y > size + 1) return null;
        //System.out.println("getcell");
        return grid.get(x).get(y);
    }

    public List<CellObject> getCell(Point p) {
        return getCell(p.x, p.y);
    }

    public Point getLocationWithOffset(Point p, Point offset) {
        int x = p.x + offset.x;
        int y = p.y + offset.y;
        if(x > size + 1 || x < 0 || y < 0 || y > size + 1) return null;
        return new Point(x,y);
    }

    public void addAllCellObjects(List<CellObject> cellObjects, List<Point> locations) throws Exception{
        for(int i = 0 ;i < cellObjects.size(); ++i) {
            if(!( (locations.get(i).x >= 0 && locations.get(i).x <= size + 1
                && locations.get(i).y >= 0 && locations.get(i).y <= size + 1))) 
                throw new Exception("cell object location out of bounds");
            //System.out.println(i +" "+ locations.get(i).x +" "+locations.get(i).y);
            this.getCell(locations.get(i)).add(cellObjects.get(i));
            this.location.put(cellObjects.get(i).getID(),locations.get(i));
        }
    }

    public void update(CellObject object, Point newLocation) {
        Point oldLocation = this.location.get(object.getID());
        this.getCell(oldLocation).remove(object);
        this.getCell(newLocation).add(object);
        this.location.put(object.getID(), newLocation);
    }
}
