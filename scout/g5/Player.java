package scout.g5;

import scout.sim.*;
import java.util.*;

//-----------------------------------------------------------------------------
// Player's State Machine
//-----------------------------------------------------------------------------

// Default state behaviour.
abstract class State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        System.err.println("move() method has to be overriden.");
        return null;
    }
}

class OrientingState extends State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        // TODO: refactor this conditional logic into new states.
        int x, y;
        if (player.distanceFromXEdge == -1 && player.distanceFromYEdge == -1) {
            x = (player.assignedOutpost.x == 0)? -1 : 1;
            y = (player.assignedOutpost.y == 0)? -1 : 1;
        } else {
            // One diagonal found.
            if (player.distanceFromXEdge == -1) {
                x = (player.assignedOutpost.x == 0)? 1 : -1;
                y = (player.assignedOutpost.y == 0)? -1 : 1;
            } else {
                x = (player.assignedOutpost.x == 0)? -1 : 1;
                y = (player.assignedOutpost.y == 0)? 1 : -1;
            }
        }
        return new Point(x, y);
    }
}

class GoingToLandmarkState extends State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        for(int i = 0 ; i < 3; ++i) for(int j = 0 ; j < 3 ; ++j) {
            if(nearbyIds.get(i).get(j) == null) continue;
            for(String ID : nearbyIds.get(i).get(j)) {
                if(ID.charAt(0) == 'L') {
                    return new Point(i-1, j-1);
                }
            }
        }

        System.err.println("We should never get here.");
        return null;
    }
}

class ExploringState extends State {
    boolean communicatedWithMessenger = false;
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
//        System.out.printf("My position is: %d, %d\n", player.x, player.y);
        // TODO: move this enemy visualization logic somewhere else.
        for(int i = 0 ; i < 3; ++ i) {
            for(int j = 0 ; j < 3 ; ++ j) {
                boolean safe = true;
                if(nearbyIds.get(i).get(j) == null) continue;
                for(String ID : nearbyIds.get(i).get(j)) {
                    if(ID.charAt(0) == 'E') {
                        safe = false;
                    }
                }

                if(player.x != -1) {
                    Point consideredLocation = new Point(player.x + i - 1, player.y + j - 1);
                    if(safe) {
                        if(!player.safeLocations.contains(consideredLocation)) {
                            player.safeLocations.add(consideredLocation);
                        }
                    } else {
                        if(!player.enemyLocations.contains(consideredLocation)) {
                            player.enemyLocations.add(consideredLocation);
                        }
                    }
                }
            }
        }

        if (communicatedWithMessenger) {
            // Explore some more till the end of the game.
            int x = (player.assignedOutpost.x == 0)? 1 : -1;
            int y = (player.assignedOutpost.y == 0)? 1 : -1;
            return new Point(x, y);
        } else {
            // Go meet the messenger.
            int n = player.n;
            int meetingPointX = (player.assignedOutpost.x == 0)? n/4 + 1 : n/4 + n/2 + 1;
            int meetingPointY = (player.assignedOutpost.y == 0)? n/4 + 1 : n/4 + n/2 + 1;
            return player.goToPosition(meetingPointX, meetingPointY);
        }
    }
}

class GoingBackToOutpostState extends State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        int x, y;
        boolean oriented = player.x != -1;
        if (oriented) {
            return player.goToPosition(player.assignedOutpost.x, player.assignedOutpost.y);
        } else {
            x = (player.assignedOutpost.x == 0)? -1 : 1;
            y = (player.assignedOutpost.y == 0)? -1 : 1;

            if (nearbyIds.get(0).get(1) == null || nearbyIds.get(2).get(1) == null) {
                x = 0;
            }
            if (nearbyIds.get(1).get(0) == null || nearbyIds.get(1).get(2) == null) {
                y = 0;
            }
            return new Point(x, y);
        }
    }
}

class DoneState extends State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        // TODO: implement don't move.
        return null;
    }
}

// Communication states. TODO: implement
class CommunicatingState extends State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        for(CellObject obj : concurrentObjects) {
            if (obj instanceof Player) {
                if (((Player) obj).getID() != player.getID()) {
                    player.stub((Player) obj);
                }
            }
        }
        return null;
    }
};
class MovingToMeetingPointState extends State {};
class WaitingForOtherPlayerState extends State {};
class EndingCommunicationState extends State {};

// Although in CS theory they are referred as symbols, events name suits better our current scenario.
abstract class Event {
    // This method is required since attributes are not overriden in subclasses.
    public int getPriority() { return 0; }

    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        System.err.println("isHappening() method has to be overriden.");
        return false;
    }
};

