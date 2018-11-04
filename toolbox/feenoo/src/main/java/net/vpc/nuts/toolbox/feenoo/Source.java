/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.nuts.toolbox.feenoo;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author vpc
 */
public interface Source {

    boolean isStream();

    boolean isFolder();

    String getName();
    
    String getExternalPath();
    
    String getInternalPath();

    Iterable<Source> getChildren();

    InputStream openStream() throws IOException;
}
