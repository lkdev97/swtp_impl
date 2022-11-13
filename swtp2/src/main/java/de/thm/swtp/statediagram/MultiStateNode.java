package de.thm.swtp.statediagram;

import java.util.ArrayList;

public class MultiStateNode extends StateNode {

    private final ArrayList<Edge> edges = new ArrayList<>();
    private boolean finalized = false;

    public MultiStateNode(int id) {
        super(id);
    }

    @Override
    public void addEdge(String trigger, StateNode node) {
        if (finalized) {
            edges.add(new Edge(node, trigger));
        } else {
            super.addEdge(trigger, node);
        }
    }

    @Override
    public void addEmptyEdge(StateNode node) {
        if (finalized) {
            edges.add(new Edge(node));
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
