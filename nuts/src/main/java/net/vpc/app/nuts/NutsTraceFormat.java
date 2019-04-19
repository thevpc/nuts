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
public interface NutsTraceFormat {

    public Object format(NutsId object, NutsOutputFormat type, NutsWorkspace ws);

    public Object format(NutsDefinition object, NutsOutputFormat type, NutsWorkspace ws);
}
