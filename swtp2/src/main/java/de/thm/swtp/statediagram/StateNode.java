package de.thm.swtp.statediagram;

import java.util.ArrayList;

public class StateNode extends Node {

    final int id;
    private final boolean startState;
    private boolean endState;
    private final ArrayList<Edge> edges = new ArrayList<>();
    private final ArrayList<StateNode> stateNodes = new ArrayList<>();

    private static int stateId = 0;

    public StateNode() {
        this(false, false);
    }

    public StateNode(boolean startState, boolean endState) {
        this.id = stateId++;
        this.startState = startState;
        this.endState = endState;
    }

    public void addEdge(String trigger, StateNode node) {
        edges.add(new Edge(node, trigger));
    }

    public void addEmptyEdge(StateNode node) {
        edges.add(new Edge(node));
    }

    public boolean isStartState() {
        return startState;
    }

    public boolean isEndState() {
        return endState;
    }

    public void setEndState(boolean endState) {
        this.endState = endState;
    }

    public int getId() {
        return id;
    }

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

    protected String generateState(StateNode state) {
        var s = generateStateHead(state);
        s.append("\n");

        return s.toString();
    }

    protected String generateState(StateNode state, String trigger) {
        var s = generateStateHead(state);
        s.append(": ");
        s.append(trigger);
        s.append("\n");

        return s.toString();
    }

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

        for (var n : stateNodes) {
            s.append(n);
        }

        return s.toString();
    }
}
