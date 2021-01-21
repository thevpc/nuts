package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.toolbox.njob.model.NJob;
import net.thevpc.nuts.toolbox.njob.model.NProject;
import net.thevpc.nuts.toolbox.njob.time.TimePeriod;
import net.thevpc.nuts.toolbox.njob.time.WeekDay;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

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
