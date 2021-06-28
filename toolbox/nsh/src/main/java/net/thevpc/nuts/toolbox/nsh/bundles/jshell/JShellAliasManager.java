/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import java.util.Set;

/**
 *
 * @author thevpc
 */
public interface JShellAliasManager {

    String get(String name);

    Set<String> getAll();

    void set(String key, String value);
    
}
