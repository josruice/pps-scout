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
    HashMap<Integer, Integer> idInitMapping = new HashMap<Integer, Integer>(); 
    
    public Player(int id) {
        super(id);
        this.id = id;
    }

    @Override
    public void init(String id, int s, int n, int t, List<Point> landmarkLocations) {
        enemyLocations = new ArrayList<>();
        safeLocations = new ArrayList<>();
        unknownLocations = new ArrayList<>();
        this.totalTurns = t;
        this.remainingTurns = t;
        this.n = n;
        int scoutsInQuadrant = s/4;
        
        this.fsm = new PlayerFSM();
 
        
        // Set coordinate boundaries and assigned outpost.
        switch(this.id % 4) {
            case 0:
                // Upper left quadrant
                // System.out.println("Upper left quadrant");
                assignedOutpost = new Point(0,0);
                upperLeft = new Point(0,0);
                upperRight = new Point(0, n/2);
                lowerRight = new Point(n/2, n/2);
                lowerLeft = new Point(n/2, 0);
                
                x_start = 1;
                x_end   = n/2-1;
                y_start = 1;
                y_end   = n/2-1;
                if(s%4 > 0) {
                    scoutsInQuadrant++;
                }
                
                
                
                break;
            case 1:
                // Upper right quadrant
                // System.out.println("Upper right quadrant");
                assignedOutpost = new Point(0,n+1);
                upperLeft = new Point(0, n/2 + 1);
                upperRight = new Point(0, n+1);
                lowerRight = new Point(n/2, n+1);
                lowerLeft = new Point(n/2, n/2 + 1);
                
                x_start = 1;
                x_end   = n/2-1;
                y_start = n/2+1;
                y_end   = n-1;
                if(s%4 > 1) {
                    scoutsInQuadrant++;
                }
                        
                break;
            case 2:
                // Lower right quadrant
                // System.out.println("Lower right quadrant");
                assignedOutpost = new Point(n+1,n+1);
                upperLeft = new Point(n/2 + 1, n/2 + 1);
                upperRight = new Point(n/2 + 1, n+1);
                lowerRight = new Point(n+1, n+1);
                lowerLeft = new Point(n+1, n/2 + 1);               
                
                x_start = n/2+1;
                x_end   = n;
                y_start = n/2+1;
                y_end   = n-1;
                if(s%4 > 2) {
                    scoutsInQuadrant++;
                }
                        
                break;
            case 3:
                // Lower left quadrant
                // System.out.println("Lower left quadrant");
                assignedOutpost = new Point(n+1,0);
                upperLeft = new Point(n/2 + 1, 0);
                upperRight = new Point(n/2 + 1, n/2);
                lowerRight = new Point(n+1, n/2);
                lowerLeft = new Point(n+1, 0);
                
                x_start = n/2+1;
                x_end   = n;
                y_start = 1;
                y_end   = n/2-1;
                
                break;
                
                /*
            case 4:
                assignedOutpost = new Point(0,0);
                upperLeft = new Point(n/4 + 1, n/4 + 1);
                upperRight = new Point(n/4 + 1, 3*n/4 + 1);
                lowerRight = new Point(3*n/4 + 1, 3*n/4 + 1);
                lowerLeft = new Point(3*n/4 + 1, n/4 + 1);
                
                upperLeftInBounds = new Point(n/4+2, n/4+2);
                upperRightInBounds = new Point(n/4+2 , 3*n/4 );
                lowerRightInBounds = new Point(3*n/4, 3*n/4);
                lowerLeftInBounds = new Point(3*n/4+2, n/4+2);
                
                pointsToReach.add(lowerRightInBounds);
                pointsToReach.add(upperRightInBounds);
                pointsToReach.add(lowerLeftInBounds);
                pointsToReach.add(upperLeftInBounds);
                */                
        }
                
        pointsToReach = generate_points(stride, x_start, x_end, y_start, y_end);
                
        for(int i=this.id%4, j=0; i<s; i+= 4, j++){         
            idInitMapping.put(i, j*(s/scoutsInQuadrant));
        }        
        
        if(scoutsInQuadrant > 1 && this.id > 0) {           
            idx = idInitMapping.get(this.id);   
        }               
        System.out.println("I am player " + this.id + ", my idx is " + idx);
        nextPointToReach = pointsToReach.get(0);                
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

        /*
        if (isEnemyAtGivenPoint(moveX+1, moveY+1, nearbyIds)) {
            // If the player is moving diagonally.
            if (moveX != 0 && moveY != 0) {
                if (!isEnemyAtGivenPoint(moveX+1, 1, nearbyIds)) {
                    moveY = 0;
                }
                else if (!isEnemyAtGivenPoint(1, moveY+1, nearbyIds)){
                    moveX = 0;
                }
            } // The following code is resulting in the player oscillating between 2 diagonal squares.
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
        */

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
        //first_half_points.add(new Point(y_start, x_end));
        second_half_points.add(new Point(x_start, y_end));
        //second_half_points.add(new Point(y_end, x_start));
        
        for(int i=stride; i < (x_end-x_start); i+= stride)
        {
            Point point_a = new Point(x_end,       y_start + i);
            //Point point_a = new Point(y_start + i, x_end);
            Point point_b = new Point(x_end - i,   y_start    );
            //Point point_b = new Point(y_start    , x_end - i);
        
            Point rev_point_a = new Point(x_start + i, y_end    );
            //Point rev_point_a = new Point(y_end    , x_start + i);
            Point rev_point_b = new Point(x_start,     y_end - i);
            //Point rev_point_b = new Point(y_end - i, x_start);
                
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