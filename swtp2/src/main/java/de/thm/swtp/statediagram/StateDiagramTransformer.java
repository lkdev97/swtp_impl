package de.thm.swtp.statediagram;

import net.sourceforge.plantuml.sequencediagram.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Transforms a given sequence diagram into a semantically equivalent state diagram.
 */
public class StateDiagramTransformer {

    private final SequenceDiagram sequenceDiagram;
    private final String targetParticipantName;
    private Iterator<Event> eventIterator;
    private Event currentEvent;
    private Participant targetParticipant;

    /**
     * Creates a new StateDiagramTransformer which transformers the given sequence diagram into a semantically
     * equivalent state diagram.
     * @param sequenceDiagram The sequence diagram to be transformed.
     * @param targetParticipantName All messages that do not deal with this participant will be ignored.
     */
    public StateDiagramTransformer(SequenceDiagram sequenceDiagram, String targetParticipantName) {
        this.sequenceDiagram = sequenceDiagram;
        this.targetParticipantName = targetParticipantName;
    }

    /**
     * Reads the next event of the provided sequence diagram.
     */
    private void nextEvent() {
        if (eventIterator.hasNext()) {
            currentEvent = eventIterator.next();
        } else {
            currentEvent = null;
        }
    }

    /**
     * Generates state diagram for a sequence diagram message (par1 --> par2: trigger).
     * @param target The state node to append to.
     * @return The next state node to append to.
     */
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

    /**
     * Generates the next state node based on the current event.
     * @return A newly generated state node.
     */
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

    /**
     * Generates state diagram for a sequence diagram loop block (loop [con] [events] end).
     * @return The next state node to append to.
     */
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

    /**
     * Generates state diagram for a sequence diagram opt block (opt [con] [events] end).
     * @return The next state node to append to.
     */
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

    /**
     * Generates a new state node and appends it to the given target state node.
     * @param target The state node to append the newly generated state to.
     * @return The next state node to append to.
     */
    private StateNode generate(StateNode target) {
        if (currentEvent instanceof Message) {
            return generateMessage(target);
        } else {
            var s = generateState();
            target.addEmptyEdge(s);
            return s;
        }
    }

    /**
     * Generates a single branch of an alt or opt block.
     * @param baseState The state to append this branch to.
     * @param endState The next state to append to.
     */
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

    /**
     * Generates state diagram for a sequence diagram alt block.
     * @return The next state to append to.
     */
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

    /**
     * Generates a new state diagram based on the given sequence diagram.
     * @return The generated state diagram.
     */
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

    /**
     * Transforms the given sequence diagram to a state diagram and returns it.
     * @return The generated state diagram.
     */
    public DiagramNode transform() {
        try {
            targetParticipant = sequenceDiagram.participants()
                    .stream()
                    .filter(p -> p.getCode().equals(targetParticipantName))
                    .findFirst()
                    .orElseThrow();
            eventIterator = sequenceDiagram.events().iterator();
            nextEvent();

            return generateDiagram();
        } catch (NoSuchElementException e) {
            System.out.println("Could not find participant with name " + targetParticipantName);
            System.exit(1);
            return null;
        }
    }
}
