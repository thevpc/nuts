/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build;

import java.util.HashMap;
import java.util.Map;

import net.thevpc.nuts.build.util.ConfReader;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;

/**
 * @author vpc
 */
public class NutsBuildRunnerContext {

    public Map<String, String> vars = new HashMap<>();
    public boolean keepStamp = false;
    public boolean updateVersion = false;
    public Boolean productionMode = null;
    public String home = System.getProperty("user.home");
    public String user = System.getProperty("user.name");
    public NPath root;
    public boolean publish;
    public NPath NUTS_WEBSITE_BASE;
    public String NUTS_DEBUG_ARG = null;
    public String nutsStableVersion = null;
    public boolean verbose = false;
    public boolean trace = false;


    public void loadConfig(NPath conf, NCmdLine cmdLine) {
        for (Map.Entry<String, String> e : ConfReader.readEntries(conf)) {
            if ("OPTIONS".equals(e.getKey())) {
                cmdLine.addAll(NCmdLine.parseDefault(e.getValue()).get().toStringList());
            } else {
                this.vars.put(e.getKey(), e.getValue());
            }
        }
    }
}
