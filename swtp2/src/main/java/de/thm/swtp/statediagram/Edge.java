package de.thm.swtp.statediagram;

/**
 * Represents an edge between two states (a transition).
 */
public class Edge {

    private final StateNode state;
    private String trigger;

    /**
     * Creates a new edge to the given state with a given trigger.
     * @param state The state to create an edge to.
     * @param trigger The trigger on which to switch to the given state.
     */
    public Edge(StateNode state, String trigger) {
        this.state = state;
        this.trigger = trigger;
    }

    /**
     * Creates a new edge to a given state without a trigger (epsilon).
     * @param state The state to create an edge to.
     */
    public Edge(StateNode state) {
        this.state = state;
    }

    /**
     * Returns the trigger of this edge.
     * @return The trigger-
     */
    public String getTrigger() {
        return trigger;
    }

    /**
     * Returns the state this edge points to.
     * @return The state.
     */
    public StateNode getState() {
        return state;
    }

    /**
     * Returns a string representation of this edge.
     * @return The string representation.
     */
    public String getStringRepresentation() {
        return trigger;
    }
}
