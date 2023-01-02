package de.thm.swtp.statediagram;

import net.sourceforge.plantuml.sequencediagram.*;

import java.util.Iterator;

public class StateDiagramTransformer {

    private final SequenceDiagram sequenceDiagram;
    private final String targetParticipantName;
    private Iterator<Event> eventIterator;
    private Event currentEvent;
    private Participant targetParticipant;

    public StateDiagramTransformer(SequenceDiagram sequenceDiagram, String targetParticipantName) {
        this.sequenceDiagram = sequenceDiagram;
        this.targetParticipantName = targetParticipantName;
    }

    private void nextEvent() {
        if (eventIterator.hasNext()) {
            currentEvent = eventIterator.next();
        } else {
            currentEvent = null;
        }
    }

    private StateNode generateMessage(StateNode target) {
        var m = (Message) currentEvent;

        nextEvent();
        if (m.dealWith(targetParticipant)) {
            var trigger = m.getLabel().get(0).toString();
            if (m.getParticipant1().equals(targetParticipant)) {
                trigger = String.format("/ %s", trigger);
            }

            var s = generateState();
            target.addEdge(trigger, s);

            return s;
        } else {
            return target;
        }
    }

    private StateNode generateState() {
        if (currentEvent instanceof GroupingStart g) {
            return switch (g.getTitle()) {
                case "alt" -> generateAlt();
                case "loop" -> generateLoop();
                case "opt" -> generateOpt();
                default -> throw new IllegalStateException(String.format("Group type %s not implemented", g.getTitle()));
            };
        } else {
            return new StateNode();
        }
    }

    private StateNode generateLoop() {
        var outerWrapperState = new MultiStateNode();
        var outerStartState = new StateNode(true, false);
        outerWrapperState.addInnerState(outerStartState);

        var innerWrapperState = new MultiStateNode();
        outerStartState.addEmptyEdge(innerWrapperState);
        var innerStartState = new StateNode(true, false);
        innerWrapperState.addInnerState(innerStartState);
        var innerEndState = new StateNode(false, true);
        var exitState = new StateNode(false, true);

        var guard = ((Grouping) currentEvent).getComment();
        nextEvent();
        var currentState = new StateNode();
        innerStartState.addEmptyEdge(currentState);
        while (!(currentEvent instanceof Grouping g && (g.getType() == GroupingType.END || g.getType() == GroupingType.ELSE))) {
            currentState = generate(currentState);
        }

        nextEvent();

        currentState.addEmptyEdge(innerEndState);

        innerWrapperState.addEdge(guard, innerWrapperState);
        innerWrapperState.addEdge("!(" + guard + ")", exitState);

        return outerWrapperState;
    }

    private StateNode generateOpt() {
        var wrapperState = new MultiStateNode();
        var startState = new StateNode(true, false);
        wrapperState.addInnerState(startState);
        var endState = new StateNode(false, true);

        startState.addEdge("!(" + ((Grouping) currentEvent).getComment() + ")", endState);

        generateBranch(startState, endState);

        nextEvent();

        return wrapperState;
    }

    private StateNode generate(StateNode target) {
        if (currentEvent instanceof Message) {
            return generateMessage(target);
        } else {
            var s = generateState();
            target.addEmptyEdge(s);
            return s;
        }
    }

    private void generateBranch(StateNode baseState, StateNode endState) {
        var branchState = new MultiStateNode();
        var currentState = new StateNode(true, false);
        branchState.addInnerState(currentState);
        baseState.addEdge(((Grouping) currentEvent).getComment(), branchState);

        nextEvent();

        while (!(currentEvent instanceof Grouping g && (g.getType() == GroupingType.END || g.getType() == GroupingType.ELSE))) {
            currentState = generate(currentState);
        }

        var innerEndState = new StateNode(false, true);
        currentState.addEmptyEdge(innerEndState);

        branchState.addEmptyEdge(endState);
    }

    private StateNode generateAlt() {
        var wrapperState = new MultiStateNode();

        var startState = new StateNode(true, false);
        wrapperState.addInnerState(startState);
        var baseState = new StateNode();
        startState.addEmptyEdge(baseState);
        var endState = new StateNode(false, true);

        while (!(currentEvent instanceof GroupingLeaf g && g.getType() == GroupingType.END)) {
            generateBranch(baseState, endState);
        }

        nextEvent();

        return wrapperState;
    }

    private DiagramNode generateDiagram() {
        var startState = new StateNode(true, false);
        var idleState = new StateNode();
        startState.addEmptyEdge(idleState);
        var target = generate(idleState);

        var stateDiagram = new DiagramNode(startState, targetParticipant.getCode());

        while (currentEvent != null) {
            target = generate(target);
        }

        var endState = new StateNode(false, true);
        target.addEmptyEdge(endState);

        return stateDiagram;
    }

    public DiagramNode transform() {
        targetParticipant = sequenceDiagram.participants()
                .stream()
                .filter(p -> p.getCode().equals(targetParticipantName))
                .findFirst()
                .orElseThrow();
        eventIterator = sequenceDiagram.events().iterator();
        nextEvent();

        return generateDiagram();
    }
}