// Null object pattern.
class NoEvent extends Event {
    public int getPriority() { return 0; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        return true;
    }
};

class PlayerSightedEvent extends Event {
    public int getPriority() { return 10; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        return false;
    }
};

class LandmarkSightedEvent extends Event {
    public int getPriority() { return 4; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        for(int i = 0 ; i < 3; ++i) for(int j = 0 ; j < 3 ; ++j) {
            if(nearbyIds.get(i).get(j) == null) continue;
            for(String ID : nearbyIds.get(i).get(j)) {
                if(ID.charAt(0) == 'L') {
                    return true;
                }
            }
        }

        return false;
    }
};

class OrientedEvent extends Event {
    public int getPriority() { return 6; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        if (player.x != -1) return true;

        // Record the location of edges. TODO: refactor to a function.
        if (nearbyIds.get(0).get(0) == null) {
            if (nearbyIds.get(2).get(0) == null) {
                player.xEdgeFound = "top";
                player.distanceFromXEdge = 0;
            }
            if (nearbyIds.get(0).get(2) == null) {
                player.yEdgeFound = "left";
                player.distanceFromYEdge = 0;
            }
        } else if (nearbyIds.get(2).get(2) == null) {
            if (nearbyIds.get(0).get(2) == null) {
                player.xEdgeFound = "bottom";
                player.distanceFromXEdge = 0;
            }
            if (nearbyIds.get(2).get(0) == null) {
                player.yEdgeFound = "right";
                player.distanceFromYEdge = 0;
            }
        }

        // If both edges have been seen, we know where we are.
        if (player.distanceFromXEdge != -1 && player.distanceFromYEdge != -1) {
            if (player.yEdgeFound.equals("left")) {
                player.x = player.distanceFromYEdge;
            } else {
                player.x = (player.n + 2) - player.distanceFromYEdge;
            }

            if (player.xEdgeFound.equals("top")) {
                player.y = player.distanceFromXEdge;
            } else {
                player.y = (player.n + 2) - player.distanceFromXEdge;
            }
            return true;
        }

        for(CellObject obj : concurrentObjects) {
            if (obj instanceof Landmark) {
                player.x = ((Landmark) obj).getLocation().x;
                player.y = ((Landmark) obj).getLocation().y;
                return true;
            }
        }

        return false;
    }
};

class NotOrientedEvent extends Event {
    public int getPriority() { return 1; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        return player.x == -1;
    }
};

class CommunicatedEvent extends Event {
    public int getPriority() { return 4; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        boolean communicated = false;
        for(CellObject obj : concurrentObjects) {
            if (obj instanceof Player) {
                if (((Player) obj).getID() != player.getID()) {
                    player.stub((Player) obj);
                }
                communicated = true;
            }

        }
        return communicated;
    }
};



// This event will trigger when the amounts of turns remaining is roughly the required to get to the closest outpost.
class EndOfMissionEvent extends Event {
    public int getPriority() { return 1000; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        int timeToOutpost, xDistanceToOutpost, yDistanceToOutpost;
        int diagonalDistance, orthogonalDistanceReminder;

        boolean oriented = player.x != -1;
        if (oriented) {
            xDistanceToOutpost = Math.abs(player.assignedOutpost.x - player.x);
            yDistanceToOutpost = Math.abs(player.assignedOutpost.y - player.y);

            diagonalDistance = Math.min(xDistanceToOutpost, yDistanceToOutpost);
            orthogonalDistanceReminder = Math.max(xDistanceToOutpost, yDistanceToOutpost) - diagonalDistance;
            timeToOutpost = diagonalDistance*3 + orthogonalDistanceReminder*2;
        } else {
            // Estimated distance, since we don't know where we are.
            timeToOutpost = player.n * 2;
        }

        int extraTimeToOutpost = (int) (timeToOutpost*0.5);
        int endOfMissionTime = timeToOutpost + extraTimeToOutpost;
        return player.remainingTurns < endOfMissionTime;
    }
};

class OutpostReachedEvent extends Event {
    public int getPriority() { return 10; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        for(CellObject obj : concurrentObjects) {
            if (obj instanceof Outpost) {
                // Exchange information with the outpost.
                for(Point safe : player.safeLocations) {
                    ((Outpost) obj).addSafeLocation(safe);
                }
                for(Point unsafe : player.enemyLocations) {
                    ((Outpost) obj).addEnemyLocation(unsafe);
                }

                return true;
            }
        }
        return false;
    }
};

