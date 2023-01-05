package de.thm.swtp.statediagram;

import java.util.ArrayList;

/**
 * Represents a state of a state machine.
 */
public class StateNode extends Node {

    // The state's unique ID. Automatically assigned on instantiation.
    protected int id;
    // Whether this state is the start or end state of a state machine. Used to represent it differently
    // in the generated state diagram code.
    private final boolean startState;
    private final boolean endState;
    // List of all edges this state has to other states.
    private final ArrayList<Edge> edges = new ArrayList<>();

    // Holds next state ID. Increased in constructor.
    protected static int stateId = 0;

    /**
     * Creates a new state node.
     */
    public StateNode() {
        this(false, false);
    }

    /**
     * Creates a new state node.
     * @param startState Whether this state is a start state.
     * @param endState Whether this state is an end state.
     */
    public StateNode(boolean startState, boolean endState) {
        this.id = stateId++;
        this.startState = startState;
        this.endState = endState;
    }

    /**
     * Adds an edge to another state node with a specified trigger.
     * @param trigger The trigger on which to switch to the given state.
     * @param node The other state.
     */
    public void addEdge(String trigger, StateNode node) {
        edges.add(new Edge(node, trigger));
    }

    /**
     * Adds an empty edge to another state node. They don't require an input (epsilon).
     * @param node The other state.
     */
    public void addEmptyEdge(StateNode node) {
        edges.add(new Edge(node));
    }

    /**
     * Returns whether this state is a start state.
     * @return Whether this state is a start state.
     */
    public boolean isStartState() {
        return startState;
    }

    /**
     * Returns whether this state is an end state.
     * @return Whether this state is an end state.
     */
    public boolean isEndState() {
        return endState;
    }

    /**
     * Generates string representation of a state transition from this state to another state as expected by PlantUML.
     * @param state The state to generate a transition to.
     * @return The StringBuilder the transition was writen to.
     */
    private StringBuilder generateStateHead(StateNode state) {
        var s = new StringBuilder();

        if (isStartState()) {
            s.append("[*]");
        } else {
            s.append(String.format("S%s", id));
        }
        s.append(" --> ");
        if (state.isEndState()) {
            s.append("[*]");
        } else {
            s.append(String.format("S%s", state.id));
        }

        return s;
    }

    /**
     * Generates a state transition without a trigger.
     * @param state The state to generate a transition to.
     * @return The string representation of this transition.
     */
    protected String generateState(StateNode state) {
        var s = generateStateHead(state);
        s.append("\n");

        return s.toString();
    }

    /**
     * Generates a state transition with a trigger.
     * @param state The state to generate a transition to.
     * @param trigger The trigger which causes this transition.
     * @return The string representation of this transition.
     */
    protected String generateState(StateNode state, String trigger) {
        var s = generateStateHead(state);
        s.append(": ");
        s.append(trigger);
        s.append("\n");

        return s.toString();
    }

    /**
     * Generates a string representation as expected by PlantUML of all states including and following this state.
     * @return The PlantUML string representation.
     */
    @Override
    public String toString() {
        var s = new StringBuilder();

        for (var e : edges) {
            var representation = e.getStringRepresentation();
            if (representation != null) {
                s.append(generateState(e.getState(), representation));
            } else {
                s.append(generateState(e.getState()));
            }
            s.append(e.getState());
        }

        return s.toString();
    }
}
