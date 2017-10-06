package scout.sim;

import java.util.ArrayList;
import java.util.List;

abstract public class Player extends CellObject {

    /**
    * better to use init instead of constructor, don't modify ID or simulator will error
    */
    public Player(int id) {
        super("P" + id);
    }

    /**
    *   Called at the start
    * @param id Is the letter 'S' concatenated with an index. Index is the same int as in the constructor.
    * @param s The number of scouts
    * @param n The size of the enemy space, board size is therefore (n+2)x(n+2)
    * @param t The number of turns for the game
    * @param landmarkLocations The list of (x,y) landmarks. Enemies and landmarks can coincide.
    */
    public abstract void init(String id, int s, int n, int t, List<Point> landmarkLocations);

    
    /**
    * @param nearbyIds 3 x 3 grid of list of neighbouring IDs. 
    *       nearbyIds.get(1).get(1) is the center, which is the current location of the player. 
    *  Scout IDs look like "S123" where 123 is some unique numbering of scouts
    *  Enemy IDs look like "E123" where 123 is some unique random number
    *  Landmark IDs look like "L123" where 123 is some unique numbering of landmarks
    *  Outpost IDs look like "O123" where 123 is some unique numbering of outposts
    *       Outposts can only be at (0,0) or (0,n+1) or (n+1,0) or (n+1,n+1) on the board
    *  If any position of the 3x3 grid is null, it is off the board, i.e. you are on the border.
    * @param concurrentObjects has the CellObjects on the (1,1) position of nearbyIds,
    *       i.e. the objects on the same cell. 
    *       The (x,y) coordinate of Landmarks can be retrieved using ((Landmark)object).getLocation();
    *       Information can be stored in or retrieved from Outposts using the public methods of scout.sim.Outpost
    *       Communication between players takes place using custom methods, since you have access to the player object.
    * @return (x,y) direction, x in {-1,0,1} y in {-1,0,1}
    */
    public abstract Point move(
            ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
            //objects on your location
            List<CellObject> concurrentObjects);

    /**
    * Called every turn as opposed to every 2/3/6/9 turns.
    * @param nearbyIds 3 x 3 grid of list of neighbouring IDs. 
    *  Scout IDs look like "S123" where 123 is some unique numbering of scouts
    *  Enemy IDs look like "E123" where 123 is some unique random number
    *  Landmark IDs look like "L123" where 123 is some unique numbering of landmarks
    *  Outpost IDs look like "O123" where 123 is some unique numbering of outposts
    *  Outposts can only be at (0,0) or (0,n+1) or (n+1,0) or (n+1,n+1). 
    */
    public abstract void communicate(
            ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
            List<CellObject> concurrentObjects
    );


    /**
    * Called when the actual move finishes. Update your player's x and y here!
    */
    public abstract void moveFinished();
}