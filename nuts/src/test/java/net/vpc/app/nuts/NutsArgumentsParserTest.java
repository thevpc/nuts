package net.vpc.app.nuts;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class NutsArgumentsParserTest {
    @Test
    public void testCompress(){
        String[][] args={
                {"--hello","world"},
                {"--hel lo","world"},
                {"--hel' l\\'o","world"},
        };
        for (String[] arg : args) {
            System.out.println(Arrays.toString(arg));
            String v = NutsArgumentsParser.compressBootArguments(arg);
            System.out.println("\t"+v);
            String[] v2 = NutsArgumentsParser.uncompressBootArguments(v);
            Assert.assertArrayEquals(
                    arg,v2
            );
        }
    }
}
