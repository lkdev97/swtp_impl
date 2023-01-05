package de.thm.swtp;

import de.thm.swtp.classdiagram.ClassDiagramValidator;
import de.thm.swtp.statediagram.StateDiagramTransformer;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.classdiagram.ClassDiagram;
import net.sourceforge.plantuml.sequencediagram.*;
import org.apache.commons.cli.*;

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

    private static void error(String message) {
        System.out.println("Error: " + message);
        System.exit(1);
    }

    public static void main(String[] args) throws IOException {
        var options = new Options()
                .addOption(Option.builder("s")
                        .longOpt("sequenceDiagram")
                        .hasArg()
                        .desc("The sequence diagram to transform to a sate diagram.")
                        .argName("path")
                        .required()
                        .build())
                .addOption(Option.builder("c")
                        .longOpt("classDiagram")
                        .hasArg()
                        .desc("The class diagram to ensure coherence with.")
                        .argName("path")
                        .build())
                .addOption(Option.builder("o")
                        .longOpt("output")
                        .hasArg()
                        .desc("Path to write the generated state diagram to.")
                        .argName("path")
                        .required()
                        .build())
                .addOption(Option.builder("p")
                        .longOpt("targetParticipant")
                        .hasArg()
                        .desc("Every event in the sequence diagram that does not deal with this participant will be ignored.")
                        .argName("name")
                        .required()
                        .build());

        var optionParser = new DefaultParser();

        try {
            var opt = optionParser.parse(options, args);

            var sequenceDiagramPath = opt.getOptionValue("sequenceDiagram");
            var classDiagramPath = opt.getOptionValue("classDiagram");
            var outputPath = opt.getOptionValue("output", null);
            var targetParticipant = opt.getOptionValue("targetParticipant");

            String sequenceDiagramCode = null;
            try {
                sequenceDiagramCode = readFile(sequenceDiagramPath);
            } catch (IllegalStateException e) {
                error("Could not open file " + sequenceDiagramPath);
            }
            SequenceDiagram sequenceDiagram = null;
            try {
                assert sequenceDiagramCode != null;
                sequenceDiagram = (SequenceDiagram) new SourceStringReader(sequenceDiagramCode).getBlocks().get(0).getDiagram();
            } catch (ClassCastException e) {
                error("PlantUML failed to parse given sequence diagram. Make sure it does not contain any syntax errors.");
            }

            if (classDiagramPath != null) {
                String classDiagramCode = null;
                try {
                    classDiagramCode = readFile(classDiagramPath);
                } catch (IllegalStateException e) {
                    error("Could not open file " + classDiagramCode);
                }
                ClassDiagram classDiagram = null;
                try {
                    assert classDiagramCode != null;
                    classDiagram = (ClassDiagram) new SourceStringReader(classDiagramCode).getBlocks().get(0).getDiagram();
                } catch (ClassCastException e) {
                    error("PlantUML failed to parse given class diagram. Make sure it does not contain any syntax errors.");
                }
                var classDiagramReader = new ClassDiagramValidator(classDiagram, sequenceDiagram);
                classDiagramReader.validate();
            }

            var stateDiagramTransformer = new StateDiagramTransformer(sequenceDiagram, targetParticipant);
            writeFile(outputPath, stateDiagramTransformer.transform().toString());
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("transformer.jar args...", options);
        }
    }
}
