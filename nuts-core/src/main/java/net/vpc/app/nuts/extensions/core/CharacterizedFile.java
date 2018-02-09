/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.extensions.util.InputStreamSource;

/**
 *
 * @author vpc
 */
class CharacterizedFile implements AutoCloseable {
    
    InputStreamSource contentFile;
    List<File> temps = new ArrayList<>();
    NutsDescriptor descriptor;
    private final DefaultNutsWorkspace outer;

    CharacterizedFile(final DefaultNutsWorkspace outer) {
        this.outer = outer;
    }

    public void addTemp(File f) {
        temps.add(f);
    }

    @Override
    public void close() {
        for (File temp : temps) {
            temp.delete();
        }
    }
    
}
