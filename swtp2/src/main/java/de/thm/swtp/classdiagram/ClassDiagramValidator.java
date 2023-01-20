package de.thm.swtp.classdiagram;

import net.sourceforge.plantuml.classdiagram.ClassDiagram;
import net.sourceforge.plantuml.sequencediagram.Message;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Used to validate the coherence of a class and sequence diagram.
 * Can detect common errors like spelling mistakes in method oder participant (class) names.
 */
public class ClassDiagramValidator {

    private final ClassDiagram classDiagram;
    private final SequenceDiagram sequenceDiagram;
    private final Map<String, ClassType> classes = new HashMap<>();

    /**
     * Creates a new ClassDiagramValidator for the given class and sequence diagrams.
     * @param classDiagram The class diagram to check coherence with.
     * @param sequenceDiagram The sequence diagram that will later be transformed into a state diagram.
     */
    public ClassDiagramValidator(ClassDiagram classDiagram, SequenceDiagram sequenceDiagram) {
        this.classDiagram = classDiagram;
        this.sequenceDiagram = sequenceDiagram;
    }

    /**
     * Returns whether a class with the given name exists in the given class diagram.
     * @param className The name of the class to look for.
     * @return Whether a class with the given name exists.
     */
    private boolean existsClass(String className) {
        return classes.containsKey(className);
    }

    /**
     * Extracts the method name from a method call.
     * Example: myMethod(1, 2) --> myMethod
     * @param methodCall The method call.
     * @return The name of the method invoked by the given method call.
     */
    private String extractMethodName(String methodCall) {
        var pattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]+");
        var matcher = pattern.matcher(methodCall);
        if (!matcher.find()) {
            throw new IllegalStateException("Could not extract method name from " + methodCall);
        }
        return matcher.group();
    }

    /**
     * Prints an error message and exits the program with a non-zero exit code.
     * @param m The sequence diagram message this error belongs to.
     * @param message The error message to display.
     */
    private void error(Message m, String message) {
        System.out.printf("Error at %s --> %s: %s", m.getParticipant1().getCode(), m.getParticipant2().getCode(), message);
        System.exit(1);
    }

    /**
     * Checks coherence between the given class and sequence diagram. Prints an error message and exits the
     * program if discrepancies are found.
     */
    public void validate() {
        for (var l : classDiagram.getLeafsvalues()) {
            var clazz = ClassType.of(l, classDiagram);
            classes.put(clazz.getName(), clazz);
        }

        for (var e : sequenceDiagram.events()) {
            if (e instanceof Message m) {
                if (!existsClass(m.getParticipant1().getCode())) {
                    error(m, String.format("Class %s does not exist.", m.getParticipant1().getCode()));
                }

                if (!existsClass(m.getParticipant2().getCode())) {
                    error(m, String.format("Class %s does not exist.", m.getParticipant2().getCode()));
                }

                if (m.getLabel().get(0).toString().contains(".")) {
                    continue;
                }

                var clazz = classes.get(m.getParticipant2().getCode());
                var methodName = extractMethodName(m.getLabel().get(0).toString());
                if (!clazz.existsMethod(methodName)) {
                    error(m, String.format("Class %s has no method %s.", clazz.getName(), methodName));
                }
            }
        }
    }
}
