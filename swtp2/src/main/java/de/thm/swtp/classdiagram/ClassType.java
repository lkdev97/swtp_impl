package de.thm.swtp.classdiagram;

import net.sourceforge.plantuml.classdiagram.ClassDiagram;
import net.sourceforge.plantuml.cucadiagram.CodeImpl;
import net.sourceforge.plantuml.cucadiagram.ILeaf;
import net.sourceforge.plantuml.cucadiagram.LinkDecor;
import net.sourceforge.plantuml.cucadiagram.LinkType;

import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Represents a class with methods. Used to detected spelling and other semantic mistakes in given sequence diagram.
 */
public class ClassType {

    private final String name;
    private final HashSet<String> methods;
    private final HashSet<ClassType> parents;

    /**
     * Creates a new class type with a name, a list of methods and a list of classes this class inherits from.
     * @param name The name of this class.
     * @param methods A list of methods this class has.
     * @param parents A list of classes this class inherits methods from.
     */
    private ClassType(String name, HashSet<String> methods, HashSet<ClassType> parents) {
        this.name = name;
        this.methods = methods;
        this.parents = parents;
    }

    /**
     * Returns the name of this class.
     * @return The name of this class.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether this class or any of its parents have a method with the given name.
     * @param name The name of the method to look for.
     * @return Whether a method with the given name exists.
     */
    public boolean existsMethod(String name) {
        if (methods.contains(name)) {
            return true;
        }

        for (var p : parents) {
            if (p.existsMethod(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extracts the method name from a PlantUML method declaration.
     * Example: +void myMethod(int x, int y) --> myMethod
     * @param declaration The PlantUML method declaration.
     * @return The method name.
     */
    private static String extractMethodName(String declaration) {
        var pattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]+");
        var matcher = pattern.matcher(declaration);
        if (!matcher.find() || !matcher.find()) {
            throw new IllegalStateException("Could not extract method name from " + declaration);
        }
        return matcher.group();
    }

    /**
     * Returns whether the given link type is class/interface extension.
     * @param l The link type.
     * @return Whether it is an extension.
     */
    private static boolean isExtends(LinkType l) {
        return l.getDecor1() == LinkDecor.EXTENDS || l.getDecor2() == LinkDecor.EXTENDS;
    }

    /**
     * Recursively generates a new class type from a given PlantUML class diagram class.
     * @param leaf The class leaf to generate a class type for.
     * @param classDiagram The class diagram the given leaf belongs to.
     * @return The generated class type.
     */
    public static ClassType of(ILeaf leaf, ClassDiagram classDiagram) {
        var className = leaf.getCodeGetName();

        var methods = new HashSet<String>();
        var parents = new HashSet<ClassType>();

        for (var m : leaf.getBodier().getMethodsToDisplay()) {
            methods.add(extractMethodName(m.toString()));
        }

        for (var l : classDiagram.getEntityFactory().getLinks()) {
            if (!l.contains(leaf) || !isExtends(l.getType()) || !l.getEntity2().equals(leaf)) continue;

            var parent = classDiagram.getLeaf(CodeImpl.of(l.getEntity1().getCodeGetName()));
            parents.add(of(parent, classDiagram));
        }

        return new ClassType(className, methods, parents);
    }
}
