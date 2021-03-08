package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.NutsApplicationContext;

public class NCompaniesSubService {
    private NutsApplicationContext context;
    private NJobConfigStore dal;
    private JobService service;

    public NCompaniesSubService(NutsApplicationContext context, NJobConfigStore dal, JobService service) {
        this.context = context;
        this.dal = dal;
        this.service = service;
    }



}
