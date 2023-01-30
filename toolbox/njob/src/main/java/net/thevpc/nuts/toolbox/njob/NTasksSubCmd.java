package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.format.NMutableTableModel;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.njob.model.*;
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

public class NTasksSubCmd {

    private JobService service;
    private NApplicationContext context;
    private NSession session;
    private JobServiceCmd parent;

    public NTasksSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.context = parent.context;
        this.service = parent.service;
        this.session = parent.session;
    }

    public void runTaskAdd(NCommandLine cmd) {
        boolean list = false;
        boolean show = false;
        boolean nameVisited = false;
        NArg a;
        List<Consumer<NTask>> runLater = new ArrayList<>();
        while (cmd.hasNext()) {
            if ((a = cmd.nextFlag("--list", "-l").orNull()) != null) {
                list = a.getBooleanValue().get(session);
            } else if ((a = cmd.nextFlag("--show", "-s").orNull()) != null) {
                show = a.getBooleanValue().get(session);
            } else if ((a = cmd.nextEntry("--on", "--due", "-t").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setDueTime(new TimeParser().parseInstant(s, false)));
            } else if ((a = cmd.nextEntry("--at").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setDueTime(new TimeParser().setTimeOnly(true).parseInstant(s, false)));
            } else if ((a = cmd.nextEntry("--start").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setStartTime(new TimeParser().parseInstant(s, false)));
            } else if ((a = cmd.nextEntry("--end").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setEndTime(new TimeParser().parseInstant(s, false)));
            } else if ((a = cmd.nextEntry("--for").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    Instant u = new TimeParser().parseInstant(s, true);
                    if (u != null) {
                        t.setDueTime(u);
                    } else {
                        t.setProject(s);
                    }
                });
            } else if ((a = cmd.nextEntry("-p", "--project").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setProject(s));
            } else if ((a = cmd.nextEntry("-n", "--name").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setName(s));
            } else if ((a = cmd.nextEntry("-f", "--flag").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    String v = s;
                    NFlag f = null;
                    if ("random".equalsIgnoreCase(v)) {
                        f = NFlag.values()[(int) (Math.random() * NFlag.values().length)];
                    } else {
                        f = NFlag.valueOf(v.toUpperCase());
                    }
                    t.setFlag(f);
                });
            } else if ((a = cmd.nextEntry("-j", "--job").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    String jobId = s;
                    NJob job = service.jobs().getJob(jobId);
                    if (job == null) {
                        cmd.throwError(NMsg.ofC("invalid job %s", jobId));
                    }
                    t.setJobId(job.getId());
                });
            } else if ((a = cmd.nextEntry("-T", "--parent").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    String taskId = s;
                    NTask parentTask = service.tasks().getTask(taskId);
                    if (parentTask == null) {
                        cmd.throwError(NMsg.ofC("invalid parent task %s", taskId));
                    }
                    t.setParentTaskId(parentTask.getId());
                });
            } else if ((a = cmd.nextEntry("-P", "--priority").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    String v = s;
                    NPriority p = NPriority.NORMAL;
                    if (v.equalsIgnoreCase("higher")) {
                        p = p.higher();
                    } else if (v.equalsIgnoreCase("lower")) {
                        p = p.lower();
                    } else {
                        p = NPriority.valueOf(v.toLowerCase());
                    }
                    t.setPriority(p);
                });
            } else if ((a = cmd.nextEntry("-o", "--obs").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    t.setObservations(s);
                });
            } else if ((a = cmd.nextEntry("-d", "--duration").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    t.setDuration(TimePeriod.parse(s, true));
                });
            } else if ((a = cmd.next("--wip").orNull()) != null) {
                runLater.add(t -> {
                    t.setStatus(NTaskStatus.WIP);
                });
            } else if ((a = cmd.next("--done").orNull()) != null) {
                runLater.add(t -> t.setStatus(NTaskStatus.DONE));
            } else if ((a = cmd.next("--cancel").orNull()) != null) {
                runLater.add(t -> t.setStatus(NTaskStatus.CANCELLED));
            } else if ((a = cmd.next("--todo").orNull()) != null) {
                runLater.add(t -> t.setStatus(NTaskStatus.TODO));
            } else if ((a = cmd.next("--high").orNull()) != null) {
                runLater.add(t -> t.setPriority(NPriority.HIGH));
            } else if ((a = cmd.next("--critical").orNull()) != null) {
                runLater.add(t -> t.setPriority(NPriority.CRITICAL));
            } else if ((a = cmd.next("--normal").orNull()) != null) {
                runLater.add(t -> t.setPriority(NPriority.NORMAL));
            } else {
                if (cmd.peek().get(session).isNonOption() && !nameVisited) {
                    String n = cmd.next("name").flatMap(NLiteral::asString).get(session);
                    runLater.add(t -> t.setName(n));
                } else {
                    cmd.throwUnexpectedArgument();
                }
            }
        }
        if (cmd.isExecMode()) {
            NTask t = new NTask();
            for (Consumer<NTask> c : runLater) {
                c.accept(t);
            }
            service.tasks().addTask(t);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().println(NMsg.ofC("task %s (%s) added.",
                        NTexts.of(context.getSession()).ofStyled(t.getId(), NTextStyle.primary5()),
                        t.getName()
                ));
            }
            if (show) {
                runTaskShow(NCommandLine.of(new String[]{t.getId()}));
            }
            if (list) {
                runTaskList(NCommandLine.of(new String[0]));
            }
        }
    }

    public void runTaskUpdate(NCommandLine cmd) {
        class Data{
            List<NTask> tasks = new ArrayList<>();
            boolean list = false;
            boolean show = false;
            List<Consumer<NTask>> runLater = new ArrayList<>();
        }
        Data d=new Data();
        while (cmd.hasNext()) {
            NArg aa = cmd.peek().get(session);
            switch(aa.key()) {
                case "--list":
                case "-l": {
                    cmd.withNextFlag((v, a, s) -> d.list=v);
                    break;
                }
                case "--show":
                case "-s": {
                    cmd.withNextFlag((v, a, s) -> d.show=v);
                    break;
                }
                case "--start": {
                    cmd.withNextEntry((v, a, s) -> d.runLater.add(t -> t.setStartTime(new TimeParser().parseInstant(v, false))));
                    break;
                }
                case "-t":
                case "--on":
                case "--due": {
                    cmd.withNextEntry((v, a, s) -> d.runLater.add(t -> t.setDueTime(TimePeriod.parseOpPeriodAsInstant(v, t.getDueTime(), true))));
                    break;
                }
                case "--at": {
                    cmd.withNextEntry((v, a, s) -> d.runLater.add(t -> t.setDueTime(new TimeParser().setTimeOnly(true).parseInstant(v, false))));
                    break;
                }
                case "--end": {
                    cmd.withNextEntry((v, a, s) -> d.runLater.add(t -> t.setEndTime(new TimeParser().parseInstant(v, false))));
                    break;
                }
                case "--wip": {
                    cmd.withNextTrueFlag((v, a, s) -> d.runLater.add(t -> t.setStatus(NTaskStatus.WIP)));
                    break;
                }
                case "--done": {
                    cmd.withNextTrueFlag((v, a, s) -> d.runLater.add(t -> t.setStatus(NTaskStatus.DONE)));
                    break;
                }
                case "--cancel": {
                    cmd.withNextTrueFlag((v, a, s) -> d.runLater.add(t -> t.setStatus(NTaskStatus.CANCELLED)));
                    break;
                }
                case "--todo": {
                    cmd.withNextTrueFlag((v, a, s) -> d.runLater.add(t -> t.setStatus(NTaskStatus.TODO)));
                    break;
                }
                case "--high": {
                    cmd.withNextTrueFlag((v, a, s) -> d.runLater.add(t -> t.setPriority(NPriority.HIGH)));
                    break;
                }
                case "--critical": {
                    cmd.withNextTrueFlag((v, a, s) -> d.runLater.add(t -> t.setPriority(NPriority.CRITICAL)));
                    break;
                }
                case "--normal": {
                    cmd.withNextTrueFlag((v, a, s) -> d.runLater.add(t -> t.setPriority(NPriority.NORMAL)));
                    break;
                }
                case "++P":
                case "++prio":
                case "--prio++": {
                    cmd.withNextTrueFlag((v, a, s) -> d.runLater.add(t -> t.setPriority((t.getPriority() == null ? NPriority.NORMAL : t.getPriority()).higher())));
                    break;
                }
                case "--P":
                case "--prio":
                case "--prio--": {
                    cmd.withNextEntry((v, a, s) -> {
                        if (!aa.key().equals("--prio")) {
                            v = null;
                        }
                        if (v == null) {
                            d.runLater.add(t -> t.setPriority((t.getPriority() == null ? NPriority.NORMAL : t.getPriority()).lower()));
                        } else {
                            NPriority pp = NPriority.parse(v);
                            d.runLater.add(t -> t.setPriority(pp));
                        }
                    });
                    break;
                }
                case "--status": {
                    cmd.withNextEntry((v, a, s) -> {
                        d.runLater.add(t -> t.setStatus(NTaskStatus.parse(v)));
                    });
                    break;
                }
                case "-d":
                case "--duration": {
                    cmd.withNextEntry((v, a, s) -> {
                        d.runLater.add(t -> t.setDuration(TimePeriod.parse(v, false)));
                    });
                    break;
                }
                case "-n":
                case "--name": {
                    cmd.withNextEntry((v, a, s) -> {
                        d.runLater.add(t -> t.setName(v));
                    });
                    break;
                }
                case "-f":
                case "--flag": {
                    cmd.withNextEntry((v, a, s) -> {
                        NFlag f = NFlag.parse(v);
                        d.runLater.add(t -> t.setFlag(f));
                    });
                    break;
                }
                case "-j":
                case "--job": {
                    cmd.withNextEntry((v, a, s) -> {
                        NJob job = service.jobs().getJob(v);
                        if (job == null) {
                            cmd.throwError(NMsg.ofC("invalid job %s", v));
                        }
                        d.runLater.add(t -> t.setJobId(job.getId()));
                    });
                    break;
                }
                case "-T":
                case "--parent": {
                    cmd.withNextEntry((v, a, s) -> {
                        NTask parentTask = service.tasks().getTask(v);
                        if (parentTask == null) {
                            cmd.throwError(NMsg.ofC("invalid parent task %s", v));
                        }
                        d.runLater.add(t -> t.setParentTaskId(parentTask.getId()));
                    });
                    break;
                }
                case "-P":
                case "--priority": {
                    cmd.withNextEntry((v, a, s) -> {
                        d.runLater.add(t -> {
                            NPriority p = t.getPriority();
                            if (v.equalsIgnoreCase("higher")) {
                                p = p.higher();
                            } else if (v.equalsIgnoreCase("lower")) {
                                p = p.lower();
                            } else {
                                p = NPriority.parse(v);
                            }
                            t.setPriority(p);
                        });
                    });
                    break;
                }
                case "--for": {
                    cmd.withNextEntry((v, a, s) -> {
                        d.runLater.add(t -> {
                            Instant u = TimePeriod.parseOpPeriodAsInstant(v, t.getDueTime(), true);
                            if (u != null) {
                                t.setDueTime(u);
                            } else {
                                t.setProject(v);
                            }
                        });
                    });
                    break;
                }
                case "-p":
                case "--project": {
                    cmd.withNextEntry((v, a, s) -> {
                        d.runLater.add(t -> t.setProject(v));
                    });
                    break;
                }
                case "-o":
                case "--obs": {
                    cmd.withNextEntry((v, a, s) -> {
                        d.runLater.add(t -> t.setObservations(v));
                    });
                    break;
                }
                case "-o+":
                case "--obs+":
                case "+obs": {
                    cmd.withNextEntry((v, a, s) -> {
                        d.runLater.add(t -> {
                            String so = t.getObservations();
                            if (so == null) {
                                so = "";
                            }
                            so = so.trim();
                            if (!so.isEmpty()) {
                                so += "\n";
                            }
                            so += v;
                            so = so.trim();
                            t.setObservations(so);
                        });
                    });
                    break;
                }
                default: {
                    if (aa.isNonOption()) {
                        String pid = cmd.next().get(session).toString();
                        NTask t = findTask(pid, cmd);
                        d.tasks.add(t);
                    } else {
                        cmd.throwUnexpectedArgument();
                    }
                }
            }
        }
        if (d.tasks.isEmpty()) {
            cmd.throwError(NMsg.ofNtf("task id expected"));
        }
        if (cmd.isExecMode()) {
            for (NTask task : d.tasks) {
                for (Consumer<NTask> c : d.runLater) {
                    c.accept(task);
                }
            }
            NTexts text = NTexts.of(context.getSession());
            for (NTask task : new LinkedHashSet<>(d.tasks)) {
                service.tasks().updateTask(task);
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().println(NMsg.ofC("task %s (%s) updated.",
                            text.ofStyled(task.getId(), NTextStyle.primary5()),
                            text.ofStyled(task.getName(), NTextStyle.primary1())
                    ));
                }
            }
            if (d.show) {
                for (NTask t : new LinkedHashSet<>(d.tasks)) {
                    runTaskList(NCommandLine.of(new String[]{t.getId()}));
                }
            }
            if (d.list) {
                runTaskList(NCommandLine.of(new String[0]));
            }
        }
    }

    private void runTaskList(NCommandLine cmd) {
        class Data{
            TimespanPattern hoursPerDay = TimespanPattern.WORK;
            int count = 100;
            NJobGroup groupBy = null;
            ChronoUnit countType = null;
            ChronoUnit timeUnit = null;
            Predicate<NTask> whereFilter = null;
            NTaskStatusFilter status = null;
        }
        Data d=new Data();
        while (cmd.hasNext()) {
            NArg aa = cmd.peek().get(session);
            switch(aa.key()) {
                case "-w":
                case "--weeks": {
                    d.countType = ChronoUnit.WEEKS;
                    d.count = cmd.nextEntry().get(session).getValue().asInt().get(session);
                    break;
                }
                case "-m":
                case "--months": {
                    d.countType = ChronoUnit.MONTHS;
                    d.count = cmd.nextEntry().get(session).getValue().asInt().get(session);
                    break;
                }
                case "-l": {
                    d.countType = null;
                    d.count = cmd.nextEntry().get(session).getValue().asInt().get(session);
                    break;
                }
                case "-u":
                case "--unit": {
                    cmd.withNextEntry((v, a, s) -> {
                        d.timeUnit = TimePeriod.parseUnit(v, false);
                    });
                    break;
                }
                case "--todo": {
                    cmd.nextEntry();
                    d.status = NTaskStatusFilter.TODO;
                    break;
                }
                case "-a":
                case "--all": {
                    cmd.nextEntry();
                    d.status = NTaskStatusFilter.ALL;
                    break;
                }
                case "-r":
                case "--recent": {
                    cmd.nextEntry();
                    d.status = NTaskStatusFilter.RECENT;
                    break;
                }
                case "--cancelled": {
                    cmd.nextEntry();
                    d.status = NTaskStatusFilter.CANCELLED;
                    break;
                }
                case "--closed": {
                    cmd.nextEntry();
                    d.status = NTaskStatusFilter.CLOSED;
                    break;
                }
                case "--wip": {
                    cmd.nextEntry();
                    d.status = NTaskStatusFilter.WIP;
                    break;
                }
                case "-o":
                case "--open": {
                    cmd.nextEntry();
                    d.status = NTaskStatusFilter.OPEN;
                    break;
                }
                case "-g":
                case "--group":
                case "--groupBy":
                case "--groupby":
                case "--group-by": {
                    NArg y = cmd.nextEntry().get(session);
                    switch (y.getStringValue().get(session)) {
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
                            cmd.pushBack(y).throwUnexpectedArgument(NMsg.ofPlain("invalid value"));
                        }
                    }
                    break;
                }
                case "--project":
                case "-p": {
                    cmd.withNextEntry((v, a, s) -> {
                        Predicate<String> sp = parent.createProjectFilter(v);
                        Predicate<NTask> t = x -> sp.test(x.getProject());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "-n":
                case "--name": {
                    cmd.withNextEntry((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NTask> t = x -> sp.test(x.getName());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "-b":
                case "--beneficiary": {
                    cmd.withNextEntry((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NTask> t = x -> {
                            NProject project = service.projects().getProject(x.getProject());
                            return sp.test(project == null ? "" : project.getBeneficiary());
                        };
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "-c":
                case "--company": {
                    cmd.withNextEntry((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NTask> t = x -> {
                            NProject project = service.projects().getProject(x.getProject());
                            return sp.test(project == null ? "" : project.getCompany());
                        };
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "-d":
                case "--duration": {
                    cmd.withNextEntry((v, a, s) -> {
                        Predicate<TimePeriod> p = TimePeriod.parseFilter(v, false);
                        Predicate<NTask> t = x -> p.test(x.getDuration());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    });
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time": {
                    cmd.withNextEntry((v, a, s) -> {
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
            Stream<NTask> r = service.tasks().findTasks(d.status, null, d.count, d.countType, d.whereFilter, d.groupBy, d.timeUnit, d.hoursPerDay);

            if (context.getSession().isPlainTrace()) {
                NMutableTableModel m = NMutableTableModel.of(session);
                List<NTask> lastResults = new ArrayList<>();
                int[] index = new int[1];
                r.forEach(x -> {
                    index[0]++;
                    m.newRow().addCells(toTaskRowArray(x,
                            parent.createHashId(index[0], -1)
                    ));
                    lastResults.add(x);
                });
                context.getSession().setProperty("LastResults", lastResults.toArray(new NTask[0]));
                NTableFormat.of(session)
                        .setBorder("spaces")
                        .setValue(m).println();
            } else {
                context.getSession().out().print(r.collect(Collectors.toList()));
            }
        }
    }

    private Object[] toTaskRowArray(NTask x, String index) {
        String project = x.getProject();
        NProject p = project == null ? null : service.projects().getProject(project);
        NTaskStatus s = x.getStatus();
        String dte0 = parent.getFormattedDate(x.getDueTime());
        NTextBuilder dte = NTexts.of(session).ofBuilder();
        if (s == NTaskStatus.CANCELLED || s == NTaskStatus.DONE) {
            dte.append(dte0, NTextStyle.pale());
        } else if (x.getDueTime() != null && x.getDueTime().compareTo(Instant.now()) < 0) {
            dte.append(dte0, NTextStyle.error());
        } else {
            dte.append(dte0, NTextStyle.keyword(2));
        }
        String projectName = p != null ? p.getName() : project != null ? project : "*";
        return new Object[]{
            index,
            NTexts.of(session).ofBuilder().append(x.getId(), NTextStyle.pale()),
            parent.getFlagString(x.getFlag()),
            parent.getStatusString(x.getStatus()),
            parent.getPriorityString(x.getPriority()),
            dte.immutable(),
            parent.getFormattedProject(projectName),
            x.getName()
        };
    }

    private void runTaskRemove(NCommandLine cmd) {
        NTexts text = NTexts.of(context.getSession());
        while (cmd.hasNext()) {
            NArg a = cmd.next().get(session);
            if (cmd.isExecMode()) {
                NTask t = findTask(a.toString(), cmd);
                if (service.tasks().removeTask(t.getId())) {
                    if (context.getSession().isPlainTrace()) {
                        context.getSession().out().println(NMsg.ofC("task %s removed.",
                                text.ofStyled(a.toString(), NTextStyle.primary5())
                        ));
                    }
                } else {
                    context.getSession().out().println(NMsg.ofC("task %s %s.",
                            text.ofStyled(a.toString(), NTextStyle.primary5()),
                            text.ofStyled("not found", NTextStyle.error())
                    ));
                }
            }
        }

    }

    private void runTaskShow(NCommandLine cmd) {
        while (cmd.hasNext()) {
            NArg a = cmd.next().get(session);
            if (cmd.isExecMode()) {
                NTask task = findTask(a.toString(), cmd);
                NPrintStream out = context.getSession().out();
                if (task == null) {
                    out.println(NMsg.ofC("```kw %s```: ```error not found```.",
                            a.toString()
                    ));
                } else {
                    out.println(NMsg.ofC("```kw %s```:",
                            task.getId()
                    ));
                    String prefix = "\t                    ";
                    out.println(NMsg.ofC("\t```kw2 task name```     : %s", JobServiceCmd.formatWithPrefix(task.getName(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 status```        : %s", JobServiceCmd.formatWithPrefix(task.getStatus(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 priority```      : %s", JobServiceCmd.formatWithPrefix(task.getPriority(), prefix)));
                    String project = task.getProject();
                    NProject p = service.projects().getProject(project);
                    if (project == null || project.length() == 0) {
                        out.println(NMsg.ofC("\t```kw2 project```       : %s", ""));
                    } else {
                        out.println(NMsg.ofC("\t```kw2 project```       : %s (%s)", project, JobServiceCmd.formatWithPrefix((p == null ? "?" : p.getName()), prefix)));
                    }
                    out.println(NMsg.ofC("\t```kw2 flag```          : %s", JobServiceCmd.formatWithPrefix(task.getFlag(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 parent id```     : %s", JobServiceCmd.formatWithPrefix(task.getParentTaskId(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 job id```        : %s", JobServiceCmd.formatWithPrefix(task.getJobId(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 due time```      : %s", JobServiceCmd.formatWithPrefix(task.getDueTime(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 start time```    : %s", JobServiceCmd.formatWithPrefix(task.getStartTime(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 end time```      : %s", JobServiceCmd.formatWithPrefix(task.getEndTime(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 duration```      : %s", JobServiceCmd.formatWithPrefix(task.getDuration(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 duration extra```: %s", JobServiceCmd.formatWithPrefix(task.getInternalDuration(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 creation time``` : %s", JobServiceCmd.formatWithPrefix(task.getCreationTime(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 modif. time```   : %s", JobServiceCmd.formatWithPrefix(task.getModificationTime(), prefix)));
                    out.println(NMsg.ofC("\t```kw2 observations```  : %s", JobServiceCmd.formatWithPrefix(task.getObservations(), prefix)));
                }
            }
        }
    }

    public boolean runTaskCommands(NCommandLine cmd) {
        if (cmd.next("a t", "t a", "ta", "at", "add task", "tasks add").isPresent()) {
            runTaskAdd(cmd);
            return true;
        } else if (cmd.next("t u", "u t", "tu", "ut", "update task", "tasks update").isPresent()) {
            runTaskUpdate(cmd);
            return true;
        } else if (cmd.next("l t", "t l", "lt", "tl", "list tasks", "tasks list").isPresent()) {
            runTaskList(cmd);
            return true;
        } else if (cmd.next("tr", "rt", "trm", "rmt", "t r", "r t", "t rm", "rm t", "remove task", "remove tasks", "rm task", "rm tasks",
                "tasks remove", "tasks rm").isPresent()) {
            runTaskRemove(cmd);
            return true;
        } else if (cmd.next("st", "ts", "s t", "t s", "show task", "show tasks", "tasks show").isPresent()) {
            runTaskShow(cmd);
            return true;
        } else if (cmd.next("t", "tasks").isPresent()) {
            if (cmd.next("--help") != null) {
                parent.showCustomHelp("njob-tasks");
            } else {
                runTaskList(cmd);
            }
            return true;
        }
        return false;
    }

    private NTask findTask(String pid, NCommandLine cmd) {
        NTask t = null;
        if (pid.startsWith("#")) {
            int x = NLiteral.of(pid.substring(1)).asInt().orElse(-1);
            if (x >= 1) {
                Object lastResults = context.getSession().getProperty("LastResults");
                if (lastResults instanceof NTask[] && x <= ((NTask[]) lastResults).length) {
                    t = ((NTask[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.tasks().getTask(pid);
        }
        if (t == null) {
            cmd.throwError(NMsg.ofC("task not found: %s", pid));
        }
        return t;
    }

}
