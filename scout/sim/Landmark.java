package scout.sim;

public class Landmark extends CellObject {

    private final Point location;
    public Landmark(int id, int x, int y) {
        super("L" + id);
        location = new Point(x,y);
    }

    public Point getLocation() {
        return location;
    }
}
