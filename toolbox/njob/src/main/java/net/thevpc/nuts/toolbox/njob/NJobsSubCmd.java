package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NMutableTableModel;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.njob.model.*;
import net.thevpc.nuts.toolbox.njob.time.*;
import net.thevpc.nuts.util.NMsg;
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
    private final NSession session;
    private final JobServiceCmd parent;

    public NJobsSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.session = parent.session;
        this.service = parent.service;
    }

    public void runJobAdd(NCmdLine cmd) {
        NJob t = new NJob();
        NRef<Boolean> list = NRef.of(false);
        NRef<Boolean> show = NRef.of(false);
        while (cmd.hasNext()) {
            NArg aa = cmd.peek().get();
            switch (aa.key()) {
                case "--list":
                case "-l": {
                    cmd.withNextFlag((v, a)->list.set(true));
                    break;
                }
                case "--show":
                case "-s": {
                    cmd.withNextFlag((v, a)->show.set(true));
                    break;
                }
                case "--time":
                case "--on":
                case "--start":
                case "-t": {
                    cmd.withNextEntry((v, a)->t.setStartTime(new TimeParser().parseInstant(v, false)));
                    break;
                }
                case "--at": {
                    cmd.withNextEntry((v, a)->t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(v, false)));
                    break;
                }
                case "--for":
                case "--project":
                case "-p": {
                    cmd.withNextEntry((v, a)->t.setProject(v));
                    break;
                }
                case "--obs":
                case "-o": {
                    cmd.withNextEntry((v, a)->t.setObservations(v));
                    break;
                }
                case "--duration":
                case "-d": {
                    cmd.withNextEntry((v, a)->t.setDuration(TimePeriod.parse(v, false)));
                    break;
                }
                default: {
                    if (aa.isNonOption()) {
                        if (t.getName() == null) {
                            t.setName(cmd.next().get().toString());
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
            if (session.isPlainTrace()) {
                session.out().println(NMsg.ofC("job %s (%s) added.",
                        NText.ofStyled(t.getId(), NTextStyle.primary5()),
                        t.getName()
                ));
            }
            if (show.get()) {
                runJobShow(NCmdLine.of(new String[]{t.getId()}));
            }
            if (list.get()) {
                runJobList(NCmdLine.of(new String[0]));
            }
        }
    }

    public void runJobUpdate(NCmdLine cmd) {
        class Data {
            List<NJob> jobs = new ArrayList<>();
            boolean list = false;
            boolean show = false;
        }
        Data d = new Data();
        List<Consumer<NJob>> runLater = new ArrayList<>();
        while (cmd.hasNext()) {
            NArg a = cmd.peek().get();
            switch (a.key()) {
                case "--list":
                case "-l": {
                    cmd.withNextFlag((v, r) -> d.list = v);
                    break;
                }
                case "--show":
                case "-s": {
                    cmd.withNextFlag((v, r) -> d.show = v);
                    break;
                }
                case "--start": {
                    cmd.withNextEntry((v, r) -> {
                        Instant vv = new TimeParser().parseInstant(v, false);
                        runLater.add(t -> t.setStartTime(vv));
                    });
                    break;
                }
                case "-t":
                case "--on": {
                    cmd.withNextEntry((v, r) -> {
                        runLater.add(t -> t.setStartTime(TimePeriod.parseOpPeriodAsInstant(v, t.getStartTime(), true)));
                    });
                    break;
                }
                case "--at": {
                    cmd.withNextEntry((v, r) -> {
                        Instant vv = new TimeParser().setTimeOnly(true).parseInstant(v, false);
                        runLater.add(t -> t.setStartTime(vv));
                    });
                    break;
                }
                case "-d":
                case "--duration": {
                    cmd.withNextEntry((v, r) -> {
                        TimePeriod vv = TimePeriod.parse(v, false);
                        runLater.add(t -> t.setDuration(vv));
                    });
                    break;
                }
                case "-n":
                case "--name": {
                    cmd.withNextEntry((v, r) -> {
                        runLater.add(t -> t.setName(v));

                    });
                    break;
                }
                case "-p":
                case "--project": {
                    cmd.withNextEntry((v, r) -> {
                        runLater.add(t -> t.setProject(v));
                    });
                    break;
                }
                case "-o":
                case "--obs": {
                    cmd.withNextEntry((v, r) -> {
                        runLater.add(t -> t.setObservations(v));
                    });
                    break;
                }
                case "-o+":
                case "--obs+":
                case "+obs": {
                    cmd.withNextEntry((v, r) -> {
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
                        NJob t = findJob(cmd.next().get().toString(), cmd);
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
            NTexts text = NTexts.of();
            for (NJob job : new LinkedHashSet<>(d.jobs)) {
                service.jobs().updateJob(job);
                if (session.isPlainTrace()) {
                    session.out().println(NMsg.ofC("job %s (%s) updated.",
                            text.ofStyled(job.getId(), NTextStyle.primary5()),
                            text.ofStyled(job.getName(), NTextStyle.primary1())
                    ));
                }
            }
            if (d.show) {
                for (NJob t : new LinkedHashSet<>(d.jobs)) {
                    runJobList(NCmdLine.of(new String[]{t.getId()}));
                }
            }
            if (d.list) {
                runJobList(NCmdLine.of(new String[0]));
            }
        }
    }

    public boolean runJobCommands(NCmdLine cmd) {
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

    private void runJobRemove(NCmdLine cmd) {
        NTexts text = NTexts.of();
        while (cmd.hasNext()) {
            NArg a = cmd.next().get();
            NJob t = findJob(a.toString(), cmd);
            if (cmd.isExecMode()) {
                if (service.jobs().removeJob(t.getId())) {
                    if (session.isPlainTrace()) {
                        session.out().println(NMsg.ofC("job %s removed.",
                                text.ofStyled(a.toString(), NTextStyle.primary5())
                        ));
                    }
                } else {
                    session.out().println(NMsg.ofC("job %s %s.",
                            text.ofStyled(a.toString(), NTextStyle.primary5()),
                            text.ofStyled("not found", NTextStyle.error())
                    ));
                }
            }
        }

    }

    private void runJobShow(NCmdLine cmd) {
        while (cmd.hasNext()) {
            NArg a = cmd.next().get();
            if (cmd.isExecMode()) {
                NJob job = findJob(a.toString(), cmd);
                NPrintStream out = session.out();
                if (job == null) {
                    out.println(NMsg.ofC("```kw %s```: ```error not found```.",
                            a.toString()
                    ));
                } else {
                    out.println(NMsg.ofC("```kw %s```:",
                            job.getId()
                    ));
                    String prefix = "\t                    ";
                    out.println(NMsg.ofC("\t```kw2 job name```      : %s:", JobServiceCmd.formatWithPrefix(job.getName(), prefix)));
                    String project = job.getProject();
                    NProject p = service.projects().getProject(project);
                    if (project == null || project.length() == 0) {
                        out.println(NMsg.ofC("\t```kw2 project```       : %s", ""));
                    } else {
                        out.println(NMsg.ofC("\t```kw2 project```       : %s (%s)", project, JobServiceCmd.formatWithPrefix(p == null ? "?" : p.getName(), prefix)));
                    }
                    out.println(NMsg.ofC("\t```kw2 duration```      : %s", JobServiceCmd.formatWithPrefix(job.getDuration(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 start time```    : %s", JobServiceCmd.formatWithPrefix(job.getStartTime(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 duration extra```: %s", JobServiceCmd.formatWithPrefix(job.getInternalDuration(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 observations```  : %s", JobServiceCmd.formatWithPrefix(job.getObservations(), prefix)));
                }
            }
        }

    }

    private void runJobList(NCmdLine cmd) {
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
            NArg a = cmd.peek().get();
            switch (a.key()) {
                case "-w":
                case "--weeks": {
                    cmd.withNextEntryValue((v, r) -> {
                        d.countType = ChronoUnit.WEEKS;
                        d.count = v.asInt().get();
                    });
                    break;
                }
                case "-m":
                case "--months": {
                    cmd.withNextEntryValue((v, r) -> {
                        d.countType = ChronoUnit.MONTHS;
                        d.count = v.asInt().get();
                    });

                    break;
                }
                case "-l": {
                    cmd.withNextEntryValue((v, r) -> {
                        d.countType = null;
                        d.count = v.asInt().get();
                    });

                    break;
                }
                case "-u":
                case "--unit": {
                    cmd.withNextEntry((v, r) -> {
                        d.timeUnit = TimePeriod.parseUnit(v, false);
                    });
                    break;
                }
                case "-g":
                case "--group":
                case "--groupBy":
                case "--groupby":
                case "--group-by": {
                    cmd.withNextEntry((v, r) -> {
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
                    cmd.withNextEntry((v, r) -> {
                        Predicate<String> sp = parent.createProjectFilter(v);
                        Predicate<NJob> t = x -> sp.test(x.getProject());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "--name": {
                    cmd.withNextEntry((v, r) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NJob> t = x -> sp.test(x.getName());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "-b":
                case "--beneficiary": {
                    cmd.withNextEntry((v, r) -> {
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
                    cmd.withNextEntry((v, r) -> {
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
                    cmd.withNextEntry((v, r) -> {
                        Predicate<TimePeriod> p = TimePeriod.parseFilter(v, false);
                        Predicate<NJob> t = x -> p.test(x.getDuration());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time": {
                    cmd.withNextEntry((v, r) -> {
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
            if (session.isPlainTrace()) {
                NMutableTableModel m = NMutableTableModel.of();
                NJobGroup finalGroupBy = d.groupBy;
                List<NJob> lastResults = new ArrayList<>();
                int[] index = new int[1];
                r.forEach(x -> {
                    NText durationString = NText.ofStyled(String.valueOf(timeUnit0 == null ? x.getDuration() : x.getDuration().toUnit(timeUnit0, d.hoursPerDay)), NTextStyle.keyword());
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
                                    NText.ofStyled(x.getId(), NTextStyle.pale()),
                                    parent.getFormattedDate(x.getStartTime()),
                                    durationString,
                                    parent.getFormattedProject(x.getProject() == null ? "*" : x.getProject()),
                                    x.getName()

                            }
                    );
                });
                session.setProperty("LastResults", lastResults.toArray(new NJob[0]));
                NTableFormat.of()
                        .setBorder("spaces")
                        .setValue(m).println();
            } else {
                session.out().print(r.collect(Collectors.toList()));
            }
        }
    }

    private NJob findJob(String pid, NCmdLine cmd) {
        NJob t = null;
        if (pid.startsWith("#")) {
            int x = JobServiceCmd.parseIntOrFF(pid.substring(1));
            if (x >= 1) {
                Object lastResults = session.getProperty("LastResults");
                if (lastResults instanceof NJob[] && x <= ((NJob[]) lastResults).length) {
                    t = ((NJob[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.jobs().getJob(pid);
        }
        if (t == null) {
            cmd.throwError(NMsg.ofC("job not found: %s", pid));
        }
        return t;
    }
}
