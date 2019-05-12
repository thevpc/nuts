/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.bridges.maven.mvnutil;

/**
 *
 * @author vpc
 */
class URLPart {

    private String type;
    private String path;

    public URLPart(String type, String path) {
        this.type = type;
        this.path = path;
    }

    public String getName() {
        String n = path;
        final int p = n.lastIndexOf('/');
        if (p > 0) {
            n = path.substring(p + 1);
        }
        return n;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "PathItem{" + "type=" + type + ", path=" + path + '}';
    }

}
