package de.thm.swtp.statediagram;

import java.util.ArrayList;
import java.util.HashMap;

public class StateNode extends Node {

    final int id;
    private final boolean startState;
    private boolean endState;

    public StateNode(int id) {
        this(id, false, false);
    }

    public StateNode(int id, boolean startState, boolean endState) {
        this.id = id;
        this.startState = startState;
        this.endState = endState;
    }

    private final HashMap<String, StateNode> edges = new HashMap<>();
    private final ArrayList<StateNode> emptyEdges = new ArrayList<>();
    private final ArrayList<StateNode> stateNodes = new ArrayList<>();

    public void addEdge(String trigger, StateNode node) {
        edges.put(trigger, node);
    }

    public void addEmptyEdge(StateNode node) {
        emptyEdges.add(node);
    }

    public void addStateNode(StateNode node) {
        stateNodes.add(node);
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

        for (var e : edges.entrySet()) {
            var state = e.getValue();
            s.append(generateState(state, e.getKey()));
            s.append(state);
        }

        for (var n : stateNodes) {
            s.append(n);
        }

        for (var state : emptyEdges) {
            s.append(generateState(state));
            s.append(state);
        }

        return s.toString();
    }
}
