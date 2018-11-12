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
public class TestNutsStartupPerf {
    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            Nuts.main(new String[]{"--version"});
        }
    }
}
