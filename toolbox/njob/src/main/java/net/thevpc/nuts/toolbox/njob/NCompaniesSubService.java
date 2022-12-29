package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.NApplicationContext;

public class NCompaniesSubService {
    private NApplicationContext context;
    private NJobConfigStore dal;
    private JobService service;

    public NCompaniesSubService(NApplicationContext context, NJobConfigStore dal, JobService service) {
        this.context = context;
        this.dal = dal;
        this.service = service;
    }



}
