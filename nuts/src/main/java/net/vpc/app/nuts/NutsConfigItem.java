package net.vpc.app.nuts;

import java.io.Serializable;

/**
 * 
 * @author vpc
 * @category Config
 */
public class NutsConfigItem implements Serializable {
    private static final long serialVersionUID = 1;
    /**
     * Api version having created the config
     */
    private String configVersion = null;

    public String getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }
}
