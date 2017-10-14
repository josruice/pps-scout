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

// Move made to orient oneself.
class OrientingState extends State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        // Diagonal bouncing.
        // Avoid enemies?
        int dx, dy;
        if (player.distanceFromEdge == -1) {
            dx = (player.assignedOutpost.x == 0)? -1 : 1;
            dy = (player.assignedOutpost.y == 0)? -1 : 1;
            //System.out.printf("Orienting: id: %d, dx: %d, dy: %d.", player.id, dx, dy);
        }
        else {
            if (player.xEdgeFound != null) {
                dx = (player.assignedOutpost.x == 0)? 1 : -1;
                dy = (player.assignedOutpost.y == 0)? -1 : 1;
            }
            else {
                dx = (player.assignedOutpost.x == 0)? -1 : 1;
                dy = (player.assignedOutpost.y == 0)? 1 : -1;
                //System.out.printf("yEdgeFound: id: %d, dx: %d, dy: %d.", player.id, dx, dy);

            }

            player.distanceFromEdge += 1;
        }

        return new Point(dx, dy);
    }
}

// Move made to go to the adjacent landmark.
class GoingToLandmarkState extends State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        for(int i = 0 ; i < 3; ++i) {
            for(int j = 0 ; j < 3 ; ++j) {
                if (nearbyIds.get(i).get(j) == null) continue;

                for (String ID : nearbyIds.get(i).get(j)) {
                    if (ID.charAt(0) == 'L') {
                        return new Point(i - 1, j - 1);
                    }
                }
            }
        }

        System.err.println("We should never get here.");
        return null;
    }
}

// The player is exploring.
class ExploringState extends State {
    Random rand = new Random();

    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        //System.out.printf("Id: %d, My position is: %d, %d\n", player.id, player.x, player.y);
        rand = new Random(player.id);
        return getDiagonalMove(player, nearbyIds);
    }

    private Point getDiagonalMove(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds) {        

        Point move = null;

        List<Point> allLocations = new ArrayList<>(player.safeLocations);
        allLocations.addAll(player.enemyLocations);        
                        
            if(player.x == player.nextPointToReach.x && player.y == player.nextPointToReach.y)  {
                            
                //System.out.println("Point reached: x " + player.x + " y " + player.y);
                int idx = (++player.idx) % (player.pointsToReach.size());
                Point next = player.pointsToReach.get(idx);
                //System.out.println("x:" + next.x + " y: " + next.y);
                //System.out.println(player.idx + " -> " + idx + " size: " + player.pointsToReach.size() + " x:" + next.x + "y: " + next.y);
                //System.out.println("0 x: " + player.pointsToReach.get(0).x + " 0 y: " + player.pointsToReach.get(0).y);
                player.nextPointToReach.x = next.x;
                player.nextPointToReach.y = next.y;                                                                                                    
            }            
            
            move = player.goToPosition(player.nextPointToReach.x, player.nextPointToReach.y,
                    nearbyIds);
 


        return move;
    }

    private boolean isMovePossible(Player player, Point p, List<Point> allLocations) {
        return /*!allLocations.contains(p) &&*/ isWithinQuadrant(player, p);
    }

    private boolean isWithinQuadrant(Player player, Point p) {
        return p.x <= player.lowerLeft.x && p.x >= player.upperLeft.x
                && p.y <= player.lowerRight.y && p.y >= player.lowerLeft.y;
    }
}

// Go to center of your quadrant.
class MovingTowardsCenterState extends State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        return player.goToPosition(player.upperRight.x + player.n/4,
                player.upperLeft.y + player.n/4, nearbyIds);
    }
}

// Go to communicate.
class GoingToCommunicateState extends State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        return player.goToPosition((int) player.n/2, (int) player.n/2, nearbyIds);
    }
}

// Go to outpost.
class GoingBackToOutpostState extends State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        int dx, dy;
        boolean oriented = player.x != -1;
        if (oriented) {
            return player.goToPosition(player.assignedOutpost.x, player.assignedOutpost.y, nearbyIds);
        } else {
            dx = (player.assignedOutpost.x == 0)? -1 : 1;
            dy = (player.assignedOutpost.y == 0)? -1 : 1;

            if (nearbyIds.get(0).get(1) == null || nearbyIds.get(2).get(1) == null) {
                dx = 0;
            }
            if (nearbyIds.get(1).get(0) == null || nearbyIds.get(1).get(2) == null) {
                dy = 0;
            }

            return new Point(dx, dy);
        }
    }
}

// Reached the outpost. Time is almost up. Don't move.
class DoneState extends State {
    public Point move(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                      List<CellObject> concurrentObjects) {
        return new Point(0, 0);
    }
}

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

// Check if there is a neighbouring landmark.
class LandmarkSightedEvent extends Event {
    public int getPriority() { return 4; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        for(int i = 0 ; i < 3; ++i) {
            for(int j = 0 ; j < 3 ; ++j) {
                if (nearbyIds.get(i).get(j) == null) continue;
                for (String ID : nearbyIds.get(i).get(j)) {
                    if (ID.charAt(0) == 'L') {
                        return true;
                    }
                }
            }
        }

        return false;
    }
};

