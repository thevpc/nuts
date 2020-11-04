/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.NoSuchElementException;
import net.thevpc.nuts.NutsIllegalArgumentException;

/**
 *
 * @author vpc
 */
public class FileSystemStash {

    private File stash;

//    static {
//        stash = new File(System.getProperty("user.home"), "stash/nuts");
//        TestUtils.println("creating stash at " + stash.getPath());
//        stash.mkdirs();
//    }
    public FileSystemStash() {
        this(null);
    }

    public FileSystemStash(File stash) {
        if (stash == null) {
            stash = new File(System.getProperty("user.home"), "stash");
        }
        this.stash = stash;
        this.stash.mkdirs();
    }

    public void restore(String f) throws IOException {
        File g = new File(stash, f + ".saved");
        if (!g.isDirectory()) {
            throw new NoSuchElementException("No such element " + f);
        }
        String oldPath = new String(Files.readAllBytes(new File(g, "path").toPath()));
        move(new File(g, "content"), new File(oldPath));
        delete(g);
    }

    public String[] list() throws IOException {
        return Arrays.stream(stash.listFiles()).map(x -> x.getName()).filter(x -> x.endsWith(".saved")).map(x -> x.substring(0, x.length() - ".saved".length()))
                .toArray(String[]::new);
    }

    public String saveIfExists(File f) throws IOException {
        if (!f.exists()) {
            return null;
        }
        return save(f);
    }

    public String save(File f) throws IOException {
        File g = File.createTempFile("stash-", ".saved", stash);
        delete(g);
        mkdirs(g);
        File path = new File(g, "path");
        Files.write(path.toPath(), f.getCanonicalPath().getBytes());
        File val = new File(g, "content");
        move(f, val);
        return g.getName().substring(0, g.getName().length() - ".saved".length());
    }

    private static void mkdirs(File sourceFile) {
        if (!sourceFile.mkdirs()) {
            throw new NutsIllegalArgumentException(null, "Unable to mkdir " + sourceFile);
        }
    }

    private static void move(File sourceFile, File destFile) throws IOException {
        copy(sourceFile, destFile);
        delete(sourceFile);
    }

    private static void copy(File sourceFile, File destFile) throws IOException {
        if (sourceFile.isDirectory()) {
            destFile.mkdirs();
            for (File file : sourceFile.listFiles()) {
                copy(file, new File(destFile, file.getName()));
            }
        } else {
            Files.copy(Paths.get(sourceFile.getPath()), Paths.get(destFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void delete(File sourceFile) throws IOException {
        if (sourceFile.isDirectory()) {
            for (File file : sourceFile.listFiles()) {
                delete(file);
            }
            Files.delete(Paths.get(sourceFile.getPath()));
        } else {
            Files.delete(Paths.get(sourceFile.getPath()));
        }
    }

    public void restoreAll() throws IOException {
        for (String id : list()) {
            restore(id);
        }
    }
}
