/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.ndi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author vpc
 */
public class NdiUtils {
    public static boolean setExecutable(Path path) {
        if (Files.exists(path) && !Files.isExecutable(path)) {
            PosixFileAttributeView p = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            if (p != null) {
                try {
                    Set<PosixFilePermission> old = new HashSet<>(p.readAttributes().permissions());
                    old.add(PosixFilePermission.OWNER_EXECUTE);
                    Files.setPosixFilePermissions(path, old);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                return true;
            }
        }
        return false;
    }    
}