// Check if the player is oriented.
class OrientedEvent extends Event {
    public int getPriority() { return 6; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        if (player.x != -1) return true;

        for(CellObject obj : concurrentObjects) {
            if (obj instanceof Landmark) {
                player.x = ((Landmark) obj).getLocation().x;
                player.y = ((Landmark) obj).getLocation().y;
                //System.out.printf("Landmark Id: %d, x: %d, y: %d.", player.id, player.x, player.y);
                return true;
            }
            if (obj instanceof Outpost) {
                player.x = ((Outpost) obj).getLocation().x;
                player.y = ((Outpost) obj).getLocation().y;
                return true;
            }
        }

        // Record the location of edges. TODO: refactor to a function.
        if (nearbyIds.get(0).get(0) == null) {
            if (nearbyIds.get(0).get(2) == null) {
                player.xEdgeFound = "top";
            }
            if (nearbyIds.get(2).get(0) == null) {
                player.yEdgeFound = "left";
            }
        } else if (nearbyIds.get(2).get(2) == null) {
            if (nearbyIds.get(2).get(0) == null) {
                player.xEdgeFound = "bottom";
            }
            if (nearbyIds.get(0).get(2) == null) {
                player.yEdgeFound = "right";
            }
        }

        if ((player.xEdgeFound == null ^ player.yEdgeFound == null)
                && player.distanceFromEdge == -1) {
            player.distanceFromEdge = 0;
        }

        // If both edges have been seen, we know where we are.
        if (player.xEdgeFound != null && player.yEdgeFound != null) {
            if (nearbyIds.get(1).get(0) == null || nearbyIds.get(1).get(2) == null) {
                if (nearbyIds.get(1).get(0) == null) {
                    player.y = 0;
                }
                else {
                    player.y = player.n + 1;
                }

                if (player.xEdgeFound.equals("top")) {
                    player.x = player.distanceFromEdge;

                }
                else {
                    player.x = (player.n + 1) - player.distanceFromEdge;
                }
            } else {
                if (nearbyIds.get(0).get(1) == null) {
                    player.x = 0;
                }
                else {
                    player.x = player.n + 1;
                }

                if (player.yEdgeFound.equals("left")) {
                    player.y = player.distanceFromEdge;
                }
                else {
                    player.y = (player.n + 1) - player.distanceFromEdge;
                }
            }

            return true;
        }

        return false;
    }
};

// The player is not yet oriented.
class NotOrientedEvent extends Event {
    public int getPriority() { return 1; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        return player.x == -1;
    }
};

// The player has reached their quadrant.
class QuadrantReachedEvent extends Event {
    public int getPriority() { return 8; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        //System.out.printf("Id: %d, My quadrant position is: %d, %d\n", player.id, player.x, player.y);
        return player.x != -1 && isWithinQuadrant(player);
    }

    private boolean isWithinQuadrant(Player player) {
        return player.x <= player.lowerLeft.x && player.x >= player.upperLeft.x
                && player.y <= player.lowerRight.y && player.y >= player.lowerLeft.y;
    }
};

// The players need to go to communicate.
class CommunicateEvent extends Event {
    public int getPriority() { return 100; }

    @Override
    public boolean isHappening(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                               List<CellObject> concurrentObjects) {
        int middleP = (int) player.n/2;

        int xDistanceToMiddle = Math.abs(middleP - player.x);
        int yDistanceToMiddle = Math.abs(middleP - player.y);

        int xDistanceToOutpost = Math.abs(player.assignedOutpost.x - middleP);
        int yDistanceToOutpost = Math.abs(player.assignedOutpost.y - middleP);

        int diagonalDistance = Math.min(xDistanceToOutpost, yDistanceToOutpost) +
                Math.min(xDistanceToMiddle, yDistanceToMiddle);
        int orthogonalDistanceReminder = Math.max(xDistanceToOutpost, yDistanceToOutpost) +
                Math.max(xDistanceToMiddle, yDistanceToMiddle) - diagonalDistance;
        int timeToOutpost = diagonalDistance*3 + orthogonalDistanceReminder*2;

        int extraTimeToOutpost = (int) (timeToOutpost*0.5);
        int endOfMissionTime = timeToOutpost + extraTimeToOutpost;

        return player.remainingTurns < endOfMissionTime;
    }
};


// This event will trigger when the amounts of turns remaining is roughly the required to
// get to the closest outpost.
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
                // The game is ending and the player has reached the outpost without knowing
                // their location.
                if (player.x == -1) {
                    player.x = ((Outpost) obj).getLocation().x;
                    player.y = ((Outpost) obj).getLocation().y;
                    player.unravelData();
                }

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

class PlayerFSM {

    private State orientingState = new OrientingState();
    private State goingToLandmarkState = new GoingToLandmarkState();
    private State exploringState = new ExploringState();
    private State goingBackToOutpostState = new GoingBackToOutpostState();
    private State doneState = new DoneState();
    private State movingToQuadrant = new MovingTowardsCenterState();
    private State goingToCommunicate = new GoingToCommunicateState();

