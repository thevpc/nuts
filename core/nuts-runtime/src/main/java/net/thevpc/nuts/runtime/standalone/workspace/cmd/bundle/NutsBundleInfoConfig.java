package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.util.NStringBuilder;

public class NutsBundleInfoConfig {
    public String target;
    public String appVersion;
    public String appTitle;
    public String appName;
    public String appDesc;

    @Override
    public String toString() {
        NStringBuilder nuts_bundle_info_config = new NStringBuilder();
        if (target != null) {
            nuts_bundle_info_config.println("target=" + target);
        }
        if (appVersion != null) {
            nuts_bundle_info_config.println("version=" + appVersion);
        }
        if (appTitle != null) {
            nuts_bundle_info_config.println("title=" + appTitle);
        }
        if (appName != null) {
            nuts_bundle_info_config.println("name=" + appName);
        }
        if (appDesc != null) {
            nuts_bundle_info_config.println("description=" + appDesc);
        }
        return nuts_bundle_info_config.toString();
    }

}
