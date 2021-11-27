package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.njob.model.NProject;
import net.thevpc.nuts.toolbox.njob.time.TimeParser;
import net.thevpc.nuts.toolbox.njob.time.WeekDay;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NProjectsSubCmd {

    private JobService service;
    private NutsApplicationContext context;
    private NutsSession session;
    private JobServiceCmd parent;

    public NProjectsSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.context = parent.context;
        this.service = parent.service;
        this.session = parent.session;
    }

    public void runProjectAdd(NutsCommandLine cmd) {
        NProject t = new NProject();
        boolean list = false;
        boolean show = false;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            if (a.getKey().getString().equals("--list") || a.getKey().getString().equals("-l")) {
                list = cmd.nextBoolean().getValue().getBoolean();
            } else if (a.getKey().getString().equals("--show") || a.getKey().getString().equals("-s")) {
                show = cmd.nextBoolean().getValue().getBoolean();
            } else if (a.getKey().getString().equals("-t") || a.getKey().getString().equals("--start") || a.getKey().getString().equals("--on")) {
                t.setStartTime(new TimeParser().parseInstant(cmd.nextString().getValue().getString(), false));
            } else if (a.getKey().getString().equals("--at")) {
                t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getValue().getString(), false));
            } else if (a.getKey().getString().equals("-b") || a.getKey().getString().equals("--beneficiary") || a.getKey().getString().equals("--for")) {
                t.setBeneficiary(cmd.nextString().getValue().getString());
            } else if (a.getKey().getString().equals("-c") || a.getKey().getString().equals("--company") || a.getKey().getString().equals("--via")) {
                t.setCompany(cmd.nextString().getValue().getString());
            } else if (a.getKey().getString().equals("-1") || a.getKey().getString().equals("--day1")) {
                t.setStartWeekDay(WeekDay.parse(cmd.nextString().getValue().getString()));
            } else if (a.getKey().getString().equals("-o") || a.getKey().getString().equals("--obs")) {
                t.setObservations(cmd.nextString().getValue().getString());
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
        if (cmd.isExecMode()) {
            service.projects().addProject(t);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("project %s (%s) added.\n",
                        NutsTexts.of(context.getSession()).ofStyled(t.getId(), NutsTextStyle.primary5()),
                        t.getName()
                );
            }
            if (show) {
                runProjectShow(NutsCommandLine.of(new String[]{t.getId()}, session));
            }
            if (list) {
                runProjectList(NutsCommandLine.of(new String[0], session));
            }
        }
    }

    public void runProjectUpdate(NutsCommandLine cmd) {
        List<NProject> projects = new ArrayList<>();
        boolean list = false;
        boolean show = false;
        String mergeTo = null;
        List<Consumer<NProject>> runLater = new ArrayList<>();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getKey().getString()) {
                case "-l":
                case "--list": {
                    list = cmd.nextBoolean().getValue().getBoolean();
                    break;
                }
                case "-s":
                case "--show": {
                    show = cmd.nextBoolean().getValue().getBoolean();
                    break;
                }
                case "--on":
                case "--start": {
                    Instant v = new TimeParser().parseInstant(cmd.nextString().getValue().getString(), false);
                    runLater.add(t -> t.setStartTime(v));
                    break;
                }
                case "--at": {
                    Instant v = new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getValue().getString(), false);
                    runLater.add(t -> t.setStartTime(v));
                    break;
                }
                case "--for":
                case "--beneficiary":
                case "-b": {
                    String v = cmd.nextString().getValue().getString();
                    runLater.add(t -> t.setBeneficiary(v));
                    break;
                }
                case "--company":
                case "--via":
                case "-c": {
                    String v = cmd.nextString().getValue().getString();
                    runLater.add(t -> t.setCompany(v));
                    break;
                }
                case "--day1":
                case "-1": {
                    WeekDay v = WeekDay.parse(cmd.nextString().getValue().getString());
                    runLater.add(t -> t.setStartWeekDay(v));
                    break;
                }
                case "--obs":
                case "-o": {
                    String v = cmd.nextString().getValue().getString();
                    runLater.add(t -> t.setObservations(v));
                    break;
                }
                case "--merge-to": {
                    NutsArgument c = cmd.nextString();
                    if (!c.isInactive()) {
                        if (mergeTo != null) {
                            cmd.pushBack(c);
                            cmd.unexpectedArgument();
                        } else {
                            mergeTo = c.getValue().getString();
                        }
                    }
                    break;
                }
                case "++obs":
                case "+o": {
                    String v = cmd.nextString().getValue().getString();
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
                        String pid = cmd.next().toString();
                        NProject t = findProject(pid, cmd);
                        projects.add(t);
                    } else {
                        cmd.unexpectedArgument();
                    }
                }
            }
        }
        if (projects.isEmpty()) {
            cmd.throwError(NutsMessage.formatted("project name expected"));
        }
        if (cmd.isExecMode()) {
            NutsTexts text = NutsTexts.of(context.getSession());
            for (NProject project : projects) {
                for (Consumer<NProject> c : runLater) {
                    c.accept(project);
                }
                service.projects().updateProject(project);
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("project %s (%s) updated.\n",
                            text.ofStyled(project.getId(), NutsTextStyle.primary5()),
                            text.ofStyled(project.getName(), NutsTextStyle.primary1())
                    );
                }
            }
            if (mergeTo != null) {
                service.projects().mergeProjects(mergeTo, projects.stream().map(x -> x.getId()).toArray(String[]::new));
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("projects merged to %s.\n",
                            NutsTexts.of(context.getSession())
                            .ofStyled(mergeTo, NutsTextStyle.primary5())
                    );
                }
            }
            if (show) {
                for (NProject t : new LinkedHashSet<>(projects)) {
                    runProjectShow(NutsCommandLine.of(new String[]{t.getId()}, session));
                }
            }
            if (list) {
                runProjectList(NutsCommandLine.of(new String[0], session));
            }
        }
    }

    private void runProjectList(NutsCommandLine cmd) {
        Predicate<NProject> whereFilter = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getKey().getString()) {
                case "-b":
                case "-beneficiary": {
                    String s = cmd.nextString().getValue().getString();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getBeneficiary());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-c":
                case "-company": {
                    String s = cmd.nextString().getValue().getString();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getCompany());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-n":
                case "--name": {
                    String s = cmd.nextString().getValue().getString();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getName());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "--unused": {
                    boolean unused = cmd.nextBoolean().getValue().getBoolean();
                    Predicate<NProject> t = x -> service.projects().isUsedProject(x.getId()) != unused;
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time": {
                    String s = cmd.nextString().getValue().getString();
                    Predicate<Instant> t = new TimeParser().parseInstantFilter(s, false);
                    whereFilter = parent.appendPredicate(whereFilter, x -> t.test(x.getStartTime()));
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        if (cmd.isExecMode()) {

            Stream<NProject> r
                    = service.projects().findProjects().filter(whereFilter == null ? x -> true : whereFilter)
                            .sorted(
                                    (x, y) -> {
                                        Instant s1 = x.getStartTime();
                                        Instant s2 = y.getStartTime();
                                        int v = s2.compareTo(s1);
                                        if (v != 0) {
                                            return v;
                                        }
                                        return x.getName().compareTo(y.getName());
                                    }
                            );

            if (context.getSession().isPlainTrace()) {
                NutsMutableTableModel m = NutsMutableTableModel.of(session);
                List<NProject> lastResults = new ArrayList<>();
                int[] index = new int[1];
                r.forEach(x -> {
                    Instant st = x.getStartTime();
                    String sts = "";
                    if (st != null) {
                        LocalDateTime d = LocalDateTime.ofInstant(st, ZoneId.systemDefault());
                        sts = d.getYear() + " " + d.getMonth().toString().toLowerCase().substring(0, 3);
                    }
                    lastResults.add(x);
                    index[0]++;
                    m.newRow().addCells(
                            parent.createHashId(index[0], -1),
                            x.getId(),
                            sts,
                            x.getCompany(),
                            x.getBeneficiary(),
                            parent.getFormattedProject(x.getName() == null ? "*" : x.getName())
                    );
                });
                context.getSession().setProperty("LastResults", lastResults.toArray(new NProject[0]));
                NutsTableFormat.of(session)
                        .setBorder("spaces")
                        .setValue(m).println(context.getSession().out());
            } else {
                context.getSession().out().printf(r.collect(Collectors.toList()));
            }
        }
    }

    private void runProjectRemove(NutsCommandLine cmd) {
        NutsTexts text = NutsTexts.of(context.getSession());
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (cmd.isExecMode()) {
                NProject t = findProject(a.toString(), cmd);
                if (service.projects().removeProject(t.getId())) {
                    if (context.getSession().isPlainTrace()) {
                        context.getSession().out().printf("project %s removed.\n",
                                text.ofStyled(a.toString(), NutsTextStyle.primary5())
                        );
                    }
                } else {
                    context.getSession().out().printf("project %s %s.\n",
                            text.ofStyled(a.toString(), NutsTextStyle.primary5()),
                            text.ofStyled("not found", NutsTextStyle.error())
                    );
                }
            }
        }

    }

    private void runProjectShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NProject project = findProject(a.toString(), cmd);
            if (project == null) {
                context.getSession().out().printf("```kw %s```: ```error not found```.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("```kw %s```:\n",
                        project.getId()
                );
                String prefix = "\t                    ";
                context.getSession().out().printf("\t```kw2 project name```  : %s\n", parent.formatWithPrefix(project.getName(), prefix));
                context.getSession().out().printf("\t```kw2 beneficiary```   : %s\n", parent.formatWithPrefix(project.getBeneficiary(), prefix));
                context.getSession().out().printf("\t```kw2 company```       : %s\n", parent.formatWithPrefix(project.getCompany(), prefix));
                context.getSession().out().printf("\t```kw2 start time```    : %s\n", parent.formatWithPrefix(project.getStartTime(), prefix));
                context.getSession().out().printf("\t```kw2 start week day```: %s\n", parent.formatWithPrefix(project.getStartWeekDay(), prefix));
                context.getSession().out().printf("\t```kw2 observations```  : %s\n", parent.formatWithPrefix(project.getObservations(), prefix));
            }
        }

    }

    private NProject findProject(String pid, NutsCommandLine cmd) {
        NProject t = null;
        if (pid.startsWith("#")) {
            int x = parent.parseIntOrFF(pid.substring(1));
            if (x >= 1) {
                Object lastResults = context.getSession().getProperty("LastResults");
                if (lastResults instanceof NProject[] && x <= ((NProject[]) lastResults).length) {
                    t = ((NProject[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.projects().getProject(pid);
        }
        if (t == null) {
            cmd.throwError(NutsMessage.cstyle("project not found: %s", pid));
        }
        return t;
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
        } else if (cmd.next("rp", "rmp", "pr", "prm", "r p", "rm p", "p r", "p rm", "remove project", "remove projects", "rm project", "rm projects", "projects remove") != null) {
            runProjectRemove(cmd);
            return true;
        } else if (cmd.next("ps", "sp", "s p", "p s", "show project", "show projects", "projects show") != null) {
            runProjectShow(cmd);
            return true;
        } else if (cmd.next("p", "projects") != null) {
            if (cmd.next("--help") != null) {
                parent.showCustomHelp("njob-projects");
            } else {
                runProjectList(cmd);
            }
            return true;
        }
        return false;
    }

}
