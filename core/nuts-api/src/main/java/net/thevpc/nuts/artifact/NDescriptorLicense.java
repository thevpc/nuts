package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NGetter;

import java.util.Map;

public interface NDescriptorLicense {

    @NGetter
    String name();

    @NGetter
    String date();

    @NGetter
    String url();

    @NGetter
    String distribution();

    @NGetter
    String comments();

    @NGetter
    String id();

    @NGetter
    Map<String, String> properties();

    NDescriptorLicense readOnly();

    NDescriptorLicenseBuilder builder();
}
