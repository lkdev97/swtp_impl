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

            var s = generateState();
            target.addEdge(trigger, s);

            return s;
        } else {
            return target;
        }
    }

    private StateNode generateState() {
        if (currentEvent instanceof GroupingStart) {
            return generateGroup();
        } else {
            return new StateNode(stateId++);
        }
    }

    private StateNode generate(StateNode target) {
        if (currentEvent instanceof Message) {
            return generateMessage(target);
        } else {;
            var s = generateState();
            target.addEmptyEdge(s);
            return s;
        }
    }

    private void generateBranch(StateNode baseState, StateNode endState) {
        var branchState = new MultiStateNode(stateId++);
        var currentState = new StateNode(stateId++, true, false);
        branchState.addInnerState(currentState);
        baseState.addEdge(((Grouping) currentEvent).getComment(), branchState);

        nextEvent();

        while (!(currentEvent instanceof Grouping g && (g.getType() == GroupingType.END || g.getType() == GroupingType.ELSE))) {
            currentState = generate(currentState);
        }

        var innerEndState = new StateNode(stateId++, false, true);
        currentState.addEmptyEdge(innerEndState);

        branchState.addEmptyEdge(endState);
    }

    private StateNode generateGroup() {
        var wrapperState = new MultiStateNode(stateId++);

        var startState = new StateNode(stateId++, true, false);
        wrapperState.addInnerState(startState);
        var baseState = new StateNode(stateId++);
        startState.addEmptyEdge(baseState);
        var endState = new StateNode(stateId++, false, true);

        while (!(currentEvent instanceof GroupingLeaf g && g.getType() == GroupingType.END)) {
            generateBranch(baseState, endState);
        }

        nextEvent();

        return wrapperState;
    }

    private DiagramNode generateDiagram() {
        var startState = new StateNode(stateId++, true, false);
        var idleState = new StateNode(stateId++);
        startState.addEmptyEdge(idleState);
        var target = generate(idleState);

        var stateDiagram = new DiagramNode(startState, targetParticipant.getCode());

        while (currentEvent != null) {
            target = generate(target);
        }

        var endState = new StateNode(stateId++, false, true);
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
