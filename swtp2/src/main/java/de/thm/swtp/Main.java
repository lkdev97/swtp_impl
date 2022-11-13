package de.thm.swtp;

import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.sequencediagram.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    private static String readFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException("File not found");
        }
    }

    private static void writeFile(String path, String content) {
        try {
            Files.writeString(Paths.get(path), content);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save file");
        }
    }

    public static void main(String[] args) throws IOException {
        var code = readFile("./input.plantuml");
        var reader = new SourceStringReader(code);
        var sequenceDiagram = (SequenceDiagram) reader.getBlocks().get(0).getDiagram();

        var transformer = new Transformer(sequenceDiagram, "GasPump");

        writeFile("./test.plantuml", transformer.transform().toString());
    }
}
