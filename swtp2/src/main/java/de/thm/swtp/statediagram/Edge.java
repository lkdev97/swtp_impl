package de.thm.swtp.statediagram;

public class Edge {

    private final StateNode state;
    private String trigger;

    public Edge(StateNode state, String trigger) {
        this.state = state;
        this.trigger = trigger;
    }

    public Edge(StateNode state) {
        this.state = state;
    }

    public String getTrigger() {
        return trigger;
    }

    public StateNode getState() {
        return state;
    }

    public String getStringRepresentation() {
        return trigger;
    }
}
