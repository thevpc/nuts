/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author vpc
 */
public class NutsSdkLocation implements Serializable {

    public static final long serialVersionUID = 1;
    private String type;
    private String name;
    private String path;
    private String version;

    public NutsSdkLocation(String type,String name, String path, String version) {
        this.type = type;
        this.name = name;
        this.path = path;
        this.version = version;
    }

    public NutsSdkLocation() {
    }

    public String getType() {
        return type;
    }

    public NutsSdkLocation setType(String type) {
        this.type = type;
        return this;
    }

    public NutsSdkLocation setName(String name) {
        this.name = name;
        return this;
    }

    public NutsSdkLocation setPath(String path) {
        this.path = path;
        return this;
    }

    public NutsSdkLocation setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }



}
