package scout.g5;

import scout.sim.*;

import java.util.*;
import java.io.Serializable;


public class Player extends scout.sim.Player {

    int id;

    int totalTurns;
    int remainingTurns;
    int n; // Size of the board.

    // Denote the current x, y position.
    int x = -1;
    int y = -1;

    // Denote the direction traversed by the players.
    int dx;
    int dy;

    boolean isOriented = false;

    // Safe, enemy and unknownLocations.
    List<Point> safeLocations;
    List<Point> enemyLocations;
    List<Location> unknownLocations;

    // Coordinate details.
    Point assignedOutpost;
    Point upperLeft;
    Point upperRight;
    Point lowerRight;
    Point lowerLeft;

    // Player's FSM.
    PlayerFSM fsm;

    // Variables used for orienting.
    String xEdgeFound = null;
    String yEdgeFound = null;
    int distanceFromEdge = -1;

    // Variables for exploring
    Point nextPointToReach = null;
    ArrayList<Point> pointsToReach = new ArrayList<Point>();       
    int idx = 0;    
    int stride = 5; 
    int x_start, x_end, y_start, y_end = -1;
    HashMap<Integer, Boolean> scoutsInQuadrant = new HashMap<Integer, Boolean>();
    HashMap<Integer, Integer> idInitMapping = new HashMap<Integer, Integer>(); 
    
    public Player(int id) {
        super(id);
        this.id = id;
    }

    @Override
    public void init(String id, int s, int n, int t, List<Point> landmarkLocations) {
        enemyLocations = new ArrayList<>();
        safeLocations = new ArrayList<>();

        // We know that the limits are safe, so add that to the safe locations.
        for (int i = 0; i < n+2; ++i) {
            safeLocations.add(new Point(0,i));
            safeLocations.add(new Point(i,0));
            safeLocations.add(new Point(n+1,i));
            safeLocations.add(new Point(i,n+1));
        }

        unknownLocations = new ArrayList<>();
        this.totalTurns = t;
        this.remainingTurns = t;
        this.n = n;
        int scoutsPerQuadrant = 0;
        boolean reverseList = false;

        this.fsm = new PlayerFSM();

        switch(this.id % 4) {
            case 0:
                assignedOutpost = new Point(0,0);
                break;
            case 1:
                assignedOutpost = new Point(0,n+1);
                break;
            case 2:
                assignedOutpost = new Point(n+1,n+1);
                break;
            case 3:
                assignedOutpost = new Point(n+1,0);
                break;
            }


        if(s < 4)   {

                upperLeft = new Point(0,0);
                upperRight = new Point(0, n);
                lowerRight = new Point(n, n);
                lowerLeft = new Point(n, 0);
                
                x_start = 1;
                x_end   = n;
                y_start = 1;
                y_end   = n;

                scoutsPerQuadrant = s; 

                if(this.id == 1)    {
                    reverseList = true;
                }
                pointsToReach = generate_points(stride, x_start, x_end, y_start, y_end);

                if(this.id == 2)  {
                    idx = pointsToReach.size() / 2;
                }        

        }
        else    {

            scoutsPerQuadrant = s/4;

            // Set coordinate boundaries and assigned outpost.
            switch(this.id % 4) {
                case 0:
                    // Upper left quadrant
                    upperLeft = new Point(0,0);
                    upperRight = new Point(0, n/2);
                    lowerRight = new Point(n/2, n/2);
                    lowerLeft = new Point(n/2, 0);
                    
                    x_start = 1;
                    x_end   = n/2;
                    y_start = 1;
                    y_end   = n/2;
                    if(s%4 > 0) {
                        scoutsPerQuadrant++;
                    }
                    
                    break;
                case 1:
                    // Upper right quadrant
                    upperLeft = new Point(0, n/2 + 1);
                    upperRight = new Point(0, n+1);
                    lowerRight = new Point(n/2, n+1);
                    lowerLeft = new Point(n/2, n/2 + 1);
                    
                    x_start = 1;
                    x_end   = n/2;
                    y_start = n/2+1;
                    y_end   = n;
                    if(s%4 > 1) {
                        scoutsPerQuadrant++;
                    }
                            
                    break;
                case 2:
                    // Lower right quadrant
                    upperLeft = new Point(n/2 + 1, n/2 + 1);
                    upperRight = new Point(n/2 + 1, n+1);
                    lowerRight = new Point(n+1, n+1);
                    lowerLeft = new Point(n+1, n/2 + 1);               
                    
                    x_start = n/2+1;
                    x_end   = n;
                    y_start = n/2+1;
                    y_end   = n;
                    if(s%4 > 2) {
                        scoutsPerQuadrant++;
                    }
                            
                    break;
                case 3:
                    // Lower left quadrant
                    upperLeft = new Point(n/2 + 1, 0);
                    upperRight = new Point(n/2 + 1, n/2);
                    lowerRight = new Point(n+1, n/2);
                    lowerLeft = new Point(n+1, 0);
                    
                    x_start = n/2+1;
                    x_end   = n;
                    y_start = 1;
                    y_end   = n/2;
                    
                    break;                    
            }

            int i= this.id%4;        
            while(4 <= this.id  && i < this.id) {
                reverseList = ! reverseList;
                i+= 4;
            }
            pointsToReach = generate_points(stride, x_start, x_end, y_start, y_end);
            

            if(this.id >= (this.id%4+2*4))  {
                if(reverseList == false)    {
                    idx = (this.id/4)*(pointsToReach.size()/(scoutsPerQuadrant));    
                }
                else {
                    idx = (this.id/4-1)*(pointsToReach.size()/(scoutsPerQuadrant));    
                }
                
            }        
            
            
        }
        
        
        if(reverseList == true) {
            Collections.reverse(pointsToReach);
        }

        System.out.println("I'm " + this.id + " and my idx is " + idx + " and reversed is " +  reverseList);

        nextPointToReach = pointsToReach.get(idx);                
    }

