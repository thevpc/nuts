package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;

public class NCompaniesSubCmd {
    private JobService service;
    private NSession session;
    private JobServiceCmd parent;

    public NCompaniesSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.service = parent.service;
        this.session = parent.session;
    }
}
