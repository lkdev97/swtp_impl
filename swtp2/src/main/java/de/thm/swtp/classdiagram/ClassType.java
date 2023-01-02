package de.thm.swtp.classdiagram;

import net.sourceforge.plantuml.classdiagram.ClassDiagram;
import net.sourceforge.plantuml.cucadiagram.CodeImpl;
import net.sourceforge.plantuml.cucadiagram.ILeaf;
import net.sourceforge.plantuml.cucadiagram.LinkDecor;
import net.sourceforge.plantuml.cucadiagram.LinkType;

import java.util.HashSet;
import java.util.regex.Pattern;

public class ClassType {

    private final String name;
    private final HashSet<String> methods;
    private final HashSet<ClassType> parents;

    private ClassType(String name, HashSet<String> methods, HashSet<ClassType> parents) {
        this.name = name;
        this.methods = methods;
        this.parents = parents;
    }

    public String getName() {
        return name;
    }

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

    private static String extractMethodName(String declaration) {
        var pattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]+");
        var matcher = pattern.matcher(declaration);
        if (!matcher.find() || !matcher.find()) {
            throw new IllegalStateException("Could not extract method name from " + declaration);
        }
        return matcher.group();
    }

    private static boolean isExtends(LinkType l) {
        return l.getDecor1() == LinkDecor.EXTENDS || l.getDecor2() == LinkDecor.EXTENDS;
    }

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
