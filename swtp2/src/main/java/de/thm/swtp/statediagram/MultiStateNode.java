package de.thm.swtp.statediagram;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiStateNode extends StateNode {

    private final HashMap<String, StateNode> edges = new HashMap<>();
    private final ArrayList<StateNode> emptyEdges = new ArrayList<>();
    private boolean finalized = false;

    public MultiStateNode(int id) {
        super(id);
    }

    @Override
    public void addEdge(String trigger, StateNode node) {
        if (finalized) {
            edges.put(trigger, node);
        } else {
            super.addEdge(trigger, node);
        }
    }

    @Override
    public void addEmptyEdge(StateNode node) {
        if (finalized) {
            emptyEdges.add(node);
        } else {
            super.addEmptyEdge(node);
        }
    }

    public void setFinalized() {
        this.finalized = true;
    }

    @Override
    public String toString() {
        var s = new StringBuilder();

        s.append(String.format("state S%s {\n", id));
        s.append(super.toString());
        s.append("}\n");

        for (var e : edges.entrySet()) {
            var state = e.getValue();
            s.append(generateState(state, e.getKey()));
            s.append(state);
        }

        for (var state : emptyEdges) {
            s.append(generateState(state));
            s.append(state);
        }

        return s.toString();
    }
}
