/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.io;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import net.vpc.app.nuts.NutsUnsupportedOperationException;

/**
 *
 * @author vpc
 */
public interface InputSource extends AutoCloseable {

    String getName();

    long length();

    boolean isPath();

    boolean isURL() ;

    Path getPath() throws NutsUnsupportedOperationException;

    URL getURL() throws NutsUnsupportedOperationException;

    Object getSource();

    InputStream open();

    void copyTo(Path path);

    MultiInputSource multi();

    @Override
    void close();

}
