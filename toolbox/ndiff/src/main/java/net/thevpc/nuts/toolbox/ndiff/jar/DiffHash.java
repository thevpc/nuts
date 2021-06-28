/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndiff.jar;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author thevpc
 */
public interface DiffHash {
    String hash(InputStream inputStream) throws IOException;
}
