package de.thm.swtp.statediagram;

import java.util.ArrayList;

public class MultiStateNode extends StateNode {

    protected final ArrayList<StateNode> innerStates = new ArrayList<>();
    private boolean added = false;

    public void addInnerState(StateNode node) {
        innerStates.add(node);
    }

    @Override
    public String toString() {
        var s = new StringBuilder();

        if (!added) {
            added = true;

            s.append(String.format("state S%s {\n", id));
            for (var e : innerStates) {
                s.append(e.toString());
            }
            s.append("}\n");

            s.append(super.toString());
        }

        return s.toString();
    }
}
