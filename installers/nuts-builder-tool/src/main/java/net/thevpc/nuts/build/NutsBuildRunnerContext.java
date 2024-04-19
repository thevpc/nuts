/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.thevpc.nuts.io.NPath;

/**
 *
 * @author vpc
 */
public class NutsBuildRunnerContext {

    public List<String> options=new ArrayList<String>();
    public Map<String, String> vars = new HashMap<>();
    public boolean keepStamp = false;
    public boolean updateVersion = false;
    public boolean publishNutsPreview = false;
    public Boolean productionMode = null;
    public String home = System.getProperty("user.home");
    public String user = System.getProperty("user.name");
    public String NUTS_INSTALLER_VERSION = "0.8.4.0";
    public NPath NUTS_ROOT_BASE;
    public NPath NUTS_WEBSITE_BASE;

}
