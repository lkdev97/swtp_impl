package de.thm.swtp;

import de.thm.swtp.classdiagram.ClassDiagramValidator;
import de.thm.swtp.statediagram.StateDiagramTransformer;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.classdiagram.ClassDiagram;
import net.sourceforge.plantuml.sequencediagram.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    /**
     * Reads a file and returns its content.
     * @param path Path of the file.
     * @return The file's content.
     */
    private static String readFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException("File not found");
        }
    }

    /**
     * Writes string to a file it created.
     * @param path Path of the file to create and write to.
     * @param content Content to be written.
     */
    private static void writeFile(String path, String content) {
        try {
            Files.writeString(Paths.get(path), content);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save file");
        }
    }

    public static void main(String[] args) throws IOException {
        var classDiagramCode = readFile("./classdiagram.plantuml");
        var sequenceDiagramCode = readFile("./input.plantuml");

        var classDiagram = (ClassDiagram) new SourceStringReader(classDiagramCode).getBlocks().get(0).getDiagram();
        var sequenceDiagram = (SequenceDiagram) new SourceStringReader(sequenceDiagramCode).getBlocks().get(0).getDiagram();

        var classDiagramReader = new ClassDiagramValidator(classDiagram, sequenceDiagram);
        var stateDiagramTransformer = new StateDiagramTransformer(sequenceDiagram, "CoffeeMachineControllerImpl");

        classDiagramReader.validate();

        writeFile("./test.plantuml", stateDiagramTransformer.transform().toString());
    }
}
