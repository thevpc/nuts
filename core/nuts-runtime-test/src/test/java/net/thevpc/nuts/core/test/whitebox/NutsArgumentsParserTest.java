package net.thevpc.nuts.core.test.whitebox;

import org.junit.jupiter.api.*;

public class NutsArgumentsParserTest {
    @Test
    public void testCompress(){
        String[][] args={
                {"--hello","world"},
                {"--hel lo","world"},
                {"--hel' l\\'o","world"},
        };
//        for (String[] arg : args) {
//            System.out.println(Arrays.toString(arg));
//            String v = NutsMinimalCommandLine.escapeArguments(arg);
//            System.out.println("\t"+v);
//            String[] v2 = NutsMinimalCommandLine.parseCommand(v);
//            Assertions.assertArrayEquals(
//                    arg,v2
//            );
//        }
    }
}