    @Override
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        Point nextMove = fsm.move(this, nearbyIds, concurrentObjects);

        // The player thinks they are not oriented. However, the FSM determined the position.
        if (!isOriented && x != -1) {
            unravelData();
            isOriented = true;
        }

        gatherInfo(nearbyIds, concurrentObjects);

        dx = nextMove.x;
        dy = nextMove.y;

        return nextMove;
    }

    // Communicate with other players.
    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        --remainingTurns;

        for (CellObject obj : concurrentObjects) {
            if (obj instanceof Player) {
                if (((Player) obj).id != this.id) {
                    mergeData((Player)obj);
                }
            }
        }
    }

    @Override
    public void moveFinished() {
        if (x != -1) {
            x += dx;
            y += dy;
        }
    }

    // Go to xFinal, yFinal.
    public Point goToPosition(int xFinal, int yFinal, ArrayList<ArrayList<ArrayList<String>>> nearbyIds) {
        int moveX = 1;
        int moveY = 1;

        if(xFinal > x) {
            moveX = 1;
        } else if(xFinal == x) {
            moveX = 0;
        } else {
            moveX = -1;
        }

        if(yFinal > y) {
            moveY = 1;
        } else if(yFinal == y) {
            moveY = 0;
        } else {
            moveY = -1;
        }

        // Say the player needs to reach xFinal, yFinal but there is an enemy at that location.
        // The current below behaviour will force the player to move into the enemy spot.
        // The behaviour may need to be tweaked further depending on what is needed.
        if (this.x + moveX == xFinal && this.y + moveY == yFinal) {
            return new Point(moveX, moveY);
        }

        // System.out.printf("To go: %d, moveX: %d, moveY: %d", this.id, xFinal, yFinal);
        // System.out.printf("Before Id: %d, moveX: %d, moveY: %d", this.id, moveX, moveY);

        if (isEnemyAtGivenPoint(moveX+1, moveY+1, nearbyIds)) {
            // If the player is moving diagonally.
            if (moveX != 0 && moveY != 0) {
                if (!isEnemyAtGivenPoint(moveX+1, 1, nearbyIds)) {
                    moveY = 0;
                }
                else if (!isEnemyAtGivenPoint(1, moveY+1, nearbyIds)){
                    moveX = 0;
                }
            }
            else if (moveX == 0 && moveY != 0) {
                if (!isEnemyAtGivenPoint(2, moveY+1, nearbyIds)) {
                    moveX = 1;
                }
                else if (!isEnemyAtGivenPoint(0, moveY+1, nearbyIds)){
                    moveX = -1;
                }
            }
            else if (moveX != 0 && moveY == 0) {
                if (!isEnemyAtGivenPoint(moveX+1, 2, nearbyIds)) {
                    moveY = 1;
                }
                else if (!isEnemyAtGivenPoint(moveX+1, 0, nearbyIds)){
                    moveY = -1;
                }
            }
        }

        // System.out.printf("After Id: %d, moveX: %d, moveY: %d", this.id, moveX, moveY);

        return new Point(moveX, moveY);
    }

    // Return true if there is an enemy at pointX, pointY.
    private boolean isEnemyAtGivenPoint(int pointX, int pointY, ArrayList<ArrayList<ArrayList<String>>> nearbyIds) {
        boolean enemy = false;

        for(String ID : nearbyIds.get(pointX).get(pointY)) {
            if(ID.charAt(0) == 'E') {
                enemy = true;
                break;
            }
        }

        return enemy;
    }

    // Merge your data with that of the other player.
    private void mergeData(Player p) {
        HashSet<Point> unionLocations = new HashSet<Point>();
        unionLocations.addAll(safeLocations);
        unionLocations.addAll(p.safeLocations);
        safeLocations = new ArrayList<Point>(unionLocations);


        HashSet<Point> unionEnemies = new HashSet<Point>();
        unionEnemies.addAll(enemyLocations);
        unionEnemies.addAll(p.enemyLocations);
        enemyLocations = new ArrayList<Point>(unionEnemies);
    }

    // Store information from this position.
    private void gatherInfo(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        List<Point> enemyLocs = new ArrayList<Point>();
        List<Point> safeLocs = new ArrayList<Point>();

        for(int i = 0 ; i < 3; ++ i) {
            for(int j = 0 ; j < 3 ; ++ j) {
                boolean safe = true;
                if(nearbyIds.get(i).get(j) == null) continue;
                for(String ID : nearbyIds.get(i).get(j)) {
                    if(ID.charAt(0) == 'E') {
                        safe = false;
                    }
                }

                if (x == -1) {
                    if (!safe) {
                        enemyLocs.add(new Point(i - 1, j - 1));
                    } else {
                        safeLocs.add(new Point(i - 1, j - 1));
                    }
                }
                else {
                    Point consideredLocation = new Point(x + i - 1, y + j - 1);
                    if(safe) {
                        if(!safeLocations.contains(consideredLocation)) {
                            safeLocations.add(consideredLocation);
                        }
                    } else {
                        if(!enemyLocations.contains(consideredLocation)) {
                            enemyLocations.add(consideredLocation);
                        }
                    }
                }
            }
        }

        if (x == -1) {
            unknownLocations.add(new Location(dx, dy, enemyLocs, safeLocs));
        }
    }

    // Unveil the previously unknown locations now that the player is oriented.
    public void unravelData() {
        int prevX = x - dx;
        int prevY = y - dy;
        for (int i = unknownLocations.size() - 1; i > 0; --i) {
            Location loc = unknownLocations.get(i);
            //System.out.println("Id: " + id + " pos: " + prevX + " " + prevY);
            for (Point p : loc.enemyLocations) {
                //System.out.println("Id: " + id + " Enemy pos: " + (prevX + p.x) + " " + (prevY + p.y));
                Point enemy = new Point(prevX + p.x, prevY + p.y);
                if (!enemyLocations.contains(enemy)) {
                    enemyLocations.add(enemy);
                }
            }

            for (Point p : loc.safeLocations) {
                Point safe = new Point(prevX + p.x, prevY + p.y);
                if (!safeLocations.contains(safe)) {
                    safeLocations.add(safe);
                }
            }

            prevX = prevX - loc.dx;
            prevY = prevY - loc.dy;
        }
    }
    
    
    private ArrayList<Point> generate_points(int stride, int x_start, int x_end, int y_start, int y_end)
    {
        ArrayList<Point> first_half_points = new ArrayList<Point>();
        ArrayList<Point> second_half_points = new ArrayList<Point>();
        first_half_points.add(new Point(x_end, y_start));
        second_half_points.add(new Point(x_start, y_end));
        
        for(int i=stride; i < (x_end-x_start); i+= stride)
        {
            Point point_a = new Point(x_end,       y_start + i);
            Point point_b = new Point(x_end - i,   y_start    );
        
            Point rev_point_a = new Point(x_start + i, y_end    );
            Point rev_point_b = new Point(x_start,     y_end - i);
                
            // Order in which points are added has to be alterned to maintain correct order.
            if((i % (stride*2)) != 0)
            {
                first_half_points.add(point_a);
                first_half_points.add(point_b);
                second_half_points.add(0, rev_point_a);
                second_half_points.add(0, rev_point_b);
            }
            else    {
                first_half_points.add(point_b);
                first_half_points.add(point_a);
                second_half_points.add(0, rev_point_b);
                second_half_points.add(0, rev_point_a);
            }
        }
        

        if(first_half_points.get(first_half_points.size()-1).x == x_end)   {
            second_half_points.add(0, new Point(x_start, y_start));
            second_half_points.add(0, new Point(x_end, y_end));
        } else  {
            second_half_points.add(0, new Point(x_end, y_end));
            second_half_points.add(0, new Point(x_start, y_start));            
        }
        
        first_half_points.addAll(second_half_points);
        return first_half_points;        
    }
}

// Store details of a particular location.
class Location implements Serializable{
    // The direction moved to reach this point.
    public int dx;
    public int dy;

    // The list of neighbouring enemies with respect to this location.
    public List<Point> enemyLocations;

    // The list of neighbouring safe locations with respect to this location.
    public List<Point> safeLocations;

    public Location(int moveX, int moveY, List<Point> enemyLoc, List<Point> safeLoc) {
        this.dx = moveX;
        this.dy = moveY;
        this.enemyLocations = enemyLoc;
        this.safeLocations = safeLoc;
    }
}