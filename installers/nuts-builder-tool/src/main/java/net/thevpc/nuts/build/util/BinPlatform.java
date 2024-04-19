/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build.util;

/**
 *
 * @author vpc
 */
public enum BinPlatform {
    LINUX32,
    LINUX64,
    WINDOWS32,
    WINDOWS64,
    MAC64;

    public String id() {
        switch (this) {
            case LINUX32:
                return "linux32";
            case LINUX64:
                return "linux64";
            case WINDOWS32:
                return "windows32";
            case WINDOWS64:
                return "windows64";
            case MAC64:
                return "mac64";
            default:
                throw new AssertionError();
        }
    }

}
