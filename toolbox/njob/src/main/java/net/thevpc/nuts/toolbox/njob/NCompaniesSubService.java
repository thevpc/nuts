package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.NSession;

public class NCompaniesSubService {
    private NSession session;
    private NJobConfigStore dal;
    private JobService service;

    public NCompaniesSubService(NSession session, NJobConfigStore dal, JobService service) {
        this.session = session;
        this.dal = dal;
        this.service = service;
    }



}
