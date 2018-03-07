/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import net.vpc.app.nuts.NutsIOException;

/**
 *
 * @author vpc
 */
class FileInputStreamSource implements InputStreamSource {
    
    private final File file;

    public FileInputStreamSource(File file) {
        this.file = file;
    }

    @Override
    public InputStream openStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new NutsIOException(e);
        }
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public Object getSource() {
        return file;
    }

    @Override
    public String toString() {
        return "FileInputStreamSource{" + "file=" + file + '}';
    }
    
}
