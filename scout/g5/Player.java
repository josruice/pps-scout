package scout.g5;

import scout.sim.*;

import java.util.*;


//Read scout.sim.Player for more information!
public class Player extends scout.sim.Player {
    List<Point> enemyLocations;
    List<Point> safeLocations;
    List<Location> unknownLocations;
    Random gen;
    int t,n;
    int x = -1;
    int y = -1;
    int dx = 0, dy = 0;
    int seed;
    int id;
    int moveX = 0;
    int moveY = 0;

    /**
     * better to use init instead of constructor, don't modify ID or simulator will error
     */
    public Player(int id) {
        super(id);
        seed=id;
        this.id = id;
    }

    /**
     *   Called at the start
     */
    @Override
    public void init(String id, int s, int n, int t, List<Point> landmarkLocations) {
        enemyLocations = new ArrayList<>();
        safeLocations = new ArrayList<>();
        unknownLocations = new ArrayList<>();
        gen = new Random(seed);
        this.t = t;
        this.n = n;
    }

    /**
     * nearby IDs is a 3 x 3 grid of nearby IDs with you in the center (1,1) position. A position is null if it is off the board.
     * Enemy IDs start with 'E', Player start with 'P', Outpost with 'O' and landmark with 'L'.
     *
     */
    @Override
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        //System.out.println("I'm at " + x + " " + y);
        for(CellObject obj : concurrentObjects) {
            if (obj instanceof Player) {
                //communicate using custom methods?
                ((Player) obj).stub();
            } else if (obj instanceof Enemy) {

            } else if (obj instanceof Landmark && x == -1) {
                x = ((Landmark) obj).getLocation().x;
                y = ((Landmark) obj).getLocation().y;
                unravelData();
            } else if (obj instanceof Outpost) {
                Object data = ((Outpost) obj).getData();
                if (x == -1) {
                    Point loc = ((Outpost) obj).getLocation();
                    x = loc.x;
                    y = loc.y;
                    unravelData();
                }
                if(data == null) {
                    ((Outpost) obj).setData((Object)"yay!!");
                }
                for(Point safe : safeLocations) {
                    ((Outpost) obj).addSafeLocation(safe);
                }
                for(Point unsafe : enemyLocations) {
                    ((Outpost) obj).addEnemyLocation(unsafe);
                }
            }
        }

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
                    }
                    else {
                        safeLocs.add(new Point(i - 1, j - 1));
                    }
                }

                if(x != -1) {
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
            // Store the move made the get to this location along with neighbouring enemies
            // and safe places.
            unknownLocations.add(new Location(moveX, moveY, enemyLocs, safeLocs));
        }

        moveX = 0;
        moveY = 0;

        if (id % 4 == 0 || id % 4 == 1) {
            if (nearbyIds.get(0).get(1) != null) {
                moveX = -1;
                setX(moveX);
            }
            if (id % 4 == 0) {
                if (nearbyIds.get(1).get(0) != null) {
                    moveY = -1;
                    setY(moveY);
                }
            }
            else {
                if (nearbyIds.get(1).get(2) != null) {
                    moveY = 1;
                    setY(moveY);
                }
            }
        }
        else {
            if (nearbyIds.get(2).get(1) != null) {
                moveX = 1;
                setX(moveX);
            }
            if (id % 4 == 2) {
                if (nearbyIds.get(1).get(0) != null) {
                    moveY = -1;
                    setY(moveY);
                }
            }
            else {
                if (nearbyIds.get(1).get(2) != null) {
                    moveY = 1;
                    setY(moveY);
                }
            }
        }

        if (x != -1) {
            System.out.println("Id: " + id + " x: " + x + " y: " + y);
        }
        return new Point(moveX, moveY);
    }

    private void setX(int move) {
        if (x != -1)
            dx = move;
    }

    private void setY(int move) {
        if (y != -1)
            dy = move;
    }

    // Iterate through the data stored till the player determined their current location.
    // Get data about previously unknown locations.
    private void unravelData() {
        int prevX = x - moveX;
        int prevY = y - moveY;
        for (int i = unknownLocations.size() - 1; i > 0; --i) {
            Location loc = unknownLocations.get(i);
            System.out.println("Id: " + id + " pos: " + prevX + " " + prevY);
            for (Point p : loc.enemyLocations) {
                System.out.println("Id: " + id + " Enemy pos: " + (prevX + p.x) + " " + (prevY + p.y));
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

            prevX = prevX - loc.moveX;
            prevY = prevY - loc.moveY;
        }
    }

    public void stub() {
        ;
    }

    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        --t;
        System.out.println("communicate");
    }

    @Override
    public void moveFinished() {
        if (x != -1 && y != -1) {
            x += dx;
            y += dy;
        }

        dx = dy = 0;
    }
}

// Store details of a particular location.
class Location {
    // The move made to reach this point.
    public int moveX;
    public int moveY;

    // The list of neighbouring enemies with respect to this location.
    public List<Point> enemyLocations;

    // The list of neighbouring safe locations with respect to this location.
    public List<Point> safeLocations;

    public Location(int moveX, int moveY, List<Point> enemyLoc, List<Point> safeLoc) {
        this.moveX = moveX;
        this.moveY = moveY;
        this.enemyLocations = enemyLoc;
        this.safeLocations = safeLoc;
    }
}