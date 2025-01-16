package de.thm.swtp.statediagram;

/**
 * Represents a state diagram node.
 */
public class DiagramNode extends Node {

    private final StateNode firstState;
    private final String name;

    /**
     * Creates a new diagram node with a given name and start state.
     * @param firstState The start state of this diagram.
     * @param name The name of this diagram.
     */
    public DiagramNode(StateNode firstState, String name) {
        this.firstState = firstState;
        this.name = name;
    }

    /**
     * Generates a string representation of this diagram node as expected by PlantUML.
     * @return The string representation.
     */
    @Override
    public String toString() {
        var s = new StringBuilder();
        s.append("@startuml\n");
        s.append("hide empty description\n");
        s.append(String.format("state %s {\n", name));
        s.append(firstState.toString());
        s.append("}\n");
        s.append("@enduml\n");

        return s.toString();
    }
}
