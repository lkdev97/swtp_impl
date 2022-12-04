package de.thm.swtp;

import de.thm.swtp.statediagram.DiagramNode;
import de.thm.swtp.statediagram.MultiStateNode;
import de.thm.swtp.statediagram.StateNode;
import net.sourceforge.plantuml.sequencediagram.*;

import java.util.Iterator;

public class Transformer {

    private final SequenceDiagram sequenceDiagram;
    private final String targetParticipantName;
    private Iterator<Event> eventIterator;
    private Event currentEvent;
    private int stateId = 0;
    private Participant targetParticipant;

    public Transformer(SequenceDiagram sequenceDiagram, String targetParticipantName) {
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

            StateNode s;
            if (currentEvent instanceof GroupingStart) {
                s = generateGroup();
                target.addEdge(trigger, s);
            } else {
                s = new StateNode(stateId++);
                target.addEdge(trigger, s);
            }
            return s;
        } else {
            return target;
        }
    }

    private StateNode generate(StateNode target) {
        if (currentEvent instanceof Message) {
            return generateMessage(target);
        } else if (currentEvent instanceof GroupingStart) {
            return generateGroup();
        }

        throw new IllegalStateException();
    }

    private void generateBranch(StateNode baseState, StateNode endState) {
        var branchState = new MultiStateNode(stateId++);
        var currentState = new StateNode(stateId, true, false);
        branchState.addInnerState(currentState);
        baseState.addEdge(((Grouping) currentEvent).getComment(), branchState);

        nextEvent();

        while (!(currentEvent instanceof Grouping)) {
            currentState = generate(currentState);
        }

        currentState.setEndState(true);
        branchState.addEmptyEdge(endState);
    }

    private StateNode generateGroup() {
        var wrapperState = new MultiStateNode(stateId++);

        var startState = new StateNode(stateId++, true, false);
        wrapperState.addInnerState(startState);
        var baseState = new StateNode(stateId++);
        startState.addEmptyEdge(baseState);
        var endState = new StateNode(stateId++, false, true);

        while (true) {
            generateBranch(baseState, endState);

            if (currentEvent instanceof GroupingLeaf g && g.getType() == GroupingType.END) {
                break;
            }
        }

        nextEvent();

        return wrapperState;
    }

    private DiagramNode generateDiagram() {
        var startState = new StateNode(stateId++, true, false);
        var target = new StateNode(stateId++);
        startState.addEmptyEdge(target);

        var stateDiagram = new DiagramNode(startState, targetParticipant.getCode());

        while (currentEvent != null) {
            target = generate(target);
        }

        target.setEndState(true);

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
