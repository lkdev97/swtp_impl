package de.thm.swtp.classdiagram;

import net.sourceforge.plantuml.classdiagram.ClassDiagram;
import net.sourceforge.plantuml.sequencediagram.Message;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ClassDiagramValidator {

    private final ClassDiagram classDiagram;
    private final SequenceDiagram sequenceDiagram;
    private final Map<String, ClassType> classes = new HashMap<>();

    public ClassDiagramValidator(ClassDiagram classDiagram, SequenceDiagram sequenceDiagram) {
        this.classDiagram = classDiagram;
        this.sequenceDiagram = sequenceDiagram;
    }

    private boolean existsClass(String className) {
        return classes.containsKey(className);
    }

    private String extractMethodName(String methodName) {
        var pattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]+");
        var matcher = pattern.matcher(methodName);
        if (!matcher.find()) {
            throw new IllegalStateException("Could not extract method name from " + methodName);
        }
        return matcher.group();
    }

    private void error(Message m, String message) {
        System.out.printf("Error at %s --> %s: %s", m.getParticipant1().getCode(), m.getParticipant2().getCode(), message);
        System.exit(1);
    }

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

                var clazz = classes.get(m.getParticipant2().getCode());
                var methodName = extractMethodName(m.getLabel().get(0).toString());
                if (!clazz.existsMethod(methodName)) {
                    error(m, String.format("Class %s has no method %s.", clazz.getName(), methodName));
                }
            }
        }
    }
}
