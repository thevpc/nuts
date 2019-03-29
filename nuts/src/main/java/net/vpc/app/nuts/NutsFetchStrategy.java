/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 */
public interface NutsFetchStrategy extends Iterable<NutsFetchMode> {

    public static final NutsFetchStrategy OFFLINE = new DefaultNutsFetchStrategy("OFFLINE", true, NutsFetchMode.INSTALLED, NutsFetchMode.LOCAL);
    public static final NutsFetchStrategy WIRED = new DefaultNutsFetchStrategy("WIRED", true, NutsFetchMode.LOCAL, NutsFetchMode.REMOTE);
    public static final NutsFetchStrategy ONLINE = new DefaultNutsFetchStrategy("ONLINE", true, NutsFetchMode.INSTALLED, NutsFetchMode.LOCAL, NutsFetchMode.REMOTE);
    public static final NutsFetchStrategy ANYWHERE = new DefaultNutsFetchStrategy("ANYWHERE", false, NutsFetchMode.LOCAL, NutsFetchMode.REMOTE);

    public static final NutsFetchStrategy INSTALLED = new DefaultNutsFetchStrategy("INSTALLED", true, NutsFetchMode.INSTALLED);
    public static final NutsFetchStrategy LOCAL = new DefaultNutsFetchStrategy("OFFLINE", true, NutsFetchMode.LOCAL);
    public static final NutsFetchStrategy REMOTE = new DefaultNutsFetchStrategy("REMOTE", true, NutsFetchMode.REMOTE);

    String name();

    NutsFetchMode[] modes();

    boolean isStopFast();

    public static NutsFetchStrategy valueOf(String s) {
        if (s != null) {
            switch (s) {
                case "ONLINE":
                    return ONLINE;
                case "WIRED":
                    return WIRED;
                case "INSTALLED":
                    return INSTALLED;
                case "LOCAL":
                    return LOCAL;
                case "OFFLINE":
                    return OFFLINE;
                case "REMOTE":
                    return REMOTE;
                case "ANYWHERE":
                    return ANYWHERE;
            }
        }
        throw new IllegalArgumentException("Unsupported NutsFetchModeFiltrer " + s);
    }

}
