/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndiff.jar;

import java.util.List;

/**
 *
 * @author thevpc
 */
public interface DiffResult extends AutoCloseable,Iterable {

    boolean hasChanges();
    
    List<DiffItem> all();

    List<DiffItem> removed();

    List<DiffItem> added();

    List<DiffItem> changed();
}
