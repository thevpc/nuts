/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.security;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author vpc
 */
public class OpenWallet {

    private String rootPath;
    private Map<String, String> passwords = new HashMap<>();

    public void clear() {
        rootPath = null;
        passwords.clear();
    }

    public boolean acceptRootPath(String s) {
        return rootPath != null && rootPath.equals(s);
    }

    public void setRootPath(String s) {
        this.rootPath = s;
    }

    public String get(String root, String path) {
        if (root == null || root.length() == 0 || path == null || path.length() == 0) {
            return null;
        }
        if(!acceptRootPath(path)){
            return null;
        }
        return passwords.get(path);
    }
    
    public void store(String root, String path, String value) {
        if (root == null || root.length() == 0 || path == null || path.length() == 0) {
            return;
        }
        if (acceptRootPath(root)) {
            passwords.put(path, value);
        } else {
            clear();
            setRootPath(root);
            passwords.put(path, value);
        }
    }

}
