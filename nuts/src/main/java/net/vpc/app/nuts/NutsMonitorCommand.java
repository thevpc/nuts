/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.InputStream;

/**
 *
 * @author vpc
 */
public interface NutsMonitorCommand {

    NutsMonitorCommand session(NutsTerminalProvider s);

    NutsMonitorCommand setSession(NutsTerminalProvider s);

    NutsTerminalProvider getSession();

    NutsMonitorCommand name(String s);

    NutsMonitorCommand setName(String s);

    String getName();

    NutsMonitorCommand origin(Object s);

    NutsMonitorCommand setOrigin(Object s);

    Object getOrigin();

    NutsMonitorCommand length(long len);

    NutsMonitorCommand setLength(long len);

    long getLength();

    NutsMonitorCommand source(String path);

    NutsMonitorCommand setSource(String path);

    NutsMonitorCommand source(InputStream path);

    NutsMonitorCommand setSource(InputStream path);

    InputStream create();
}
