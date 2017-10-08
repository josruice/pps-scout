package scout.g5;

import scout.sim.*;
import java.util.*;

//-----------------------------------------------------------------------------
// Player's State Machine
//-----------------------------------------------------------------------------

// Default state behaviour.
abstract class State {
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        System.err.println("move() method has to be overriden.");
        return null;
    }
}

class OrientingState extends State {
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        return null;
    }
}

class ExploringState extends State {
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        return null;
    }
}

class GoingBackToOutposState extends State {
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        return null;
    }
}

class DoneState extends State {
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        // TODO: implement don't move.
        return null;
    }
}

// Communication states. TODO: implement
class CommunicatingState extends State {};
class MovingToMeetingPointState extends State {};
class WaitingForOtherPlayerState extends State {};
class EndingCommunicationState extends State {};

// Although in CS theory they are referred as symbols, events name suits better our current scenario.
abstract class Event {
    final int priority = 0;
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        System.err.println("isHappening() method has to be overriden.");
        return false;
    }

    // This method is required since attributes are not overriden in subclasses.
    public int getPriority() {
        System.err.println("getPriority() method has to be overriden.");
        return this.priority;
    }
};

// Null object pattern.
class NoEvent extends Event {
    final int priority = 0;
    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        return true;
    }
};

class PlayerSightedEvent extends Event {
    final int priority = 5;
    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        return false;
    }
};

class OrientedEvent extends Event {
    final int priority = 3;
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        return false;
    }
};

class NotOrientedEvent extends Event {
    final int priority = 1;
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        return false;
    }
};

// This event will trigger when the amounts of turns remaining is roughly the required to get to the closest outpost.
class EndOfMissionEvent extends Event {
    final int priority = 1000;
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
//        int timeToOutpost = n*n; // Initialize to unreasonably high number.
//        if (oriented) {
//
//        } else {
//
//        }
//
//        int extraTimeToOutpost = (int) (timeToOutpost*1.1);
//        int endOfMissionTime = timeToOutpost + extraTimeToOutpost;
//        boolean isEndOfMission = remainingTurns < endOfMissionTime;
//        if (isEndOfMission) {
//            this.currentState = States.END_OF_MISSION;
//        }
        return false;
    }
};

class OutpostReachedEvent extends Event {
    final int priority = 10;
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
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
    private State exploringState = new ExploringState();
    private State goingBackToOutposState = new GoingBackToOutposState();
    private State doneState = new DoneState();

    private State[] states = {orientingState, exploringState, goingBackToOutposState, doneState};


    private Event playerSightedEvent = new PlayerSightedEvent();
    private Event orientedEvent = new OrientedEvent();
    private Event notOrientedEvent = new NotOrientedEvent();
    private Event endOfMissionEvent = new EndOfMissionEvent();
    private Event outpostReachedEvent = new OutpostReachedEvent();

    private Event[] events = {playerSightedEvent, orientedEvent, notOrientedEvent, endOfMissionEvent, outpostReachedEvent};

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
        orientingTransitions.put(EndOfMissionEvent.class, goingBackToOutposState);
        transitions.put(OrientingState.class, orientingTransitions);

        Map<Class<? extends Event>, State> exploringTransitions;
        exploringTransitions = new HashMap<Class<? extends Event>, State>();
        exploringTransitions.put(EndOfMissionEvent.class, goingBackToOutposState);
        transitions.put(ExploringState.class, exploringTransitions);

        Map<Class<? extends Event>, State> goingBackToOutpostTransitions;
        goingBackToOutpostTransitions = new HashMap<Class<? extends Event>, State>();
        goingBackToOutpostTransitions.put(OutpostReachedEvent.class, doneState);
        transitions.put(GoingBackToOutposState.class, goingBackToOutpostTransitions);
    }

    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                     List<CellObject> concurrentObjects) {
        this.updateState(player, nearbyIds, concurrentObjects);
        return currentState.move(nearbyIds, concurrentObjects);
    }

    private void updateState(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                     List<CellObject> concurrentObjects) {
        Event highestPriorityEvent = getHighestPriorityEvent(player, nearbyIds, concurrentObjects);

        Map<Class<? extends Event>, State> currentStateTransitions = transitions.get(currentState.getClass());
        if (currentStateTransitions.containsKey(highestPriorityEvent.getClass())) {
            this.currentState = currentStateTransitions.get(highestPriorityEvent.getClass());
        }
    }

    private Event getHighestPriorityEvent(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                                          List<CellObject> concurrentObjects) {
        Event highestPriorityEvent = new NoEvent();
        for (Event event : events) {
            if (event.isHappening(player, nearbyIds, concurrentObjects)) {
                if (event.priority > highestPriorityEvent.getPriority()) {
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
    Random gen;
    int totalTurns;
    int remainingTurns;
    int n;
    int x = -1, y = -1;
    int dx = 0, dy = 0;
    int seed;
    int id;
    PlayerFSM fsm;
    boolean oriented;  // Determines if the player knows his locations or not.

    /**
     * better to use init instead of constructor, don't modify ID or simulator will error
     */
    public Player(int id) {
        super(id);
        seed=id;
        this.id = id;
        this.fsm = new PlayerFSM();
    }

    /**
     *   Called at the start
     */
    @Override
    public void init(String id, int s, int n, int t, List<Point> landmarkLocations) {
        enemyLocations = new ArrayList<>();
        safeLocations = new ArrayList<>();
        gen = new Random(seed);
        this.totalTurns = t;
        this.remainingTurns = t;
        this.n = n;
    }

    /**
     * nearby IDs is a 3 x 3 grid of nearby IDs with you in the center (1,1) position. A position is null if it is off the board.
     * Enemy IDs start with 'E', Player start with 'P', Outpost with 'O' and landmark with 'L'.
     *
     */
    @Override
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        this.fsm.move(this, nearbyIds, concurrentObjects);


        //System.out.println("I'm at " + x + " " + y);
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
                ((Player) obj).stub();
            } else if (obj instanceof Enemy) {

            } else if (obj instanceof Landmark) {
                x = ((Landmark) obj).getLocation().x;
                y = ((Landmark) obj).getLocation().y;
            } else if (obj instanceof Outpost) {
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


        int moveX = 0, moveY = 0;

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

        // System.out.println("id: " + id + " movex: " + moveX + " movey: " + moveY);
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

    public void stub() {
        ;
    }

    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        --remainingTurns;
        System.out.println("communicate");
    }

    @Override
    public void moveFinished() {
        x += dx;
        y += dy;
        dx = dy = 0;
    }
}
