package de.thm.swtp.statediagram;

import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.error.PSystemErrorV2;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateDiagramTransformerTest {

    private SequenceDiagram parseSequenceDiagram(String code) {
        return (SequenceDiagram) new SourceStringReader(code).getBlocks().get(0).getDiagram();
    }

    @Test
    public void testMessageEvent() {
        StateNode.stateIdCounter = 0;

        var code =
                """
                @startuml message
                participant User
                participant GasPump
                participant Bank
                User --> GasPump: insertCard
                GasPump --> User: requestPin
                User --> GasPump: pinCode
                GasPump --> Bank: validate
                Bank --> GasPump: result(pinOK)
                @enduml
                """;

        var sequenceDiagram = parseSequenceDiagram(code);
        var stateDiagram = new StateDiagramTransformer(sequenceDiagram, "GasPump").transform().toString();

        assertEquals(stateDiagram, """
                @startuml
                hide empty description
                state GasPump {
                [*] --> S1
                S1 --> S2: insertCard
                S2 --> S3: / requestPin
                S3 --> S4: pinCode
                S4 --> S5: / validate
                S5 --> S6: result(pinOK)
                S6 --> [*]
                }
                @enduml
                """);
    }

    @Test
    public void testOptEvent() {
        StateNode.stateIdCounter = 0;

        var code =
                """
                @startuml message
                participant User
                participant GasPump
                opt pinOK
                    GasPump --> User: startFuel
                    User --> GasPump: hangUp
                end
                @enduml
                """;

        var sequenceDiagram = parseSequenceDiagram(code);
        var stateDiagram = new StateDiagramTransformer(sequenceDiagram, "GasPump").transform().toString();

        assertEquals(stateDiagram, """
                @startuml
                hide empty description
                state GasPump {
                [*] --> S1
                S1 --> S2
                state S2 {
                [*] --> [*]: !(pinOK)
                [*] --> S5: pinOK
                state S5 {
                [*] --> S7: / startFuel
                S7 --> S8: hangUp
                S8 --> [*]
                }
                S5 --> [*]
                }
                S2 --> [*]
                }
                @enduml
                """);
    }

    @Test
    public void testAltEvent() {
        StateNode.stateIdCounter = 0;

        var code =
                """
                @startuml message
                participant User
                participant GasPump
                alt pinOK
                    GasPump --> User: startFuel
                    User --> GasPump: hangUp
                else
                    GasPump --> User: invalidPin
                end
                @enduml
                """;

        var sequenceDiagram = parseSequenceDiagram(code);
        var stateDiagram = new StateDiagramTransformer(sequenceDiagram, "GasPump").transform().toString();

        assertEquals(stateDiagram, """
                @startuml
                hide empty description
                state GasPump {
                [*] --> S1
                S1 --> S2
                state S2 {
                [*] --> S4
                S4 --> S6: pinOK
                state S6 {
                [*] --> S8: / startFuel
                S8 --> S9: hangUp
                S9 --> [*]
                }
                S6 --> [*]
                S4 --> S11
                state S11 {
                [*] --> S13: / invalidPin
                S13 --> [*]
                }
                S11 --> [*]
                }
                S2 --> [*]
                }
                @enduml
                """);
    }

    @Test
    public void testLoopEvent() {
        StateNode.stateIdCounter = 0;

        var code =
                """
                @startuml message
                participant User
                participant GasPump
                loop !pinOK
                    GasPump --> User: requestPin
                    User --> GasPump: providePin
                    GasPump --> GasPump: validatePin
                end
                @enduml
                """;

        var sequenceDiagram = parseSequenceDiagram(code);
        var stateDiagram = new StateDiagramTransformer(sequenceDiagram, "GasPump").transform().toString();

        assertEquals(stateDiagram, """
                @startuml
                hide empty description
                state GasPump {
                [*] --> S1
                S1 --> S2
                state S2 {
                [*] --> S4
                state S4 {
                [*] --> S8
                S8 --> S9: / requestPin
                S9 --> S10: providePin
                S10 --> S11: / validatePin
                S11 --> [*]
                }
                S4 --> S4: !pinOK
                S4 --> [*]: !(!pinOK)
                }
                S2 --> [*]
                }
                @enduml
                """);
    }

    @Test
    void testAllEvents() {
        StateNode.stateIdCounter = 0;

        var code =
                """
                @startuml message
                participant User
                participant GasPump
                participant Bank
                User --> GasPump: insertCard
                loop !pinOk
                    GasPump --> User: requestPin
                    User --> GasPump: pinCode
                    GasPump --> Bank: validate
                    Bank --> GasPump: result(pinOK)
                    alt pinOK
                        GasPump --> User: startFuel
                        User --> GasPump: hangUp
                    else
                        GasPump --> User: showWarning
                    end
                    
                    opt cancel
                        GasPump --> User: showDialog
                    end
                end
                GasPump --> User: cardOut
                @enduml
                """;

        var sequenceDiagram = parseSequenceDiagram(code);
        var stateDiagram = new StateDiagramTransformer(sequenceDiagram, "GasPump").transform().toString();

        assertEquals(stateDiagram, """
                @startuml
                hide empty description
                state GasPump {
                [*] --> S1
                S1 --> S2: insertCard
                state S2 {
                [*] --> S4
                state S4 {
                [*] --> S8
                S8 --> S9: / requestPin
                S9 --> S10: pinCode
                S10 --> S11: / validate
                S11 --> S12: result(pinOK)
                state S12 {
                [*] --> S14
                S14 --> S16: pinOK
                state S16 {
                [*] --> S18: / startFuel
                S18 --> S19: hangUp
                S19 --> [*]
                }
                S16 --> [*]
                S14 --> S21
                state S21 {
                [*] --> S23: / showWarning
                S23 --> [*]
                }
                S21 --> [*]
                }
                S12 --> S25
                state S25 {
                [*] --> [*]: !(cancel)
                [*] --> S28: cancel
                state S28 {
                [*] --> S30: / showDialog
                S30 --> [*]
                }
                S28 --> [*]
                }
                S25 --> [*]
                }
                S4 --> S4: !pinOk
                S4 --> [*]: !(!pinOk)
                }
                S2 --> S32: / cardOut
                S32 --> [*]
                }
                @enduml
                """);
    }

    @Test
    public void testSyntaxError() {
        StateNode.stateIdCounter = 0;

        var code =
                """
                @startuml message
                participant User
                participant GasPump
                opts pinOK
                    GasPump --> User: startFuel
                    User --> GasPump: hangUp
                end
                @enduml
                """;

        try {
            parseSequenceDiagram(code);
            fail();
        } catch (ClassCastException e) {

        }
    }
}