// Communication events. TODO: implement
class NotInMeetingPointEvent            extends Event {};
class MeetingPointReached               extends Event {};
class OtherPlayerNotInMeetingPointEvent extends Event {};
class OtherPlayerInMeetingPointEvent    extends Event {};
class CommunicationCompletedEvent       extends Event {};
class CommunicationTimeoutEvent         extends Event {};

// Player's Finite State Machine
class PlayerFSM {

    private State orientingState = new OrientingState();
    private State goingToLandmarkState = new GoingToLandmarkState();
    private State exploringState = new ExploringState();
    private State goingBackToOutpostState = new GoingBackToOutpostState();
    private State doneState = new DoneState();

    private State[] states = {orientingState, goingToLandmarkState, exploringState, goingBackToOutpostState, doneState};


    private Event playerSightedEvent = new PlayerSightedEvent();
    private Event landmarkSightedEvent = new LandmarkSightedEvent();
    private Event orientedEvent = new OrientedEvent();
    private Event notOrientedEvent = new NotOrientedEvent();
    private Event endOfMissionEvent = new EndOfMissionEvent();
    private Event outpostReachedEvent = new OutpostReachedEvent();
    private Event communicatedEvent = new CommunicatedEvent();

    private Event[] events = {playerSightedEvent, landmarkSightedEvent, orientedEvent, notOrientedEvent,
            endOfMissionEvent, outpostReachedEvent, communicatedEvent};

    /*
     * This represents the transitions table of the FSM. It use a State class and an Event class, and returns the new State.
     *
     * At implementation level, this is basically a hash with:
     *  - Key: A class that inherits from State (from example, OrientingState)
     *  - Value: Another hash with:
     *     - Key: A class that inherits from Event (from example, PlayerSightedEvent)
     *     - Value: A State object (from example, communicatingState)
     *
     * This can be interpreted as: a Player in OrientingState, that spots another player nearby (PlayerSightedEvent)
     * will move to communicatingState, where it will be supposed to exchange information with him.
     */
    private Map<Class<? extends State>, Map<Class<? extends Event>, State>> transitions;
    protected State currentState;

    public PlayerFSM() {
        currentState = orientingState;

        transitions = new HashMap<Class<? extends State>, Map<Class<? extends Event>, State>>();

        Map<Class<? extends Event>, State> orientingTransitions;
        orientingTransitions = new HashMap<Class<? extends Event>, State>();
        orientingTransitions.put(OrientedEvent.class, exploringState);
        orientingTransitions.put(LandmarkSightedEvent.class, goingToLandmarkState);
        orientingTransitions.put(EndOfMissionEvent.class, goingBackToOutpostState);
        transitions.put(OrientingState.class, orientingTransitions);

        Map<Class<? extends Event>, State> goingToLandmarkTransitions;
        goingToLandmarkTransitions = new HashMap<Class<? extends Event>, State>();
        goingToLandmarkTransitions.put(OrientedEvent.class, exploringState);
        goingToLandmarkTransitions.put(EndOfMissionEvent.class, goingBackToOutpostState);
        transitions.put(GoingToLandmarkState.class, goingToLandmarkTransitions);

        Map<Class<? extends Event>, State> exploringTransitions;
        exploringTransitions = new HashMap<Class<? extends Event>, State>();
        exploringTransitions.put(EndOfMissionEvent.class, goingBackToOutpostState);
        transitions.put(ExploringState.class, exploringTransitions);

        Map<Class<? extends Event>, State> goingBackToOutpostTransitions;
        goingBackToOutpostTransitions = new HashMap<Class<? extends Event>, State>();
        goingBackToOutpostTransitions.put(OutpostReachedEvent.class, doneState);
        transitions.put(GoingBackToOutpostState.class, goingBackToOutpostTransitions);

        Map<Class<? extends Event>, State> doneTransitions;
        doneTransitions = new HashMap<Class<? extends Event>, State>();
        transitions.put(DoneState.class, doneTransitions);
    }

    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                     List<CellObject> concurrentObjects) {
        this.updateState(player, nearbyIds, concurrentObjects);
        return currentState.move(player, nearbyIds, concurrentObjects);
    }

    private void updateState(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                     List<CellObject> concurrentObjects) {
        Event highestPriorityEvent = getHighestPriorityEvent(player, nearbyIds, concurrentObjects);

        Map<Class<? extends Event>, State> currentStateTransitions = transitions.get(currentState.getClass());
        if (currentStateTransitions.containsKey(highestPriorityEvent.getClass())) {
            State newState = currentStateTransitions.get(highestPriorityEvent.getClass());
            System.out.printf("[%d] Moving from %s to %s, because of %s\n", player.id, currentState.getClass().getSimpleName(),
                    newState.getClass().getSimpleName(), highestPriorityEvent.getClass().getSimpleName());
            this.currentState = newState;
        }
    }

    private Event getHighestPriorityEvent(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                                          List<CellObject> concurrentObjects) {
        Event highestPriorityEvent = new NoEvent();
        for (Event event : events) {
            if (event.isHappening(player, nearbyIds, concurrentObjects)) {
                if (event.getPriority() > highestPriorityEvent.getPriority()) {
                    highestPriorityEvent = event;
                }
            }
        }
        return highestPriorityEvent;
    }
}

