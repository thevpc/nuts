/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

/**
 *
 * @author vpc
 */
public interface NutsHashCommand {

    NutsHashCommand source(InputStream input);

    NutsHashCommand source(File file);

    NutsHashCommand source(Path file);

    NutsHashCommand source(NutsDescriptor file);

    String computeString();

    byte[] computeBytes();

    String getAlgo();

    NutsHashCommand md5();

    NutsHashCommand setAlgorithm(String algo);

    NutsHashCommand sha1();

    NutsHashCommand algorithm(String algo);

    String getAlgorithm();
}
