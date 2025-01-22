/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import net.thevpc.nuts.build.util.ConfReader;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NOptional;

/**
 * @author vpc
 */
public class NutsBuildRunnerContext {

    public Map<String, String> vars = new HashMap<>();
    public boolean keepStamp = false;
    public boolean updateVersion = false;
    public Boolean productionMode = null;
    public String home = System.getProperty("user.home");
    public String remoteTheVpcSshUser = System.getProperty("user.name");
    public String remoteTheVpcSshHost = "thevpc.net";
    public NPath nutsRootFolder;
    public boolean publish;
    public NPath websiteProjectFolder;
    public NPath repositoryProjectFolder;
    public String nutsDebugArg = null;

    public String nutsStableApiVersion = null;
    public String nutsStableAppVersion = null;
    public String nutsStableRuntimeVersion = null;
    private String remoteTheVpcSshConnexion;

    public boolean verbose = false;
    public boolean trace = false;

    public NOptional<String> getRemoteTheVpcSshHost() {
        return NOptional.of(remoteTheVpcSshHost);
    }

    public void setRemoteTheVpcSshHost(String remoteTheVpcSshHost) {
        this.remoteTheVpcSshHost = remoteTheVpcSshHost;
    }

    public NOptional<String> getRemoteTheVpcSshConnexion() {
        return NOptional.of(remoteTheVpcSshConnexion).orElseUse(
                () -> {
                    String s = getRemoteTheVpcSshHost().orElse(System.getProperty("user.home"))
                            + ":"
                            + getRemoteTheVpcSshHost().orElse("thevpc.net");
                    return NOptional.of(s);
                }
        );
    }

    public void setRemoteTheVpcSshConnexion(String remoteTheVpcSshConnexion) {
        this.remoteTheVpcSshConnexion = remoteTheVpcSshConnexion;
    }

    public String getRemoteTheVpcSshUser() {
        return remoteTheVpcSshUser;
    }

    public void setRemoteTheVpcSshUser(String remoteUser) {
        this.remoteTheVpcSshUser = remoteUser;
    }

    public Function<String, Object> varMapper() {
        return new NFunction<String, Object>() {
            @Override
            public Object apply(String s) {
                if (vars.containsKey(s)) {
                    return vars.get(s);
                }
                Properties p = System.getProperties();
                Object r = p.get(s);
                if (r != null) {
                    return r;
                }
                switch (s) {
                    case "remoteTheVpcSshUser": {
                        return getRemoteTheVpcSshUser();
                    }
                    case "stableApiVersion": {
                        return nutsStableApiVersion;
                    }
                    case "stableAppVersion": {
                        return nutsStableAppVersion;
                    }
                    case "stableRuntimeVersion": {
                        return nutsStableRuntimeVersion;
                    }
                    case "root":
                    case "rootFolder":
                    case "nutsRootFolder": {
                        return nutsRootFolder;
                    }
                    case "websiteProjectFolder": {
                        return websiteProjectFolder;
                    }
                    case "repositoryProjectFolder": {
                        return repositoryProjectFolder;
                    }
                }
                return null;
            }
        };
    }

    public void setVar(String key, String value) {
        this.vars.put(key, value);
    }

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
