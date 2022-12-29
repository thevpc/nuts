package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.format.NMutableTableModel;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.njob.model.*;
import net.thevpc.nuts.toolbox.njob.time.*;
import net.thevpc.nuts.util.NRef;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NJobsSubCmd {

    private final JobService service;
    private final NApplicationContext context;
    private final NSession session;
    private final JobServiceCmd parent;

    public NJobsSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.context = parent.context;
        this.service = parent.service;
        this.session = parent.session;
    }

    public void runJobAdd(NCommandLine cmd) {
        NJob t = new NJob();
        NRef<Boolean> list = NRef.of(false);
        NRef<Boolean> show = NRef.of(false);
        while (cmd.hasNext()) {
            NArgument aa = cmd.peek().get();
            switch (aa.key()) {
                case "--list":
                case "-l": {
                    cmd.withNextBoolean((v,a,s)->list.set(true));
                    break;
                }
                case "--show":
                case "-s": {
                    cmd.withNextBoolean((v,a,s)->show.set(true));
                    break;
                }
                case "--time":
                case "--on":
                case "--start":
                case "-t": {
                    cmd.withNextString((v,a,s)->t.setStartTime(new TimeParser().parseInstant(v, false)));
                    break;
                }
                case "--at": {
                    cmd.withNextString((v,a,s)->t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(v, false)));
                    break;
                }
                case "--for":
                case "--project":
                case "-p": {
                    cmd.withNextString((v,a,s)->t.setProject(v));
                    break;
                }
                case "--obs":
                case "-o": {
                    cmd.withNextString((v,a,s)->t.setObservations(v));
                    break;
                }
                case "--duration":
                case "-d": {
                    cmd.withNextString((v,a,s)->t.setDuration(TimePeriod.parse(v, false)));
                    break;
                }
                default: {
                    if (aa.isNonOption()) {
                        if (t.getName() == null) {
                            t.setName(cmd.next().get(session).toString());
                        } else {
                            cmd.throwUnexpectedArgument();
                        }
                    } else {
                        cmd.throwUnexpectedArgument();
                    }
                }
            }
        }
        if (cmd.isExecMode()) {
            service.jobs().addJob(t);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("job %s (%s) added.\n",
                        NTexts.of(context.getSession()).ofStyled(t.getId(), NTextStyle.primary5()),
                        t.getName()
                );
            }
            if (show.get()) {
                runJobShow(NCommandLine.of(new String[]{t.getId()}));
            }
            if (list.get()) {
                runJobList(NCommandLine.of(new String[0]));
            }
        }
    }

    public void runJobUpdate(NCommandLine cmd) {
        class Data {
            List<NJob> jobs = new ArrayList<>();
            boolean list = false;
            boolean show = false;
        }
        Data d = new Data();
        List<Consumer<NJob>> runLater = new ArrayList<>();
        while (cmd.hasNext()) {
            NArgument a = cmd.peek().get(session);
            switch (a.key()) {
                case "--list":
                case "-l": {
                    cmd.withNextBoolean((v, r, s) -> d.list = v);
                    break;
                }
                case "--show":
                case "-s": {
                    cmd.withNextBoolean((v, r, s) -> d.show = v);
                    break;
                }
                case "--start": {
                    cmd.withNextString((v, r, s) -> {
                        Instant vv = new TimeParser().parseInstant(v, false);
                        runLater.add(t -> t.setStartTime(vv));
                    });
                    break;
                }
                case "-t":
                case "--on": {
                    cmd.withNextString((v, r, s) -> {
                        runLater.add(t -> t.setStartTime(TimePeriod.parseOpPeriodAsInstant(v, t.getStartTime(), true)));
                    });
                    break;
                }
                case "--at": {
                    cmd.withNextString((v, r, s) -> {
                        Instant vv = new TimeParser().setTimeOnly(true).parseInstant(v, false);
                        runLater.add(t -> t.setStartTime(vv));
                    });
                    break;
                }
                case "-d":
                case "--duration": {
                    cmd.withNextString((v, r, s) -> {
                        TimePeriod vv = TimePeriod.parse(v, false);
                        runLater.add(t -> t.setDuration(vv));
                    });
                    break;
                }
                case "-n":
                case "--name": {
                    cmd.withNextString((v, r, s) -> {
                        runLater.add(t -> t.setName(v));

                    });
                    break;
                }
                case "-p":
                case "--project": {
                    cmd.withNextString((v, r, s) -> {
                        runLater.add(t -> t.setProject(v));
                    });
                    break;
                }
                case "-o":
                case "--obs": {
                    cmd.withNextString((v, r, s) -> {
                        runLater.add(t -> t.setObservations(v));
                    });
                    break;
                }
                case "-o+":
                case "--obs+":
                case "+obs": {
                    cmd.withNextString((v, r, s) -> {
                        runLater.add(t -> {
                            String ss = t.getObservations();
                            if (ss == null) {
                                ss = "";
                            }
                            ss = ss.trim();
                            if (!ss.isEmpty()) {
                                ss += "\n";
                            }
                            ss += v;
                            ss = ss.trim();
                            t.setObservations(ss);
                        });
                    });
                    break;
                }
                default: {
                    if (a.isNonOption()) {
                        NJob t = findJob(cmd.next().get(session).toString(), cmd);
                        d.jobs.add(t);
                    } else {
                        cmd.throwUnexpectedArgument();
                    }
                }
            }
        }
        if (d.jobs.isEmpty()) {
            cmd.throwError(NMsg.ofNtf("job id expected"));
        }
        if (cmd.isExecMode()) {
            for (NJob job : d.jobs) {
                for (Consumer<NJob> c : runLater) {
                    c.accept(job);
                }
            }
            NTexts text = NTexts.of(context.getSession());
            for (NJob job : new LinkedHashSet<>(d.jobs)) {
                service.jobs().updateJob(job);
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("job %s (%s) updated.\n",
                            text.ofStyled(job.getId(), NTextStyle.primary5()),
                            text.ofStyled(job.getName(), NTextStyle.primary1())
                    );
                }
            }
            if (d.show) {
                for (NJob t : new LinkedHashSet<>(d.jobs)) {
                    runJobList(NCommandLine.of(new String[]{t.getId()}));
                }
            }
            if (d.list) {
                runJobList(NCommandLine.of(new String[0]));
            }
        }
    }

    public boolean runJobCommands(NCommandLine cmd) {
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

    private void runJobRemove(NCommandLine cmd) {
        NTexts text = NTexts.of(context.getSession());
        while (cmd.hasNext()) {
            NArgument a = cmd.next().get(session);
            NJob t = findJob(a.toString(), cmd);
            if (cmd.isExecMode()) {
                if (service.jobs().removeJob(t.getId())) {
                    if (context.getSession().isPlainTrace()) {
                        context.getSession().out().printf("job %s removed.\n",
                                text.ofStyled(a.toString(), NTextStyle.primary5())
                        );
                    }
                } else {
                    context.getSession().out().printf("job %s %s.\n",
                            text.ofStyled(a.toString(), NTextStyle.primary5()),
                            text.ofStyled("not found", NTextStyle.error())
                    );
                }
            }
        }

    }

    private void runJobShow(NCommandLine cmd) {
        while (cmd.hasNext()) {
            NArgument a = cmd.next().get(session);
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

    private void runJobList(NCommandLine cmd) {
        class Data {
            TimespanPattern hoursPerDay = TimespanPattern.WORK;
            int count = 100;
            NJobGroup groupBy = null;
            ChronoUnit countType = null;
            ChronoUnit timeUnit = null;
            Predicate<NJob> whereFilter = null;
        }
        Data d = new Data();
        while (cmd.hasNext()) {
            NArgument a = cmd.peek().get(session);
            switch (a.key()) {
                case "-w":
                case "--weeks": {
                    cmd.withNextValue((v, r, s) -> {
                        d.countType = ChronoUnit.WEEKS;
                        d.count = v.asInt().get(session);
                    });
                    break;
                }
                case "-m":
                case "--months": {
                    cmd.withNextValue((v, r, s) -> {
                        d.countType = ChronoUnit.MONTHS;
                        d.count = v.asInt().get(session);
                    });

                    break;
                }
                case "-l": {
                    cmd.withNextValue((v, r, s) -> {
                        d.countType = null;
                        d.count = v.asInt().get(session);
                    });

                    break;
                }
                case "-u":
                case "--unit": {
                    cmd.withNextString((v, r, s) -> {
                        d.timeUnit = TimePeriod.parseUnit(v, false);
                    });
                    break;
                }
                case "-g":
                case "--group":
                case "--groupBy":
                case "--groupby":
                case "--group-by": {
                    cmd.withNextString((v, r, s) -> {
                        switch (v) {
                            case "p":
                            case "project": {
                                d.groupBy = NJobGroup.PROJECT_NAME;
                                break;
                            }
                            case "n":
                            case "name": {
                                d.groupBy = NJobGroup.NAME;
                                break;
                            }
                            case "s":
                            case "summary": {
                                d.groupBy = NJobGroup.SUMMARY;
                                break;
                            }
                            default: {
                                cmd.pushBack(r).throwUnexpectedArgument(NMsg.ofPlain("invalid value"));
                            }
                        }
                    });
                    break;
                }
                case "-p":
                case "--project": {
                    cmd.withNextString((v, r, s) -> {
                        Predicate<String> sp = parent.createProjectFilter(v);
                        Predicate<NJob> t = x -> sp.test(x.getProject());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "--name": {
                    cmd.withNextString((v, r, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NJob> t = x -> sp.test(x.getName());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "-b":
                case "--beneficiary": {
                    cmd.withNextString((v, r, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NJob> t = x -> {
                            NProject project = service.projects().getProject(x.getProject());
                            return sp.test(project == null ? "" : project.getBeneficiary());
                        };
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "-c":
                case "--company": {
                    cmd.withNextString((v, r, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NJob> t = x -> {
                            NProject project = service.projects().getProject(x.getProject());
                            return sp.test(project == null ? "" : project.getCompany());
                        };
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "-d":
                case "--duration": {
                    cmd.withNextString((v, r, s) -> {
                        Predicate<TimePeriod> p = TimePeriod.parseFilter(v, false);
                        Predicate<NJob> t = x -> p.test(x.getDuration());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time": {
                    cmd.withNextString((v, r, s) -> {
                        Predicate<Instant> t = new TimeParser().parseInstantFilter(v, false);
                        d.whereFilter = parent.appendPredicate(d.whereFilter, x -> t.test(x.getStartTime()));

                    });
                    break;
                }
                default: {
                    cmd.throwUnexpectedArgument();
                }
            }
        }
        if (cmd.isExecMode()) {
            Stream<NJob> r = service.jobs().findLastJobs(null, d.count, d.countType, d.whereFilter, d.groupBy, d.timeUnit, d.hoursPerDay);
            ChronoUnit timeUnit0 = d.timeUnit;
            if (context.getSession().isPlainTrace()) {
                NMutableTableModel m = NMutableTableModel.of(session);
                NJobGroup finalGroupBy = d.groupBy;
                List<NJob> lastResults = new ArrayList<>();
                int[] index = new int[1];
                r.forEach(x -> {
                    NString durationString = NTexts.of(session).ofStyled(String.valueOf(timeUnit0 == null ? x.getDuration() : x.getDuration().toUnit(timeUnit0, d.hoursPerDay)), NTextStyle.keyword());
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
                                    NTexts.of(session).ofStyled(x.getId(), NTextStyle.pale()),
                                    parent.getFormattedDate(x.getStartTime()),
                                    durationString,
                                    parent.getFormattedProject(x.getProject() == null ? "*" : x.getProject()),
                                    x.getName()

                            }
                    );
                });
                context.getSession().setProperty("LastResults", lastResults.toArray(new NJob[0]));
                NTableFormat.of(session)
                        .setBorder("spaces")
                        .setValue(m).println();
            } else {
                context.getSession().out().printf(r.collect(Collectors.toList()));
            }
        }
    }

    private NJob findJob(String pid, NCommandLine cmd) {
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
            cmd.throwError(NMsg.ofCstyle("job not found: %s", pid));
        }
        return t;
    }
}