//Read scout.sim.Player for more information!
public class Player extends scout.sim.Player {

    List<Point> enemyLocations;
    List<Point> safeLocations;
    List<Location> unknownLocations;
    Random gen;
    int totalTurns;
    int remainingTurns;
    int n;
    int x = -1, y = -1;
    int dx = 0, dy = 0;
    int seed;
    int id;
    int moveX = 0;
    int moveY = 0;

    PlayerFSM fsm;
    Point assignedOutpost;
    int distanceFromYEdge;
    String yEdgeFound;
    int distanceFromXEdge;
    String xEdgeFound;

    
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
        unknownLocations = new ArrayList<>();
        gen = new Random(seed);
        this.totalTurns = t;
        this.remainingTurns = t;
        this.n = n;

        this.distanceFromYEdge = -1;
        this.distanceFromXEdge = -1;

        this.fsm = new PlayerFSM();

        switch(this.id) {
            case 0:
                assignedOutpost = new Point(0,0);
                break;
            case 1:
                assignedOutpost = new Point(n+2,0);
                break;
            case 2:
                assignedOutpost = new Point(n+2,n+2);
                break;
            case 3:
                assignedOutpost = new Point(0,n+2);
                break;
            case 4:
                // Messenger.
                break;
        }

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
        if (this.id < 4) {
            return this.fsm.move(this, nearbyIds, concurrentObjects);
        }

        //System.out.println("I'm at " + x + " " + y);
        for(CellObject obj : concurrentObjects) {
            if (obj instanceof Player) {
                if(((Player) obj).getID() != getID())   {
                    stub((Player) obj);
                }
            } else if (obj instanceof Enemy) {

            } else if (obj instanceof Landmark && x == -1) {
                x = ((Landmark) obj).getLocation().x;
                y = ((Landmark) obj).getLocation().y;
                unravelData();
            } else if (obj instanceof Outpost) {
                Object data = ((Outpost) obj).getData();

                x = ((Outpost) obj).getLocation().x;
                y = ((Outpost) obj).getLocation().y;
                unravelData();

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

        Point nextPoint = null;

        System.out.printf("Id: %d, x: %d, y: %d\n", id, x, y);
        if(id == 4 && x != -1)  {
            nextPoint = messengerMove();            
        }      
        else {
            nextPoint = moveToOutpost(nearbyIds);   
        }

        if (x == -1) {
            // Store the move made the get to this location along with neighbouring enemies
            // and safe places.
            unknownLocations.add(new Location(moveX, moveY, enemyLocs, safeLocs));
        }

        // System.out.println("id: " + id + " movex: " + moveX + " movey: " + moveY);
        return nextPoint;
    }

    void setX(int move) {
        if (x != -1)
            dx = move;
    }

    void setY(int move) {
        if (y != -1)
            dy = move;
    }

    private Point moveToOutpost(ArrayList<ArrayList<ArrayList<String>>> nearbyIds)    {

        int numPlayerStrategies = 4;

        moveX = 0;
        moveY = 0;
        
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

    Point goToPosition(int xFinal, int yFinal)  {
        
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

        safeLocations = new ArrayList<Point>(unionLocations);


        HashSet<Point> unionEnemies = new HashSet<Point>();
        unionEnemies.addAll(enemyLocations);
        unionEnemies.addAll(player.enemyLocations);

        enemyLocations = new ArrayList<Point>(enemyLocations);
    }

    // Iterate through the data stored till the player determined their current location.
    // Get data about previously unknown locations.
    void unravelData() {
        int prevX = x - moveX;
        int prevY = y - moveY;
        for (int i = unknownLocations.size() - 1; i >= 0; --i) {
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
                if (prevX + p.x < 0 || prevY + p.y < 0) {
                    continue;
                }
                Point safe = new Point(prevX + p.x, prevY + p.y);
                if (!safeLocations.contains(safe)) {
                    safeLocations.add(safe);
                }
            }

            prevX = prevX - loc.moveX;
            prevY = prevY - loc.moveY;
        }
    }

    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        --remainingTurns;
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