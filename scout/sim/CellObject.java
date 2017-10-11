package scout.sim;
import java.io.Serializable;
public class CellObject implements Serializable{
	private final String id;
    public CellObject(String id) {
        this.id = id;
    }
    public String getID() {
        return this.id;
    }
}