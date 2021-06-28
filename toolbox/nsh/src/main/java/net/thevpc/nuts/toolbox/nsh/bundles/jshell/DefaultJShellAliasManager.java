/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author thevpc
 */
public class DefaultJShellAliasManager implements JShellAliasManager {

    public final Map<String, String> aliases = new HashMap<String, String>();

    @Override
    public Set<String> getAll() {
        return Collections.unmodifiableSet(aliases.keySet());
    }

    @Override
    public void set(String key, String value) {
        synchronized (aliases) {
            if (value == null) {
                aliases.remove(key);
            } else {
                aliases.put(key, value);
            }
        }
    }

    @Override
    public String get(String name) {
        synchronized (aliases) {
            return aliases.get(name);
        }
    }

}
