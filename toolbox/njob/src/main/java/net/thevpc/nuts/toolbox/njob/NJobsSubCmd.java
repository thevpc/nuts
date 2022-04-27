package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.njob.model.*;
import net.thevpc.nuts.toolbox.njob.time.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NJobsSubCmd {

    private final JobService service;
    private final NutsApplicationContext context;
    private final NutsSession session;
    private final JobServiceCmd parent;

    public NJobsSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.context = parent.context;
        this.service = parent.service;
        this.session = parent.session;
    }

    public void runJobAdd(NutsCommandLine cmd) {
        NJob t = new NJob();
        boolean list = false;
        boolean show = false;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek().get();
            switch (a.getStringKey().orElse("")) {
                case "--list":
                case "-l": {
                    list = cmd.nextBooleanValueLiteral().get(session);
                    break;
                }
                case "--show":
                case "-s": {
                    show = cmd.nextBooleanValueLiteral().get(session);
                    break;
                }
                case "--time":
                case "--on":
                case "--start":
                case "-t": {
                    t.setStartTime(new TimeParser().parseInstant(cmd.nextStringValueLiteral().get(session), false));
                    break;
                }
                case "--at": {
                    t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.nextStringValueLiteral().get(session), false));
                    break;
                }
                case "--for":
                case "--project":
                case "-p": {
                    t.setProject(cmd.nextStringValueLiteral().get(session));
                    break;
                }
                case "--obs":
                case "-o": {
                    t.setObservations(cmd.nextStringValueLiteral().get(session));
                    break;
                }
                case "--duration":
                case "-d": {
                    t.setDuration(TimePeriod.parse(cmd.nextStringValueLiteral().get(session), false));
                    break;
                }
                default: {
                    if (a.isNonOption()) {
                        if (t.getName() == null) {
                            t.setName(cmd.next().get(session).toString());
                        } else {
                            cmd.throwUnexpectedArgument(session);
                        }
                    } else {
                        cmd.throwUnexpectedArgument(session);
                    }
                }
            }
        }
        if (cmd.isExecMode()) {
            service.jobs().addJob(t);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("job %s (%s) added.\n",
                        NutsTexts.of(context.getSession()).ofStyled(t.getId(), NutsTextStyle.primary5()),
                        t.getName()
                );
            }
            if (show) {
                runJobShow(NutsCommandLine.of(new String[]{t.getId()}));
            }
            if (list) {
                runJobList(NutsCommandLine.of(new String[0]));
            }
        }
    }

    public void runJobUpdate(NutsCommandLine cmd) {
        List<NJob> jobs = new ArrayList<>();
        boolean list = false;
        boolean show = false;
        List<Consumer<NJob>> runLater = new ArrayList<>();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek().get(session);
            switch (a.getStringKey().orElse("")) {
                case "--list":
                case "-l": {
                    list = cmd.nextBooleanValueLiteral().get(session);
                    break;
                }
                case "--show":
                case "-s": {
                    show = cmd.nextBooleanValueLiteral().get(session);
                    break;
                }
                case "--start": {
                    Instant v = new TimeParser().parseInstant(cmd.nextStringValueLiteral().get(session), false);
                    runLater.add(t -> t.setStartTime(v));
                    break;
                }
                case "-t":
                case "--on": {
                    String v = cmd.nextStringValueLiteral().get(session);
                    runLater.add(t -> t.setStartTime(TimePeriod.parseOpPeriodAsInstant(v, t.getStartTime(), true)));
                    break;
                }
                case "--at": {
                    Instant v = new TimeParser().setTimeOnly(true).parseInstant(cmd.nextStringValueLiteral().get(session), false);
                    runLater.add(t -> t.setStartTime(v));
                    break;
                }
                case "-d":
                case "--duration": {
                    TimePeriod v = TimePeriod.parse(cmd.nextStringValueLiteral().get(session), false);
                    runLater.add(t -> t.setDuration(v));
                    break;
                }
                case "-n":
                case "--name": {
                    String v = cmd.nextStringValueLiteral().get(session);
                    runLater.add(t -> t.setName(v));
                    break;
                }
                case "-p":
                case "--project": {
                    String v = cmd.nextStringValueLiteral().get(session);
                    runLater.add(t -> t.setProject(v));
                    break;
                }
                case "-o":
                case "--obs": {
                    String v = cmd.nextStringValueLiteral().get(session);
                    runLater.add(t -> t.setObservations(v));
                    break;
                }
                case "-o+":
                case "--obs+":
                case "+obs": {
                    String v = cmd.nextStringValueLiteral().get(session);
                    runLater.add(t -> {
                        String s = t.getObservations();
                        if (s == null) {
                            s = "";
                        }
                        s = s.trim();
                        if (!s.isEmpty()) {
                            s += "\n";
                        }
                        s += v;
                        s = s.trim();
                        t.setObservations(s);
                    });
                    break;
                }
                default: {
                    if (a.isNonOption()) {
                        NJob t = findJob(cmd.next().get(session).toString(), cmd);
                        jobs.add(t);
                    } else {
                        cmd.throwUnexpectedArgument(session);
                    }
                }
            }
        }
        if (jobs.isEmpty()) {
            cmd.throwError(NutsMessage.formatted("job id expected"), session);
        }
        if (cmd.isExecMode()) {
            for (NJob job : jobs) {
                for (Consumer<NJob> c : runLater) {
                    c.accept(job);
                }
            }
            NutsTexts text = NutsTexts.of(context.getSession());
            for (NJob job : new LinkedHashSet<>(jobs)) {
                service.jobs().updateJob(job);
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("job %s (%s) updated.\n",
                            text.ofStyled(job.getId(), NutsTextStyle.primary5()),
                            text.ofStyled(job.getName(), NutsTextStyle.primary1())
                    );
                }
            }
            if (show) {
                for (NJob t : new LinkedHashSet<>(jobs)) {
                    runJobList(NutsCommandLine.of(new String[]{t.getId()}));
                }
            }
            if (list) {
                runJobList(NutsCommandLine.of(new String[0]));
            }
        }
    }

    public boolean runJobCommands(NutsCommandLine cmd) {
        if (cmd.next("aj", "ja", "a j", "j a", "add job", "jobs add").isPresent()) {
            runJobAdd(cmd);
            return true;
        } else if (cmd.next("lj", "jl", "l j", "j l", "list jobs", "jobs list").isPresent()) {
            runJobList(cmd);
            return true;
        } else if (cmd.next("rj", "jr", "jrm", "rmj", "j rm", "rm j", "j r", "r j", "remove job", "remove jobs", "jobs remove").isPresent()) {
            runJobRemove(cmd);
            return true;
        } else if (cmd.next("uj", "ju", "j u", "u j", "update job", "update jobs", "jobs update", "jobs update").isPresent()) {
            runJobUpdate(cmd);
            return true;
        } else if (cmd.next("js", "sj", "j s", "s j", "show job", "show jobs", "jobs show").isPresent()) {
            runJobShow(cmd);
            return true;
        } else if (cmd.next("j", "jobs").isPresent()) {
            if (cmd.next("--help").isPresent()) {
                parent.showCustomHelp("njob-jobs");
            } else {
                runJobList(cmd);
            }
            return true;
        } else {
            return false;
        }
    }

    private void runJobRemove(NutsCommandLine cmd) {
        NutsTexts text = NutsTexts.of(context.getSession());
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next().get(session);
            NJob t = findJob(a.toString(), cmd);
            if (cmd.isExecMode()) {
                if (service.jobs().removeJob(t.getId())) {
                    if (context.getSession().isPlainTrace()) {
                        context.getSession().out().printf("job %s removed.\n",
                                text.ofStyled(a.toString(), NutsTextStyle.primary5())
                        );
                    }
                } else {
                    context.getSession().out().printf("job %s %s.\n",
                            text.ofStyled(a.toString(), NutsTextStyle.primary5()),
                            text.ofStyled("not found", NutsTextStyle.error())
                    );
                }
            }
        }

    }

    private void runJobShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next().get(session);
            if (cmd.isExecMode()) {
                NJob job = findJob(a.toString(), cmd);
                if (job == null) {
                    context.getSession().out().printf("```kw %s```: ```error not found```.\n",
                            a.toString()
                    );
                } else {
                    context.getSession().out().printf("```kw %s```:\n",
                            job.getId()
                    );
                    String prefix = "\t                    ";
                    context.getSession().out().printf("\t```kw2 job name```      : %s:\n", JobServiceCmd.formatWithPrefix(job.getName(), prefix));
                    String project = job.getProject();
                    NProject p = service.projects().getProject(project);
                    if (project == null || project.length() == 0) {
                        context.getSession().out().printf("\t```kw2 project```       : %s\n", "");
                    } else {
                        context.getSession().out().printf("\t```kw2 project```       : %s (%s)\n", project, JobServiceCmd.formatWithPrefix(p == null ? "?" : p.getName(), prefix));
                    }
                    context.getSession().out().printf("\t```kw2 duration```      : %s\n", JobServiceCmd.formatWithPrefix(job.getDuration(), prefix));
                    context.getSession().out().printf("\t```kw2 start time```    : %s\n", JobServiceCmd.formatWithPrefix(job.getStartTime(), prefix));
                    context.getSession().out().printf("\t```kw2 duration extra```: %s\n", JobServiceCmd.formatWithPrefix(job.getInternalDuration(), prefix));
                    context.getSession().out().printf("\t```kw2 observations```  : %s\n", JobServiceCmd.formatWithPrefix(job.getObservations(), prefix));
                }
            }
        }

    }

    private void runJobList(NutsCommandLine cmd) {
        TimespanPattern hoursPerDay = TimespanPattern.WORK;
        int count = 100;
        NJobGroup groupBy = null;
        ChronoUnit countType = null;
        ChronoUnit timeUnit = null;
        Predicate<NJob> whereFilter = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek().get(session);
            switch (a.getStringKey().orElse("")) {
                case "-w":
                case "--weeks": {
                    countType = ChronoUnit.WEEKS;
                    count = cmd.nextString().get(session).getValue().asInt().get(session);
                    break;
                }
                case "-m":
                case "--months": {
                    countType = ChronoUnit.MONTHS;
                    count = cmd.nextString().get(session).getValue().asInt().get(session);
                    break;
                }
                case "-l": {
                    countType = null;
                    count = cmd.nextString().get(session).getValue().asInt().get(session);
                    break;
                }
                case "-u":
                case "--unit": {
                    timeUnit = TimePeriod.parseUnit(cmd.nextStringValueLiteral().get(session), false);
                    break;
                }
                case "-g":
                case "--group":
                case "--groupBy":
                case "--groupby":
                case "--group-by": {
                    NutsArgument y = cmd.nextString().get(session);
                    switch (y.getStringValue().get(session)) {
                        case "p":
                        case "project": {
                            groupBy = NJobGroup.PROJECT_NAME;
                            break;
                        }
                        case "n":
                        case "name": {
                            groupBy = NJobGroup.NAME;
                            break;
                        }
                        case "s":
                        case "summary": {
                            groupBy = NJobGroup.SUMMARY;
                            break;
                        }
                        default: {
                            cmd.pushBack(y, session).throwUnexpectedArgument(NutsMessage.cstyle("invalid value"), session);
                        }
                    }
                    break;
                }
                case "-p":
                case "--project": {
                    String s = cmd.nextStringValueLiteral().get(session);
                    Predicate<String> sp = parent.createProjectFilter(s);
                    Predicate<NJob> t = x -> sp.test(x.getProject());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "--name": {
                    String s = cmd.nextStringValueLiteral().get(session);
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NJob> t = x -> sp.test(x.getName());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-b":
                case "--beneficiary": {
                    String s = cmd.nextStringValueLiteral().get(session);
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NJob> t = x -> {
                        NProject project = service.projects().getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getBeneficiary());
                    };
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-c":
                case "--company": {
                    String s = cmd.nextStringValueLiteral().get(session);
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NJob> t = x -> {
                        NProject project = service.projects().getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getCompany());
                    };
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-d":
                case "--duration": {
                    String s = cmd.nextStringValueLiteral().get(session);
                    Predicate<TimePeriod> p = TimePeriod.parseFilter(s, false);
                    Predicate<NJob> t = x -> p.test(x.getDuration());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time": {
                    String s = cmd.nextStringValueLiteral().get(session);
                    Predicate<Instant> t = new TimeParser().parseInstantFilter(s, false);
                    whereFilter = parent.appendPredicate(whereFilter, x -> t.test(x.getStartTime()));
                    break;
                }
                default: {
                    cmd.throwUnexpectedArgument(session);
                }
            }
        }
        if (cmd.isExecMode()) {
            Stream<NJob> r = service.jobs().findLastJobs(null, count, countType, whereFilter, groupBy, timeUnit, hoursPerDay);
            ChronoUnit timeUnit0 = timeUnit;
            if (context.getSession().isPlainTrace()) {
                NutsMutableTableModel m = NutsMutableTableModel.of(session);
                NJobGroup finalGroupBy = groupBy;
                List<NJob> lastResults = new ArrayList<>();
                int[] index = new int[1];
                r.forEach(x -> {
                    NutsString durationString = NutsTexts.of(session).ofStyled(String.valueOf(timeUnit0 == null ? x.getDuration() : x.getDuration().toUnit(timeUnit0, hoursPerDay)), NutsTextStyle.keyword());
                    index[0]++;
                    lastResults.add(x);
                    m.newRow().addCells(
                            (finalGroupBy != null)
                                    ? new Object[]{
                                    parent.createHashId(index[0], -1),
                                    parent.getFormattedDate(x.getStartTime()),
                                    durationString,
                                    parent.getFormattedProject(x.getProject() == null ? "*" : x.getProject()),
                                    x.getName()

                            } : new Object[]{
                                    parent.createHashId(index[0], -1),
                                    NutsTexts.of(session).ofStyled(x.getId(), NutsTextStyle.pale()),
                                    parent.getFormattedDate(x.getStartTime()),
                                    durationString,
                                    parent.getFormattedProject(x.getProject() == null ? "*" : x.getProject()),
                                    x.getName()

                            }
                    );
                });
                context.getSession().setProperty("LastResults", lastResults.toArray(new NJob[0]));
                NutsTableFormat.of(session)
                        .setBorder("spaces")
                        .setValue(m).println();
            } else {
                context.getSession().out().printf(r.collect(Collectors.toList()));
            }
        }
    }

    private NJob findJob(String pid, NutsCommandLine cmd) {
        NJob t = null;
        if (pid.startsWith("#")) {
            int x = JobServiceCmd.parseIntOrFF(pid.substring(1));
            if (x >= 1) {
                Object lastResults = context.getSession().getProperty("LastResults");
                if (lastResults instanceof NJob[] && x <= ((NJob[]) lastResults).length) {
                    t = ((NJob[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.jobs().getJob(pid);
        }
        if (t == null) {
            cmd.throwError(NutsMessage.cstyle("job not found: %s", pid), session);
        }
        return t;
    }
}
