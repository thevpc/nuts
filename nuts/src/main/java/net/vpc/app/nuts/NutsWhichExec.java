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
public interface NutsWhichExec {

    NutsExecutableType getType();

    NutsId getId();

    String getName();

    String getDescription();

}
