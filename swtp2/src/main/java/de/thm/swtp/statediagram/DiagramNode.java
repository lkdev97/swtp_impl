package de.thm.swtp.statediagram;

public class DiagramNode extends Node {

    private final StateNode firstState;
    private final String name;

    public DiagramNode(StateNode firstState, String name) {
        this.firstState = firstState;
        this.name = name;
    }

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