    private State[] states = {orientingState, goingToLandmarkState, exploringState, goingBackToOutpostState,
            doneState};

    private Event landmarkSightedEvent = new LandmarkSightedEvent();
    private Event orientedEvent = new OrientedEvent();
    private Event endOfMissionEvent = new EndOfMissionEvent();
    private Event outpostReachedEvent = new OutpostReachedEvent();
    private Event quadrantReachedEvent = new QuadrantReachedEvent();
    private Event goToCommunicateEvent = new CommunicateEvent();

    private Event[] events = {landmarkSightedEvent, orientedEvent, endOfMissionEvent,
            outpostReachedEvent, quadrantReachedEvent, goToCommunicateEvent};

    //
    // This represents the transitions table of the FSM. It use a State class and an Event class, and
    // returns the new State.
    //
    // At implementation level, this is basically a hash with:
    //  - Key: A class that inherits from State (from example, OrientingState)
    //  - Value: Another hash with:
    //     - Key: A class that inherits from Event (from example, PlayerSightedEvent)
    //     - Value: A State object (from example, communicatingState)
    //
    // This can be interpreted as: a Player in OrientingState, that spots another player nearby
    // (PlayerSightedEvent) will move to communicatingState, where it will be supposed to exchange
    // information with him.
    //
    private Map<Class<? extends State>, Map<Class<? extends Event>, State>> transitions;
    protected State currentState;

    public PlayerFSM() {
        currentState = orientingState;

        transitions = new HashMap<Class<? extends State>, Map<Class<? extends Event>, State>>();

        Map<Class<? extends Event>, State> orientingTransitions;
        orientingTransitions = new HashMap<Class<? extends Event>, State>();
        orientingTransitions.put(OrientedEvent.class, movingToQuadrant);
        orientingTransitions.put(LandmarkSightedEvent.class, goingToLandmarkState);
        orientingTransitions.put(EndOfMissionEvent.class, goingBackToOutpostState);
        transitions.put(OrientingState.class, orientingTransitions);

        Map<Class<? extends Event>, State> goingToLandmarkTransitions;
        goingToLandmarkTransitions = new HashMap<Class<? extends Event>, State>();
        goingToLandmarkTransitions.put(OrientedEvent.class, movingToQuadrant);
        goingToLandmarkTransitions.put(EndOfMissionEvent.class, goingBackToOutpostState);
        transitions.put(GoingToLandmarkState.class, goingToLandmarkTransitions);

        Map<Class<?extends Event>, State> movingToQuadrant;
        movingToQuadrant = new HashMap<Class<?extends Event>, State>();
        movingToQuadrant.put(QuadrantReachedEvent.class, exploringState);
        movingToQuadrant.put(EndOfMissionEvent.class, goingBackToOutpostState);
        transitions.put(MovingTowardsCenterState.class, movingToQuadrant);

        Map<Class<? extends Event>, State> exploringTransitions;
        exploringTransitions = new HashMap<Class<? extends Event>, State>();
        exploringTransitions.put(CommunicateEvent.class, goingToCommunicate);
        exploringTransitions.put(EndOfMissionEvent.class, goingBackToOutpostState);
        transitions.put(ExploringState.class, exploringTransitions);

        Map<Class<? extends Event>, State> communicatingTransitions;
        communicatingTransitions = new HashMap<Class<? extends Event>, State>();
        communicatingTransitions.put(EndOfMissionEvent.class, goingBackToOutpostState);
        transitions.put(GoingToCommunicateState.class, communicatingTransitions);

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
        Map<Class<? extends Event>, State> currentStateTransitions = transitions.get(currentState.getClass());
        Event highestPriorityEvent = getHighestPriorityEvent(player, nearbyIds, concurrentObjects, currentStateTransitions);

        if (currentStateTransitions.containsKey(highestPriorityEvent.getClass())) {
            State newState = currentStateTransitions.get(highestPriorityEvent.getClass());
            System.out.printf("[%d] Moving from %s to %s, because of %s\n",
                    player.id, currentState.getClass().getSimpleName(),
                    newState.getClass().getSimpleName(), highestPriorityEvent.getClass().getSimpleName());
            this.currentState = newState;
        }
    }

    private Event getHighestPriorityEvent(Player player, ArrayList<ArrayList<ArrayList<String>>> nearbyIds,
                                          List<CellObject> concurrentObjects,
                                          Map<Class<? extends Event>, State> currentStateTransitions) {
        Event highestPriorityEvent = new NoEvent();
        for (Event event : events) {
            if (currentStateTransitions.containsKey(event.getClass())) {
                if (event.isHappening(player, nearbyIds, concurrentObjects)) {
                    if (event.getPriority() > highestPriorityEvent.getPriority()) {
                        highestPriorityEvent = event;
                    }
                }
            }
        }
        return highestPriorityEvent;
    }
}