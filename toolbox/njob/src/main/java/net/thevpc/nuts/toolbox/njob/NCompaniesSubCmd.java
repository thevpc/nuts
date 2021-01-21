package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.njob.model.NJob;
import net.thevpc.nuts.toolbox.njob.model.NJobGroup;
import net.thevpc.nuts.toolbox.njob.model.NProject;
import net.thevpc.nuts.toolbox.njob.time.TimeParser;
import net.thevpc.nuts.toolbox.njob.time.TimePeriod;
import net.thevpc.nuts.toolbox.njob.time.TimespanPattern;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NCompaniesSubCmd {
    private JobService service;
    private NutsApplicationContext context;
    private NutsWorkspace ws;
    private JobServiceCmd parent;

    public NCompaniesSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.context = parent.context;
        this.service = parent.service;
        this.ws = parent.ws;
    }
}
