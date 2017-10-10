package scout.sim;
import java.io.Serializable;
public class Point implements Serializable {
    public int x;
    public int y;
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
    	if(!(other instanceof Point)) return false;
    	Point o = (Point) other;
    	return x == o.x && y == o.y;
    }

    @Override
    public int hashCode() {
    	return new Integer(x * 10000 + y).hashCode();
    }
}
