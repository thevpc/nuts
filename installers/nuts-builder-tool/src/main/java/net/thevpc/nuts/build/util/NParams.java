/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build.util;

/**
 *
 * @author vpc
 */
public class NParams {

    public static NamedStringParam ofString(String name, String defaultValue) {
        return new NamedStringParam(name, defaultValue);
    }
}
