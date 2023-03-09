/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.alias;

import java.util.Set;

/**
 *
 * @author thevpc
 */
public interface NShellAliasManager {

    String get(String name);

    Set<String> getAll();

    void set(String key, String value);
    
}
