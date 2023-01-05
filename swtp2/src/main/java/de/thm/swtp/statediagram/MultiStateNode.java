package de.thm.swtp.statediagram;

import java.util.ArrayList;

/**
 * A state node which contains a state machine itself (sub-automata).
 */
public class MultiStateNode extends StateNode {

    protected final ArrayList<StateNode> innerStates = new ArrayList<>();
    // Whether this state has been added to the string representation of the state diagram already.
    // Prevents infinite recursion.
    private boolean added = false;

    /**
     * Adds a new state to the inner content of this multi-state.
     * @param node The state to add.
     */
    public void addInnerState(StateNode node) {
        innerStates.add(node);
    }

    /**
     * Returns a string representation of this state node as expected by PlantUML.
     * @return The string representation of this state node.
     */
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
