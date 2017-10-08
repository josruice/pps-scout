package scout.g5;

import scout.sim.*;

import java.util.*;


//Read scout.sim.Player for more information!
public class Player extends scout.sim.Player {
    List<Point> enemyLocations;
    List<Point> safeLocations;
    Random gen;
    int t,n;
    int x = -1;
    int y = -1;
    int dx = 0, dy = 0;
    int seed;
    int id;
    
    int state = -1;
    int waitTurns = 5;  
    Point upperLeft = null;
    Point upperRight = null;
    Point lowerLeft = null;
    Point lowerRight = null;
    
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
        gen = new Random(seed);
        this.t = t;
        this.n = n;
        this.upperLeft = new Point(n/4 + 1, n/4 + 1);
        this.upperRight = new Point(n/4 + 1, n/4 + n/2 + 1);
        this.lowerRight = new Point(n/4 + n/2 + 1, n/4 + n/2 + 1);
        this.lowerLeft = new Point(n/4 + n/2 + 1, n/4 + 1);
                  
    }

    /**
     * nearby IDs is a 3 x 3 grid of nearby IDs with you in the center (1,1) position. A position is null if it is off the board.
     * Enemy IDs start with 'E', Player start with 'P', Outpost with 'O' and landmark with 'L'.
     *
     */
    @Override
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        //System.out.println("I'm " + id + " and I'm at " + x + " " + y);
        for(int i = 0 ; i < 3; ++ i) {
            for(int j = 0 ; j < 3 ; ++ j) {
                boolean safe = true;
                if(nearbyIds.get(i).get(j) == null) continue;
                for(String ID : nearbyIds.get(i).get(j)) {
                    if(ID.charAt(0) == 'E') {
                        safe = false;
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
        for(CellObject obj : concurrentObjects) {
            if (obj instanceof Player) {
                //communicate using custom methods?
                //((Player) obj).stub();                
                if(((Player) obj).getID() != getID())   {
                    stub((Player) obj); 
                }               
                               
            } else if (obj instanceof Enemy) {

            } else if (obj instanceof Landmark) {
                x = ((Landmark) obj).getLocation().x;
                y = ((Landmark) obj).getLocation().y;
            } else if (obj instanceof Outpost) {
                x = ((Outpost) obj).getLocation().x;
                y = ((Outpost) obj).getLocation().y;
                
                Object data = ((Outpost) obj).getData();
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
        
        Point nextPoint = null;
        
        if(id == 4 && x != -1)  {
            nextPoint = messengerMove();            
        }      
        else {
            nextPoint = moveToOutpost(nearbyIds);   
        }
        

        // System.out.println("id: " + id + " movex: " + moveX + " movey: " + moveY);
        return nextPoint;
    }

    private void setX(int move) {
        if (x != -1)
            dx = move;
    }

    private void setY(int move) {
        if (y != -1)
            dy = move;
    }

    private Point moveToOutpost(ArrayList<ArrayList<ArrayList<String>>> nearbyIds)    {
        
        int moveX = 0, moveY = 0;
        int numPlayerStrategies = 4;
        
        if (id % numPlayerStrategies == 0 || id % numPlayerStrategies == 1) {
            if (nearbyIds.get(0).get(1) != null) {
                moveX = -1;
                setX(moveX);
            }
            if (id % numPlayerStrategies == 0) {
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
            if (id % numPlayerStrategies == 2) {
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
        
        return new Point(moveX, moveY);
    }

    private Point messengerMove()   {
        
            
        Point nextPoint = new Point(0, 0);              
        
        if(state == -1) {  
            if(x == upperLeft.x && y == upperLeft.y) {                          
                waitTurns--;
                if(waitTurns == 0)  {
                    state = 0;
                    waitTurns = 5;
                }                   
            } else {
                nextPoint = goToPosition(upperLeft.x, upperLeft.y); 
            }                                   
        } else if(state == 0)   {                       
            if(x == upperRight.x && y == upperRight.y) {                                                
                waitTurns--;
                if(waitTurns == 0)  {
                    state = 1;
                    waitTurns = 5;
                }           
            }   else {
                nextPoint = goToPosition(upperRight.x, upperRight.y);   
            }                       
            
        } else if(state == 1)   {
            if(x == lowerRight.x && y == lowerRight.y) {                                
                waitTurns--;
                if(waitTurns == 0)  {
                    state = 2;
                    waitTurns = 5;
                }           
            } else  {
                nextPoint = goToPosition(lowerRight.x, lowerRight.y);   
            }           
                        
        } else if(state == 2)   {
            if(x == lowerLeft.x && y == lowerLeft.y) {              
                waitTurns--;
                if(waitTurns == 0)  {
                    state = -1;
                    waitTurns = 5;
                }           
            } else  {
                nextPoint = goToPosition(lowerLeft.x, lowerLeft.y); 
            }                           
        } 
        
        
        return nextPoint;
    }

    private Point goToPosition(int xFinal, int yFinal)  {
        
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
        
        setX(moveX);
        setY(moveY);
        return new Point(moveX, moveY);
        
    }
    
    public void stub(Player player) {               
        
        HashSet<Point> unionLocations = new HashSet<Point>();
        unionLocations.addAll(safeLocations);
        unionLocations.addAll(player.safeLocations);
        
        player.safeLocations = new ArrayList<Point>(unionLocations);
        safeLocations = new ArrayList<Point>(unionLocations);
        
        
        HashSet<Point> unionEnemies = new HashSet<Point>();
        unionEnemies.addAll(enemyLocations);
        unionEnemies.addAll(player.enemyLocations);
                
        player.enemyLocations = new ArrayList<Point>(enemyLocations);
        enemyLocations = new ArrayList<Point>(enemyLocations);
                
    }

    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        --t;
        //System.out.println("communicate");
    }

    @Override
    public void moveFinished() {
        x += dx;
        y += dy;
        dx = dy = 0;
    }
}
