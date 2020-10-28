package net.vpc.app.nuts.toolbox.njob;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsString;
import net.vpc.app.nuts.toolbox.njob.model.*;
import net.vpc.app.nuts.toolbox.njob.time.TimeFormatter;
import net.vpc.app.nuts.toolbox.njob.time.TimeParser;
import net.vpc.app.nuts.toolbox.njob.time.TimePeriod;
import net.vpc.app.nuts.toolbox.njob.time.TimespanPattern;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JobServiceCmd {
    private JobService service;
    private NutsApplicationContext context;

    public JobServiceCmd(NutsApplicationContext context) {
        this.context = context;
        this.service = new JobService(context);
    }


    public void runJobAdd(NutsCommandLine cmd) {
        NJob t = new NJob();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            if (a.getStringKey().equals("--start") || a.getStringKey().equals("--on")) {
                t.setStartTime(new TimeParser().parseInstant(cmd.nextString().getStringValue()));
            } else if (a.getStringKey().equals("--at")) {
                t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue()));
            } else if (a.getStringKey().equals("--project") || a.getStringKey().equals("--for")) {
                t.setProject(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("--desc")) {
                t.setObservations(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("--duration")) {
                t.setDuration(TimePeriod.parse(cmd.nextString().getStringValue(), false));
            } else if (a.isNonOption()) {
                if (t.getName() == null) {
                    t.setName(cmd.next().toString());
                } else {
                    cmd.unexpectedArgument();
                }
            } else {
                cmd.unexpectedArgument();
            }
        }
        service.addJob(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("job <<%s>> added.\n",
                    t.getId()
            );
        }
    }

    public void runTaskAdd(NutsCommandLine cmd) {
        NTask t = new NTask();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            if (a.getStringKey().equals("--on") || a.getStringKey().equals("--due")) {
                t.setDueTime(new TimeParser().parseInstant(cmd.nextString().getStringValue()));
            } else if (a.getStringKey().equals("--at")) {
                t.setDueTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue()));
            } else if (a.getStringKey().equals("--start")) {
                t.setStartTime(new TimeParser().parseInstant(cmd.nextString().getStringValue()));
            } else if (a.getStringKey().equals("--end")) {
                t.setEndTime(new TimeParser().parseInstant(cmd.nextString().getStringValue()));
            } else if (a.getStringKey().equals("--for") || a.getStringKey().equals("--project")) {
                t.setProject(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("--name")) {
                t.setName(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("--flag")) {
                String v = cmd.nextString().getStringValue();
                NFlag f = null;
                if ("random".equalsIgnoreCase(v)) {
                    f = NFlag.values()[(int) (Math.random() * NFlag.values().length)];
                } else {
                    f = NFlag.valueOf(v.toUpperCase());
                }
                t.setFlag(f);
            } else if (a.getStringKey().equals("--job")) {
                String jobId = cmd.nextString().getStringValue();
                NJob job = service.getJob(jobId);
                if (job == null) {
                    cmd.throwError("invalid job " + jobId);
                }
                t.setJobId(job.getId());
            } else if (a.getStringKey().equals("--parent")) {
                String taskId = cmd.nextString().getStringValue();
                NTask parentTask = service.getTask(taskId);
                if (parentTask == null) {
                    cmd.throwError("invalid parent task " + taskId);
                }
                t.setParentTaskId(parentTask.getId());
            } else if (a.getStringKey().equals("--priority")) {
                String v = cmd.nextString().getStringValue();
                NPriority p = NPriority.NORMAL;
                if (v.equalsIgnoreCase("higher")) {
                    p = p.higher();
                } else if (v.equalsIgnoreCase("lower")) {
                    p = p.lower();
                } else {
                    p = NPriority.valueOf(v.toLowerCase());
                }
                t.setPriority(p);
            } else if (a.getStringKey().equals("--desc")) {
                t.setObservations(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("--duration")) {
                t.setDuration(TimePeriod.parse(cmd.nextString().getStringValue(), true));
            } else if (a.isNonOption()) {
                if (t.getName() == null) {
                    t.setName(cmd.next().toString());
                } else {
                    cmd.unexpectedArgument();
                }
            } else {
                cmd.unexpectedArgument();
            }
        }
        service.addTask(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("task <<%s>> added.\n",
                    t.getId()
            );
        }
    }

    public void runProjectAdd(NutsCommandLine cmd) {
        NProject t = new NProject();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            if (a.getStringKey().equals("--start") || a.getStringKey().equals("--on")) {
                t.setStartTime(new TimeParser().parseInstant(cmd.nextString().toString()));
            } else if (a.getStringKey().equals("--at")) {
                t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().toString()));
            } else if (a.getStringKey().equals("--beneficiary") || a.getStringKey().equals("--for")) {
                t.setBeneficiary(cmd.nextString().toString());
            } else if (a.getStringKey().equals("--company") || a.getStringKey().equals("--via")) {
                t.setCompany(cmd.nextString().toString());
            } else if (a.getStringKey().equals("--day1")) {
                t.setStartWeekDay(NDay.valueOf(cmd.nextString().toString().toUpperCase()));
            } else if (a.getStringKey().equals("--obs")) {
                t.setObservations(cmd.nextString().toString());
            } else if (a.isNonOption()) {
                if (t.getName() == null) {
                    t.setName(cmd.next().toString());
                } else {
                    cmd.unexpectedArgument();
                }
            } else {
                cmd.unexpectedArgument();
            }
        }
        service.addProject(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("project <<%s>> added.\n",
                    t.getName()
            );
        }
    }

    public void runProjectUpdate(NutsCommandLine cmd) {
        NProject t = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            if (a.getStringKey().equals("on")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setStartTime(new TimeParser().parseInstant(cmd.nextString().toString()));
            } else if (a.getStringKey().equals("at")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().toString()));
            } else if (a.getStringKey().equals("for")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setBeneficiary(cmd.nextString().toString());
            } else if (a.getStringKey().equals("via")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setCompany(cmd.nextString().toString());
            } else if (a.getStringKey().equals("week")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setStartWeekDay(NDay.valueOf(cmd.nextString().toString().toUpperCase()));
            } else if (a.getStringKey().equals("obs")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setObservations(cmd.nextString().toString());
            } else {
                t = service.getProject(cmd.nextString().toString());
                if (t == null) {
                    cmd.throwError("project not found: " + a.toString());
                }
            }
        }
        if (t == null) {
            cmd.throwError("project name expected");
        }
        service.updateProject(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("project {{%s}} updated.\n",
                    t.getName()
            );
        }
    }

    public void runTaskUpdate(NutsCommandLine cmd) {
        List<NTask> tasks = new ArrayList<>();
        boolean list = false;
        boolean show = false;
        List<Consumer<NTask>> runLater = new ArrayList<>();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            if (a.getStringKey().equals("--list") || a.getStringKey().equals("-l")) {
                list = cmd.nextBoolean().getBooleanValue();
            } else if (a.getStringKey().equals("--show") || a.getStringKey().equals("-s")) {
                show = cmd.nextBoolean().getBooleanValue();
            } else if (a.getStringKey().equals("--start")) {
                Instant v = new TimeParser().parseInstant(cmd.nextString().getStringValue());
                runLater.add(t -> t.setStartTime(v));
            } else if (a.getStringKey().equals("--due") || a.getStringKey().equals("--on")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> t.setDueTime(TimePeriod.parseOpPeriodAsInstant(v,t.getDueTime())));
            } else if (a.getStringKey().equals("--at")) {
                Instant v = new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue());
                runLater.add(t -> t.setDueTime(v));
            } else if (a.getStringKey().equals("--end")) {
                Instant v = new TimeParser().parseInstant(cmd.nextString().getStringValue());
                runLater.add(t -> t.setEndTime(v));
            } else if (a.getStringKey().equals("--wip")) {
                cmd.skip();
                runLater.add(t -> t.setStatus(NTaskStatus.WIP));
            } else if (a.getStringKey().equals("--done")) {
                cmd.skip();
                runLater.add(t -> t.setStatus(NTaskStatus.DONE));
            } else if (a.getStringKey().equals("--cancel") || a.getStringKey().equals("--cancelled")) {
                cmd.skip();
                runLater.add(t -> t.setStatus(NTaskStatus.CANCELLED));
            } else if (a.getStringKey().equals("--todo")) {
                cmd.skip();
                runLater.add(t -> t.setStatus(NTaskStatus.TODO));
            } else if (a.getStringKey().equals("--high")) {
                cmd.skip();
                runLater.add(t -> t.setPriority(NPriority.HIGH));
            } else if (a.getStringKey().equals("--critical")) {
                cmd.skip();
                runLater.add(t -> t.setPriority(NPriority.CRITICAL));
            } else if (a.getStringKey().equals("--normal")) {
                cmd.skip();
                runLater.add(t -> t.setPriority(NPriority.NORMAL));
            } else if (a.getStringKey().equals("--prio++") || a.getStringKey().equals("-p++")) {
                cmd.skip();
                runLater.add(t -> t.setPriority((t.getPriority() == null ? NPriority.NORMAL : t.getPriority()).higher()));
            } else if (a.getStringKey().equals("--prio--") || a.getStringKey().equals("-p--")) {
                cmd.skip();
                runLater.add(t -> t.setPriority((t.getPriority() == null ? NPriority.NORMAL : t.getPriority()).lower()));
            } else if (a.getStringKey().equals("--status")) {
                NTaskStatus v = NTaskStatus.valueOf(cmd.nextString().getStringValue().toLowerCase());
                runLater.add(t -> t.setStatus(v));
            } else if (a.getStringKey().equals("--duration")) {
                TimePeriod v = TimePeriod.parse(cmd.nextString().getStringValue(), false);
                runLater.add(t -> t.setDuration(v));
            } else if (a.getStringKey().equals("--name")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> t.setName(v));
            } else if (a.getStringKey().equals("--flag")) {
                String v = cmd.nextString().getStringValue();
                NFlag f = ("random".equalsIgnoreCase(v)) ?
                        NFlag.values()[(int) (Math.random() * NFlag.values().length)]
                        : NFlag.valueOf(v.toUpperCase());
                runLater.add(t -> t.setFlag(f));
            } else if (a.getStringKey().equals("--job")) {
                String jobId = cmd.nextString().getStringValue();
                NJob job = service.getJob(jobId);
                if (job == null) {
                    cmd.throwError("invalid job " + jobId);
                }
                runLater.add(t -> t.setJobId(job.getId()));
            } else if (a.getStringKey().equals("--parent")) {
                String taskId = cmd.nextString().getStringValue();
                NTask parentTask = service.getTask(taskId);
                if (parentTask == null) {
                    cmd.throwError("invalid parent task " + taskId);
                }
                runLater.add(t -> t.setParentTaskId(parentTask.getId()));
            } else if (a.getStringKey().equals("--priority")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> {
                    NPriority p = t.getPriority();
                    if (v.equalsIgnoreCase("higher")) {
                        p = p.higher();
                    } else if (v.equalsIgnoreCase("lower")) {
                        p = p.lower();
                    } else {
                        p = NPriority.valueOf(v.toLowerCase());
                    }
                    t.setPriority(p);
                });
            } else if (a.getStringKey().equals("--for") || a.getStringKey().equals("--project")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> t.setProject(v));
            } else if (a.getStringKey().equals("--obs")) {
                runLater.add(t -> t.setObservations(cmd.nextString().getStringValue()));
            } else if (a.isNonOption()) {
                NTask t = service.getTask(cmd.next().toString());
                if (t == null) {
                    cmd.throwError("task not found: " + a.toString());
                }
                tasks.add(t);
            } else {
                cmd.unexpectedArgument();
            }
        }
        if (tasks.isEmpty()) {
            cmd.throwError("task id expected");
        }
        for (NTask task : tasks) {
            for (Consumer<NTask> c : runLater) {
                c.accept(task);
            }
        }
        for (NTask task : new LinkedHashSet<>(tasks)) {
            service.updateTask(task);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("task {{%s}} updated.\n",
                        task.getId()
                );
            }
        }
        if (show) {
            for (NTask t : new LinkedHashSet<>(tasks)) {
                runTaskList(context.getWorkspace().commandLine().create(t.getId()));
            }
        }
        if (list) {
            runTaskList(context.getWorkspace().commandLine().create());
        }
    }

    public boolean runCommands(NutsCommandLine cmd) {
        if (runProjectCommands(cmd)) {
            return true;
        }
        if (runJobCommands(cmd)) {
            return true;
        }
        if (runTaskCommands(cmd)) {
            return true;
        }
        return false;
    }

    public boolean runJobCommands(NutsCommandLine cmd) {
        if (cmd.next("aj", "ja", "a j", "j a", "add job", "jobs add") != null) {
            runJobAdd(cmd);
            return true;
        } else if (cmd.next("lj", "jl", "l j", "j l", "list jobs", "jobs list") != null) {
            runJobList(cmd);
            return true;
        } else if (cmd.next("jrm", "rmj", "j rm", "rm j", "remove job", "remove jobs", "jobs remove") != null) {
            runJobRemove(cmd);
            return true;
        } else if (cmd.next("js", "sj", "j s", "s j", "show job", "show jobs", "jobs show") != null) {
            runJobShow(cmd);
            return true;
        } else if (cmd.next("j", "jobs") != null) {
            runJobList(cmd);
            return true;
        } else {
            return false;
        }
    }

    public boolean runProjectCommands(NutsCommandLine cmd) {
        if (cmd.next("ap", "a p", "pa", "p a", "add project", "projects add") != null) {
            runProjectAdd(cmd);
            return true;
        } else if (cmd.next("pu", "up", "p u", "u p", "update project", "projects update") != null) {
            runProjectUpdate(cmd);
            return true;
        } else if (cmd.next("lp", "pl", "l p", "p l", "list projects", "projects list") != null) {
            runProjectList(cmd);
            return true;
        } else if (cmd.next("rp", "rmp", "pr", "prm", "r p", "rm p", "p r", "p rm", "remove project", "remove projects", "rm project", "rm projects", "projects remove") != null
        ) {
            runProjectRemove(cmd);
            return true;
        } else if (cmd.next("ps", "sp", "s p", "p s", "show project", "show projects", "projects show") != null) {
            runProjectShow(cmd);
            return true;
        } else if (cmd.next("p", "projects") != null) {
            runProjectList(cmd);
            return true;
        }
        return false;
    }

    public boolean runTaskCommands(NutsCommandLine cmd) {
        if (cmd.next("a t", "t a", "ta", "at", "add task", "tasks add") != null) {
            runTaskAdd(cmd);
            return true;
        } else if (cmd.next("t u", "u t", "tu", "ut", "update task", "tasks update") != null) {
            runTaskUpdate(cmd);
            return true;
        } else if (cmd.next("l t", "t l", "lt", "tl", "list tasks", "tasks list") != null) {
            runTaskList(cmd);
            return true;
        } else if (cmd.next("tr", "rt", "trm", "rmt", "t r", "r t", "t rm", "rm t", "remove task", "remove tasks", "rm task", "rm tasks"
                , "tasks remove", "tasks rm") != null
        ) {
            runTaskRemove(cmd);
            return true;
        } else if (cmd.next("st", "ts", "s t", "t s", "show task", "show tasks", "tasks show") != null) {
            runTaskShow(cmd);
            return true;
        } else if (cmd.next("t", "tasks") != null) {
            runTaskList(cmd);
            return true;
        }
        return false;
    }

    private void runJobRemove(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (service.removeJob(a.toString())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("job {{%s}} removed.\n",
                            a.toString()
                    );
                }
            }
        }

    }

    private void runTaskRemove(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (service.removeTask(a.toString())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("task {{%s}} removed.\n",
                            a.toString()
                    );
                }
            }
        }

    }

    private void runProjectRemove(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (service.removeProject(a.toString())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("project {{%s}} removed.\n",
                            a.toString()
                    );
                }
            }
        }

    }

    private void runJobShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NJob job = service.getJob(a.toString());
            if (job == null) {
                context.getSession().out().printf("<<%s>>: @@not found@@.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("<<%s>>:\n",
                        a.toString()
                );
                context.getSession().out().printf("\t==job name==      : %s:\n", job.getName() == null ? "" : job.getName().toString());
                context.getSession().out().printf("\t==project==       : %s:\n", job.getProject() == null ? "" : job.getProject().toString());
                context.getSession().out().printf("\t==duration==      : %s:\n", job.getDuration() == null ? "" : job.getDuration().toString());
                context.getSession().out().printf("\t==start time==    : %s:\n", job.getStartTime() == null ? "" : LocalDateTime.ofInstant(job.getStartTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==duration extra==: %s:\n", job.getInternalDuration() == null ? "" : job.getInternalDuration().toString());
                context.getSession().out().printf("\t==observations==  : %s:\n", job.getObservations() == null ? "" : job.getObservations().toString());
            }
        }

    }

    private void runProjectShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NProject project = service.getProject(a.toString());
            if (project == null) {
                context.getSession().out().printf("<<%s>>: @@not found@@.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("<<%s>>:\n",
                        a.toString()
                );
                context.getSession().out().printf("\t==project name==  : %s:\n", project.getName() == null ? "" : project.getName().toString());
                context.getSession().out().printf("\t==beneficiary==   : %s:\n", project.getBeneficiary() == null ? "" : project.getBeneficiary().toString());
                context.getSession().out().printf("\t==company==       : %s:\n", project.getCompany() == null ? "" : project.getCompany().toString());
                context.getSession().out().printf("\t==start time==    : %s:\n", project.getStartTime() == null ? "" : LocalDateTime.ofInstant(project.getStartTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==start week day==: %s:\n", project.getStartWeekDay() == null ? "" : project.getStartWeekDay().toString());
                context.getSession().out().printf("\t==observations==  : %s:\n", project.getObservations() == null ? "" : project.getObservations().toString());
            }
        }

    }

    private void runTaskShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NTask task = service.getTask(a.toString());
            if (task == null) {
                context.getSession().out().printf("<<%s>>: @@not found@@.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("<<%s>>:\n",
                        a.toString()
                );
                context.getSession().out().printf("\t==task name==     : %s\n", task.getName() == null ? "" : task.getName().toString());
                context.getSession().out().printf("\t==status==        : %s\n", task.getStatus() == null ? "" : task.getStatus().toString());
                context.getSession().out().printf("\t==priority==      : %s\n", task.getPriority() == null ? "" : task.getPriority().toString());
                context.getSession().out().printf("\t==project==       : %s\n", task.getProject() == null ? "" : task.getProject().toString());
                context.getSession().out().printf("\t==flag==          : %s\n", task.getFlag() == null ? "" : task.getFlag().toString());
                context.getSession().out().printf("\t==parent id==     : %s\n", task.getParentTaskId() == null ? "" : task.getParentTaskId().toString());
                context.getSession().out().printf("\t==job id==        : %s\n", task.getJobId() == null ? "" : task.getJobId().toString());
                context.getSession().out().printf("\t==due time==      : %s\n", task.getDueTime() == null ? "" : LocalDateTime.ofInstant(task.getDueTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==start time==    : %s\n", task.getStartTime() == null ? "" : LocalDateTime.ofInstant(task.getStartTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==end time==      : %s\n", task.getEndTime() == null ? "" : LocalDateTime.ofInstant(task.getEndTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==duration==      : %s\n", task.getDuration() == null ? "" : task.getDuration().toString());
                context.getSession().out().printf("\t==duration extra==: %s\n", task.getInternalDuration() == null ? "" : task.getInternalDuration().toString());
                context.getSession().out().printf("\t==creation time== : %s\n", task.getCreationTime() == null ? "" : LocalDateTime.ofInstant(task.getCreationTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==modif. time==   : %s\n", task.getModificationTime() == null ? "" : LocalDateTime.ofInstant(task.getModificationTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==observations==  : %s\n", task.getObservations() == null ? "" : task.getObservations());
            }
        }

    }

    private void runJobList(NutsCommandLine cmd) {
        TimespanPattern hoursPerDay = TimespanPattern.WORK;
        int count = 100;
        NJobGroup groupBy = null;
        ChronoUnit countType = null;
        TimeUnit timeUnit = null;
        Predicate<NJob> whereFilter = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-w": {
                    countType = ChronoUnit.WEEKS;
                    count = cmd.nextString().getArgumentValue().getInt();
                    break;
                }
                case "-m": {
                    countType = ChronoUnit.MONTHS;
                    count = cmd.nextString().getArgumentValue().getInt();
                    break;
                }
                case "-l": {
                    countType = null;
                    count = cmd.nextString().getArgumentValue().getInt();
                    break;
                }
                case "-u":
                case "--unit": {
                    timeUnit = TimePeriod.parseUnit(cmd.nextString().getStringValue(), false);
                    break;
                }
                case "-g":
                case "--group":
                case "--groupBy":
                case "--groupby":
                case "--group-by": {
                    NutsArgument y = cmd.nextString();
                    switch (y.getStringValue()) {
                        case "p":
                        case "project": {
                            groupBy = NJobGroup.PROJECT_NAME;
                            break;
                        }
                        case "n":
                        case "name": {
                            groupBy = NJobGroup.JOB_NAME;
                            break;
                        }
                        case "s":
                        case "summary": {
                            groupBy = NJobGroup.SUMMARY;
                            break;
                        }
                        default: {
                            cmd.pushBack(y).unexpectedArgument("unvalid value");
                        }
                    }
                    break;
                }
                case "-p": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NJob> t = x -> sp.test(x.getProject());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-n": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NJob> t = x -> sp.test(x.getName());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-b": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NJob> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getBeneficiary());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-c": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NJob> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getCompany());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-d": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<TimePeriod> p = TimePeriod.parseFilter(s, false);
                    Predicate<NJob> t = x -> p.test(x.getDuration());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-t": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<Instant> t = new TimeParser().parseInstantFilter(s, false);
                    whereFilter = appendPredicate(whereFilter, x -> t.test(x.getStartTime()));
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        Stream<NJob> r = service.findLastJobs(null, count, countType, whereFilter, groupBy, timeUnit, hoursPerDay);
        TimeUnit timeUnit0 = timeUnit;
        if (groupBy != null) {
            if (context.getSession().isPlainTrace()) {
                r.forEach(x -> {
                    context.getSession().out().printf("starting ##%s## [[%s]] during ##%s## [==%s==] %s\n",
                            x.getStartTime(),
                            x.getStartTime().atZone(ZoneId.systemDefault()).getDayOfWeek().toString().toLowerCase().substring(0, 3),
                            timeUnit0 == null ? x.getDuration() : x.getDuration().toUnit(timeUnit0, hoursPerDay),
                            x.getProject() == null ? "*" : x.getProject(),
                            x.getName()
                    );
                });
            } else {
                context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
            }
        } else {
            if (context.getSession().isPlainTrace()) {
                r.forEach(x -> {
                    context.getSession().out().printf("<<%s>> at ##%s## [[%s]] during ##%s## [==%s==] %s\n",
                            x.getId(),
                            x.getStartTime(),
                            x.getStartTime().atZone(ZoneId.systemDefault()).getDayOfWeek().toString().toLowerCase().substring(0, 3),
                            timeUnit0 == null ? x.getDuration() : x.getDuration().toUnit(timeUnit0, hoursPerDay),
                            x.getProject() == null ? "*" : x.getProject(),
                            x.getName()
                    );
                });
            } else {
                context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
            }
        }
    }

    private void runTaskList(NutsCommandLine cmd) {
        TimespanPattern hoursPerDay = TimespanPattern.WORK;
        int count = 100;
        NJobGroup groupBy = null;
        ChronoUnit countType = null;
        TimeUnit timeUnit = null;
        Predicate<NTask> whereFilter = null;
        NTaskStatusFilter status = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-w": {
                    countType = ChronoUnit.WEEKS;
                    count = cmd.nextString().getArgumentValue().getInt();
                    break;
                }
                case "-m": {
                    countType = ChronoUnit.MONTHS;
                    count = cmd.nextString().getArgumentValue().getInt();
                    break;
                }
                case "-l": {
                    countType = null;
                    count = cmd.nextString().getArgumentValue().getInt();
                    break;
                }
                case "-u":
                case "--unit": {
                    timeUnit = TimePeriod.parseUnit(cmd.nextString().getStringValue(), false);
                    break;
                }
                case "--todo": {
                    cmd.nextString();
                    status = NTaskStatusFilter.TODO;
                    break;
                }
                case "-a":
                case "--all":
                    {
                    cmd.nextString();
                    status = NTaskStatusFilter.ALL;
                    break;
                }
                case "--cancelled": {
                    cmd.nextString();
                    status = NTaskStatusFilter.CANCELLED;
                    break;
                }
                case "--closed": {
                    cmd.nextString();
                    status = NTaskStatusFilter.CLOSED;
                    break;
                }
                case "--wip": {
                    cmd.nextString();
                    status = NTaskStatusFilter.WIP;
                    break;
                }
                case "--open": {
                    cmd.nextString();
                    status = NTaskStatusFilter.OPEN;
                    break;
                }
                case "-g":
                case "--group":
                case "--groupBy":
                case "--groupby":
                case "--group-by": {
                    NutsArgument y = cmd.nextString();
                    switch (y.getStringValue()) {
                        case "p":
                        case "project": {
                            groupBy = NJobGroup.PROJECT_NAME;
                            break;
                        }
                        case "n":
                        case "name": {
                            groupBy = NJobGroup.JOB_NAME;
                            break;
                        }
                        case "s":
                        case "summary": {
                            groupBy = NJobGroup.SUMMARY;
                            break;
                        }
                        default: {
                            cmd.pushBack(y).unexpectedArgument("invalid value");
                        }
                    }
                    break;
                }
                case "-p": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> sp.test(x.getProject());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-n": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> sp.test(x.getName());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-b": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getBeneficiary());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-c": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getCompany());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-d": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<TimePeriod> p = TimePeriod.parseFilter(s, false);
                    Predicate<NTask> t = x -> p.test(x.getDuration());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-t": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<Instant> t = new TimeParser().parseInstantFilter(s, false);
                    whereFilter = appendPredicate(whereFilter, x -> t.test(x.getStartTime()));
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        Stream<NTask> r = service.findTasks(status, null, count, countType, whereFilter, groupBy, timeUnit, hoursPerDay);
        TimeUnit timeUnit0 = timeUnit;
        if (groupBy != null) {
            if (context.getSession().isPlainTrace()) {
                r.forEach(x -> {
                    printTaskRow(x);
                });
            } else {
                context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
            }
        } else {
            if (context.getSession().isPlainTrace()) {
                r.forEach(x -> {
                    printTaskRow(x);
                });
            } else {
                context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
            }
        }
    }

    private void printTaskRow(NTask x) {
        context.getSession().out().printf("<<%s>> %s %s %s due ##%s## [==%s==] %s\n",
                x.getId(),
                getFlagString(x.getFlag()),
                getStatusString(x.getStatus()),
                getPriorityString(x.getPriority()),
                formatDate(x.getDueTime()),
                x.getProject() == null ? "*" : x.getProject(),
                x.getName()
        );
    }

    private String formatDate(Instant x) {
        if (x == null) {
            return "?";
        }
        return new TimeFormatter().format(x.atZone(ZoneId.systemDefault()).toLocalDateTime());
//        String s = x.atZone(ZoneId.systemDefault()).toString() + " " +
//                x.atZone(ZoneId.systemDefault()).getDayOfWeek().toString().toLowerCase().substring(0, 3);
//        return s;
    }

    private NutsString getCheckedString(Boolean x) {
        if (x == null) {
            return new NutsString("");
        }
        if (x) {
            return new NutsString("\u2611");
        } else {
            return new NutsString("\u25A1");
        }
    }

    private NutsString getPriorityString(NPriority x) {
        if (x == null) {
            return new NutsString("N");
        }
        switch (x) {
            case NONE:
                return new NutsString("<<0>>");
            case LOW:
                return new NutsString("<<L>>");
            case NORMAL:
                return new NutsString("N");
            case MEDIUM:
                return new NutsString("##M##");
            case URGENT:
                return new NutsString("{{U}}");
            case HIGH:
                return new NutsString("**H**");
            case CRITICAL:
                return new NutsString("@@C@@");
        }
        return new NutsString("?");
    }

    private NutsString getStatusString(NTaskStatus x) {
        if (x == null) {
            return new NutsString("*");
        }
        switch (x) {
            case TODO:
//                return new NutsString("\u2B58");
                return new NutsString("\u24c9");
//            case TODO:return new NutsString("\u25A1");
            case DONE:
                return new NutsString("((\u2611))");
//            case WIP:return new NutsString("##\u25B6##");
            case WIP:
                return new NutsString("##\u24CC##");
//                return new NutsString("##\u25F6##");
            case CANCELLED:
//                return new NutsString("@@\u2613@@");
//                return new NutsString("@@\u2421@@");
//                return new NutsString("@@\u26C3@@");
                return new NutsString("@@\u2718@@");
        }
        return new NutsString("?");
    }

    private NutsString getFlagString(NFlag x) {
        if (x == null) {
            x = NFlag.NONE;
        }
        switch (x) {
            case NONE:
                return new NutsString("\u2690");
            case STAR1:
                return new NutsString("==\u2605==");
            case STAR2:
                return new NutsString("{{\u2605}}");
            case STAR3:
                return new NutsString("##\u2605##");
            case STAR4:
                return new NutsString("[[\u2605]]");
            case STAR5:
                return new NutsString("**\u2605**");

            case FLAG1:
                return new NutsString("==\u2691==");
            case FLAG2:
                return new NutsString("{{\u2691}}");
            case FLAG3:
                return new NutsString("##\u2691##");
            case FLAG4:
                return new NutsString("[[\u2691]]");
            case FLAG5:
                return new NutsString("**\u2691**");

            case KING1:
                return new NutsString("==\u265A==");
            case KING2:
                return new NutsString("{{\u265A}}");
            case KING3:
                return new NutsString("##\u265A##");
            case KING4:
                return new NutsString("[[\u265A]]");
            case KING5:
                return new NutsString("**\u265A**");

            case HEART1:
                return new NutsString("==\u2665==");
            case HEART2:
                return new NutsString("{{\u2665}}");
            case HEART3:
                return new NutsString("##\u2665##");
            case HEART4:
                return new NutsString("[[\u2665]]");
            case HEART5:
                return new NutsString("**\u2665**");
            case PHONE1:
                return new NutsString("==\u260E==");
            case PHONE2:
                return new NutsString("{{\u260E}}");
            case PHONE3:
                return new NutsString("##\u260E##");
            case PHONE4:
                return new NutsString("[[\u260E]]");
            case PHONE5:
                return new NutsString("**\u260E**");
        }
        return new NutsString("[" + x.toString().toLowerCase() + "]");
    }

    private void runProjectList(NutsCommandLine cmd) {
        Predicate<NProject> whereFilter = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-c": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getBeneficiary());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-C": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getCompany());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-n": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getName());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-t": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<Instant> t = new TimeParser().parseInstantFilter(s, false);
                    whereFilter = appendPredicate(whereFilter, x -> t.test(x.getStartTime()));
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        Stream<NProject> r =
                service.findProjects().filter(whereFilter == null ? x -> true : whereFilter)
                        .sorted(Comparator.comparing(NProject::getName));

        if (context.getSession().isPlainTrace()) {
            r.forEach(x -> {
                context.getSession().out().printf("##%s## [==%s==] [==%s==] %s\n",
                        x.getStartTime(),
                        x.getCompany(),
                        x.getBeneficiary(),
                        x.getName()
                );
            });
        } else {
            context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
        }
    }

    private <T> Predicate<T> appendPredicate(Predicate<T> whereFilter, Predicate<T> t) {
        if (whereFilter == null) {
            whereFilter = t;
        } else {
            whereFilter = whereFilter.and(t);
        }
        return whereFilter;
    }

    private Predicate<String> createStringFilter(String s) {
        if (s.length() > 0 && s.startsWith("/") && s.endsWith("/")) {
            Pattern pattern = Pattern.compile(s);
            return x -> pattern.matcher(x == null ? "" : x).matches();
        }
        if (s.length() > 0 && s.contains("*")) {
            Pattern pattern = Pattern.compile(JobService.wildcardToRegex(s));
            return x -> pattern.matcher(x == null ? "" : x).matches();
        }
        return x -> s.equals(x == null ? "" : x);
    }

    public String colorize(Object a) {
        return colorize(a, null);
    }

    public String colorize(Object a, Object hash) {

        int h = Math.abs(hash != null ? hash.hashCode() : a != null ? a.hashCode() : 0);
        String et = context.getWorkspace().io().term().getTerminalFormat().escapeText(String.valueOf(a));
        String[] AA = new String[]{
                "?",
                "@@?@@",
                "[[?]]",
                "((?))",
                "{{?}}",
                "<<?>>",
                "**?**",
                "##?##",
                "^^?^^",
                "__?__",
                "==?==",
                "@@@?@@@",
                "[[[?]]]",
                "(((?)))",
                "{{{?}}}",
                "<<<?>>>",
                "***?***",
                "###?###",
                "^^^?^^^",
                "___?___",
                "===?===",
                "@@@@?@@@@",
                "[[[[?]]]]",
                "((((?))))",
                "{{{{?}}}}",
                "<<<<?>>>>",
                "****?****",
                "####?####",
                "^^^^?^^^^",
                "____?____",
                "====?====",
        };
        return AA[Math.abs(h) % AA.length].replace("?", et);
    }
}
