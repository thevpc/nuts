/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.util.io;

import java.nio.file.Path;

import net.vpc.app.nuts.NutsInput;

/**
 *
 * @author vpc
 */
public interface CoreInput extends NutsInput {

    void copyTo(Path path);

    MultiInput multi();

}
