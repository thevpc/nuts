package net.thevpc.nuts.toolbox.nsh.test;

import net.thevpc.nuts.toolbox.nsh.cmds.util.BufferedLineIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestBufferedLineIterator {
    public static void main(String[] args) {
        try (BufferedReader r = Files.newBufferedReader(Paths.get("/home/vpc/.inputrc"))) {
            BufferedLineIterator b = new BufferedLineIterator(r, -3L, -2L);
            while(b.hasNext()){
                System.out.println(b.next());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
