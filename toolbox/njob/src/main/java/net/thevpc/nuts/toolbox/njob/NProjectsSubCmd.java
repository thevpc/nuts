package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.format.NutsMutableTableModel;
import net.thevpc.nuts.format.NutsTableFormat;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.toolbox.njob.model.NProject;
import net.thevpc.nuts.toolbox.njob.time.TimeParser;
import net.thevpc.nuts.toolbox.njob.time.WeekDay;
import net.thevpc.nuts.util.NutsRef;

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
        NutsRef<Boolean> list = NutsRef.of(false);
        NutsRef<Boolean> show = NutsRef.of(false);
        while (cmd.hasNext()) {
            NutsArgument aa = cmd.peek().get(session);
            switch (aa.key()) {
                case "--list":
                case "-l": {
                    cmd.withNextBoolean((v, a, s) -> list.set(v));
                    break;
                }
                case "--show":
                case "-s": {
                    cmd.withNextBoolean((v, a, s) -> show.set(v));
                    break;
                }
                case "-t":
                case "--start":
                case "--on": {
                    cmd.withNextString((v, a, s) -> t.setStartTime(new TimeParser().parseInstant(v, false)));
                    break;
                }
                case "--at": {
                    cmd.withNextString((v, a, s) -> t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(v, false)));
                    break;
                }
                case "-b":
                case "--beneficiary":
                case "--for": {
                    cmd.withNextString((v, a, s) -> t.setBeneficiary(v));
                    break;
                }
                case "-c":
                case "--company":
                case "--via": {
                    cmd.withNextString((v, a, s) -> t.setCompany(v));
                    break;
                }
                case "-1":
                case "--day1": {
                    cmd.withNextString((v, a, s) -> t.setStartWeekDay(WeekDay.parse(v)));
                    break;
                }
                case "-o":
                case "--obs": {
                    cmd.withNextString((v, a, s) -> t.setObservations(v));
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
            service.projects().addProject(t);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("project %s (%s) added.\n",
                        NutsTexts.of(context.getSession()).ofStyled(t.getId(), NutsTextStyle.primary5()),
                        t.getName()
                );
            }
            if (show.get()) {
                runProjectShow(NutsCommandLine.of(new String[]{t.getId()}));
            }
            if (list.get()) {
                runProjectList(NutsCommandLine.of(new String[0]));
            }
        }
    }

    public void runProjectUpdate(NutsCommandLine cmd) {
        class Data {
            List<NProject> projects = new ArrayList<>();
            boolean list = false;
            boolean show = false;
            String mergeTo = null;
            List<Consumer<NProject>> runLater = new ArrayList<>();
        }
        Data d = new Data();
        while (cmd.hasNext()) {
            NutsArgument aa = cmd.peek().get(session);
            switch (aa.key()) {
                case "-l":
                case "--list": {
                    cmd.withNextBoolean((v, a, s) -> d.list = v);
                    break;
                }
                case "-s":
                case "--show": {
                    cmd.withNextBoolean((v, a, s) -> d.show = v);
                    break;
                }
                case "--on":
                case "--start": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setStartTime(new TimeParser().parseInstant(v, false))));
                    break;
                }
                case "--at": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(v, false))));
                    break;
                }
                case "--for":
                case "--beneficiary":
                case "-b": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setBeneficiary(v)));
                    break;
                }
                case "--company":
                case "--via":
                case "-c": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setCompany(v)));
                    break;
                }
                case "--day1":
                case "-1": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setStartWeekDay(WeekDay.parse(v))));
                    break;
                }
                case "--obs":
                case "-o": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setObservations(v)));
                    break;
                }
                case "--merge-to": {
                    cmd.withNextString((v, a, s) -> {
                        if (d.mergeTo != null) {
                            cmd.pushBack(a);
                            cmd.throwUnexpectedArgument();
                        } else {
                            d.mergeTo = v;
                        }
                    });
                    break;
                }
                case "++obs":
                case "+o": {
                    cmd.withNextString((v, a, s) -> {
                        d.runLater.add(t -> {
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
                    if (aa.isNonOption()) {
                        String pid = cmd.next().get(session).toString();
                        NProject t = findProject(pid, cmd);
                        d.projects.add(t);
                    } else {
                        cmd.throwUnexpectedArgument();
                    }
                }
            }
        }
        if (d.projects.isEmpty()) {
            cmd.throwError(NutsMessage.ofNtf("project name expected"));
        }
        if (cmd.isExecMode()) {
            NutsTexts text = NutsTexts.of(context.getSession());
            for (NProject project : d.projects) {
                for (Consumer<NProject> c : d.runLater) {
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
            if (d.mergeTo != null) {
                service.projects().mergeProjects(d.mergeTo, d.projects.stream().map(x -> x.getId()).toArray(String[]::new));
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("projects merged to %s.\n",
                            NutsTexts.of(context.getSession())
                                    .ofStyled(d.mergeTo, NutsTextStyle.primary5())
                    );
                }
            }
            if (d.show) {
                for (NProject t : new LinkedHashSet<>(d.projects)) {
                    runProjectShow(NutsCommandLine.of(new String[]{t.getId()}));
                }
            }
            if (d.list) {
                runProjectList(NutsCommandLine.of(new String[0]));
            }
        }
    }

    private void runProjectList(NutsCommandLine cmd) {
        final NutsRef<Predicate<NProject>> whereFilter = NutsRef.ofNull();
        while (cmd.hasNext()) {
            NutsArgument aa = cmd.peek().get(session);
            switch (aa.key()) {
                case "-b":
                case "-beneficiary": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NProject> t = x -> sp.test(x.getBeneficiary());
                        parent.appendPredicateRef(whereFilter, t);
                    });
                    break;
                }
                case "-c":
                case "-company": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NProject> t = x -> sp.test(x.getCompany());
                        parent.appendPredicateRef(whereFilter, t);
                    });
                    break;
                }
                case "-n":
                case "--name": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NProject> t = x -> sp.test(x.getName());
                        parent.appendPredicateRef(whereFilter, t);
                    });
                    break;
                }
                case "--unused": {
                    cmd.withNextBoolean((v, a, s) -> {
                        Predicate<NProject> t = x -> service.projects().isUsedProject(x.getId()) != v;
                        parent.appendPredicateRef(whereFilter, t);
                    });
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<Instant> t = new TimeParser().parseInstantFilter(v, false);
                        parent.appendPredicateRef(whereFilter, x -> t.test(x.getStartTime()));
                    });
                    break;
                }
                default: {
                    cmd.throwUnexpectedArgument();
                }
            }
        }
        if (cmd.isExecMode()) {

            Stream<NProject> r
                    = service.projects().findProjects().filter(whereFilter.isNull() ? x -> true : whereFilter.get())
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
            NutsArgument a = cmd.next().get(session);
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
            NutsArgument a = cmd.next().get(session);
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
                context.getSession().out().printf("\t```kw2 project name```  : %s\n", JobServiceCmd.formatWithPrefix(project.getName(), prefix));
                context.getSession().out().printf("\t```kw2 beneficiary```   : %s\n", JobServiceCmd.formatWithPrefix(project.getBeneficiary(), prefix));
                context.getSession().out().printf("\t```kw2 company```       : %s\n", JobServiceCmd.formatWithPrefix(project.getCompany(), prefix));
                context.getSession().out().printf("\t```kw2 start time```    : %s\n", JobServiceCmd.formatWithPrefix(project.getStartTime(), prefix));
                context.getSession().out().printf("\t```kw2 start week day```: %s\n", JobServiceCmd.formatWithPrefix(project.getStartWeekDay(), prefix));
                context.getSession().out().printf("\t```kw2 observations```  : %s\n", JobServiceCmd.formatWithPrefix(project.getObservations(), prefix));
            }
        }

    }

    private NProject findProject(String pid, NutsCommandLine cmd) {
        NProject t = null;
        if (pid.startsWith("#")) {
            int x = JobServiceCmd.parseIntOrFF(pid.substring(1));
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
            cmd.throwError(NutsMessage.ofCstyle("project not found: %s", pid));
        }
        return t;
    }

    public boolean runProjectCommands(NutsCommandLine cmd) {
        if (cmd.next("ap", "a p", "pa", "p a", "add project", "projects add").isPresent()) {
            runProjectAdd(cmd);
            return true;
        } else if (cmd.next("pu", "up", "p u", "u p", "update project", "projects update").isPresent()) {
            runProjectUpdate(cmd);
            return true;
        } else if (cmd.next("lp", "pl", "l p", "p l", "list projects", "projects list").isPresent()) {
            runProjectList(cmd);
            return true;
        } else if (cmd.next("rp", "rmp", "pr", "prm", "r p", "rm p", "p r", "p rm", "remove project", "remove projects", "rm project", "rm projects", "projects remove").isPresent()) {
            runProjectRemove(cmd);
            return true;
        } else if (cmd.next("ps", "sp", "s p", "p s", "show project", "show projects", "projects show").isPresent()) {
            runProjectShow(cmd);
            return true;
        } else if (cmd.next("p", "projects").isPresent()) {
            if (cmd.next("--help").isPresent()) {
                parent.showCustomHelp("njob-projects");
            } else {
                runProjectList(cmd);
            }
            return true;
        }
        return false;
    }

}
