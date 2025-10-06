package net.thevpc.nuts.artifact;

import java.util.Map;

public interface NDescriptorLicense {

    String getName();

    String getDate();

    String getUrl();

    String getDistribution();

    String getComments();

    String getId();

    Map<String, String> getProperties();
    NDescriptorLicense readOnly();

    NDescriptorLicenseBuilder builder();
}
