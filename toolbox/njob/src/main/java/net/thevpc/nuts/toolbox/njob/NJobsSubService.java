package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.toolbox.njob.model.NJob;
import net.thevpc.nuts.toolbox.njob.model.NJobGroup;
import net.thevpc.nuts.toolbox.njob.model.NProject;
import net.thevpc.nuts.toolbox.njob.time.TimePeriod;
import net.thevpc.nuts.toolbox.njob.time.TimePeriods;
import net.thevpc.nuts.toolbox.njob.time.TimespanPattern;
import net.thevpc.nuts.toolbox.njob.time.WeekDay;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NJobsSubService {
    private NutsApplicationContext context;
    private NJobConfigStore dal;
    private JobService service;

    public NJobsSubService(NutsApplicationContext context, NJobConfigStore dal, JobService service) {
        this.context = context;
        this.dal = dal;
        this.service = service;
    }


    public void addJob(NJob job) {
        if (job.getName() == null) {
            job.setName("work");
        }
        job.setCreationTime(Instant.now());
        job.setModificationTime(job.getCreationTime());
        if (job.getStartTime() == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.HOUR, 8);
            job.setStartTime(c.getTime().toInstant());
        }
        if (job.getDuration() == null) {
            job.setDuration(new TimePeriod(1, ChronoUnit.HOURS));
        }
        if (job.getProject() == null) {
            job.setProject("misc");
        }

        String project = job.getProject();
        if (project != null) {
            NProject p = service.projects().getProject(project);
            if (p == null) {
                if (service.isIdFormat(project)) {
                    throw new NoSuchElementException("Project not found: " + project);
                }
                p = new NProject();
                p.setName(project);
                service.projects().addProject(p);
            }
        }
        job.setId(null);
//        if (job.getId() != null) {
//            NJob r = dal.load(NJob.class, job.getId());
//            if (r == null) {
//                throw new IllegalArgumentException();
//            }
//        }
        dal.store(job);
    }


    public Stream<NJob> findWeekJobs(Instant date) {
        return findAllJobs().filter(x -> {
            WeekDay d = WeekDay.MONDAY;
            if (x.getProject() != null) {
                WeekDay d0 = service.projects().getProject(x.getProject()).getStartWeekDay();
                if (d0 != null) {
                    d = d0;
                }
            }
            return service.getStartWeek(date, d).equals(service.getStartWeek(x.getStartTime(), d));
        });
    }

    public Stream<NJob> findLastJobs(Instant endTime, int lastCount, ChronoUnit lastUnit, Predicate<NJob> whereFilter, NJobGroup groupBy, ChronoUnit groupTimeUnit, TimespanPattern groupPattern) {
        Instant endTime0 = endTime == null ? Instant.now() : endTime;
        Stream<NJob> s = findAllJobs().filter(x -> {
            if (!(endTime == null || x.getStartTime().compareTo(endTime) <= 0)) {
                return false;
            }
            if (lastCount >= 0 && lastUnit != null) {
                LocalDate endDate = endTime0.atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate startDate = x.getStartTime().atZone(ZoneId.systemDefault()).toLocalDate();
                return lastCount <= 0 || lastUnit.between(startDate, endDate) <= lastCount;
            }
            return true;
        }).sorted((o1, o2) -> -o1.getStartTime().compareTo(o2.getStartTime()));
        if (whereFilter != null) {
            s = s.filter(whereFilter);
        }
        if (lastUnit == null && lastCount > 0) {
            s = s.limit(lastCount);
        }
        if (groupBy != null) {
            s = s.collect(Collectors.groupingBy(
                    (NJobGroup.PROJECT_NAME.equals(groupBy)) ? x -> x.getProject() :
                            (NJobGroup.NAME.equals(groupBy)) ? x -> x.getProject() + ":" + x.getName() :
                                    (NJobGroup.SUMMARY.equals(groupBy)) ? x -> "summary" :
                                            x -> x.getId()

            ))
                    .entrySet().stream().map(x -> groupJobs(x.getValue(), groupTimeUnit, groupPattern));
        }
        return s;
    }


    public void updateJob(NJob job) {
        if (job.getName() == null) {
            job.setName("work");
        }
        Instant now = Instant.now();
        if (job.getCreationTime() == null) {
            job.setCreationTime(now);
        }
        job.setModificationTime(now);
        if (job.getObservations() == null) {
            job.setObservations("");
        }
        if (job.getStartTime() == null) {

        }
        if (job.getStartTime() == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.HOUR, 8);
            job.setStartTime(c.getTime().toInstant());
        }
        if (job.getDuration() == null) {
            job.setDuration(new TimePeriod(1, ChronoUnit.HOURS));
        }
        if (job.getProject() == null) {
            job.setProject("misc");
        }

        String project = job.getProject();
        if (project != null) {
            NProject p = service.projects().getProject(project);
            if (p == null) {
                if (service.isIdFormat(project)) {
                    throw new NoSuchElementException("Project not found: " + project);
                }
                p = new NProject();
                p.setName(project);
                service.projects().addProject(p);
            }
        }
        dal.store(job);
    }


    public Stream<NJob> tailWeekJobs(int count) {
        Instant w0 = service.getStartWeek(service.subWeek(count), WeekDay.SUNDAY);
        Instant w1 = service.getStartWeek(service.subWeek(count - 1), WeekDay.SUNDAY);
        return findAllJobs().filter(x -> {
            return x.getStartTime().compareTo(w0) >= 0 && x.getStartTime().compareTo(w1) < 0;
        });
    }

    public NJob getJob(String jobId) {
        return dal.load(NJob.class, jobId);
    }

    public Stream<NJob> findMonthJobs(Instant date) {
        if (date == null) {
            date = Instant.now();
        }
        Instant finalDate = date;
        return findAllJobs().filter(x -> {
            return service.getStartMonth(finalDate).equals(service.getStartMonth(x.getStartTime()));
        });
    }

    public NJob groupJobs(Collection<NJob> value, ChronoUnit timeUnit, TimespanPattern hoursPerDay) {
        return groupJobs(value.toArray(new NJob[0]), timeUnit, hoursPerDay);
    }

    public NJob groupJobs(NJob[] value, ChronoUnit timeUnit, TimespanPattern hoursPerDay) {
        NJob t = new NJob();
        TimePeriods tp = new TimePeriods();
        TreeSet<ChronoUnit> atu = new TreeSet<ChronoUnit>();
        TreeSet<String> names = new TreeSet<String>();
        TreeSet<String> projects = new TreeSet<String>();
        for (NJob nJob : value) {
            tp.add(nJob.getDuration());
            t.setStartTime(nJob.getStartTime());
            atu.add(nJob.getDuration().getUnit());
            names.add(nJob.getName());
            projects.add(nJob.getProject());
        }
        t.setProject(projects.size() == 0 ? "" : projects.size() == 1 ? projects.toArray()[0].toString() :
                (projects.size() <= 3 || String.join(",", projects).length() < 20) ? String.join(",", projects) :
                        (String.valueOf(projects.size()) + " projects")
        );
        ChronoUnit[] atu0 = atu.toArray(new ChronoUnit[0]);
        String jobs = " Job" + ((value.length == 1) ? "" : "s");
        String named = (names.size() == 0) ? "" : (names.size() == 1) ? (" named " + names.toArray()[0]) : (" with " + (names.size()) + " different names");
        t.setName(value.length + jobs + named);
        t.setDuration(tp.toUnit(timeUnit != null ? timeUnit : (atu0.length == 0 ? ChronoUnit.DAYS : atu0[0]), hoursPerDay));
        t.setId(UUID.randomUUID().toString());
        return t;
    }


    public boolean removeJob(String jobId) {
        long count = service.tasks().findAllTasks().filter(x -> jobId.equals(x.getJobId())).count();
        if (count > 1) {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("job is used in %d tasks. It cannot be removed.",count));
        } else if (count > 0) {
            throw new NutsIllegalArgumentException(context.getSession(),NutsMessage.cstyle("job is used in one task. It cannot be removed."));
        }
        return dal.delete(NJob.class, jobId);
    }



    public Stream<NJob> findAllJobs() {
        return dal.search(NJob.class);
    }

}
