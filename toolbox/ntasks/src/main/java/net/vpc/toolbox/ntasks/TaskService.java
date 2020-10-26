package net.vpc.toolbox.ntasks;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsIllegalArgumentException;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class TaskService {
    private NutsApplicationContext context;
    private NDal dal;

    public TaskService(NutsApplicationContext context) {
        this.context = context;
        this.dal = new NDal(context);
    }

    public static String wildcardToRegex(String pattern) {
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder("^");
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '$':
                case '{':
                case '}':
                case '+': {
                    sb.append('\\').append(c);
                    break;
                }
                case '?': {
                    sb.append(".");
                    break;
                }
                case '*': {
                    sb.append(".*");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        sb.append('$');
        return sb.toString();
    }

    public void addProject(NProject p) {
        String name = p.getName();
        if (name == null) {
            throw new IllegalArgumentException("Invalid project");
        }
        NProject p0 = getProject(name);
        if (p0 != null) {
            throw new NutsIllegalArgumentException(context.getWorkspace(), "project already exists: " + name);
        }
        saveProject(p);
    }

    public void saveProject(NProject p) {
        String name = p.getName();
        if (name == null) {
            throw new IllegalArgumentException("Invalid project");
        }
        if (p.getCustomer() == null) {
            p.setCustomer("unspecified");
        }
        if (p.getCompany() == null) {
            p.setCompany(p.getCustomer());
        }
        if (p.getStartTime() == null) {
            p.setStartTime(Instant.now());
        }
        if (p.getStartWeekDay() == null) {
            p.setStartWeekDay(NDay.MONDAY);
        }
        dal.store(p);
    }

    public void saveTask(NTask task) {
        if (task.getName() == null) {
            task.setName("work");
        }

        if (task.getStartTime() == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.HOUR, 8);
            task.setStartTime(c.getTime().toInstant());
        }
        if (task.getDuration() == null) {
            task.setDuration(new NTimePeriod(1, TimeUnit.HOURS));
        }
        if (task.getProject() == null) {
            task.setProject("misc");
        }

        String project = task.getProject();
        if (project != null) {
            NProject p = dal.load(NProject.class, project);
            if (p == null) {
                p = new NProject();
                p.setName(project);
                saveProject(p);
            }
        }
        if (task.getId() == null) {
            task.setId(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
            dal.store(task);
        } else {
            NProject r = dal.load(NProject.class, task.getId());
            if (r == null) {
                throw new IllegalArgumentException();
            }
            dal.store(task);
        }
    }

    public Instant getStartWeek(Instant date, NDay startWeekDay) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date.toEpochMilli());
        int d = c.get(Calendar.DAY_OF_WEEK);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int d0 = startWeekDay.ordinal() - NDay.SUNDAY.ordinal() + 1;
        if (d != d0) {
            c.add(Calendar.DAY_OF_YEAR, d0 - d);
        }
        return Instant.ofEpochMilli(c.getTimeInMillis());
    }

    public Instant getStartMonth(Instant date) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date.toEpochMilli());
        int d = c.get(Calendar.DAY_OF_WEEK);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DAY_OF_MONTH, 1);
        return Instant.ofEpochMilli(c.getTimeInMillis());
    }

    public Instant subWeek(int weekIndex) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Instant.now().toEpochMilli());
        int d = c.get(Calendar.DAY_OF_WEEK);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DAY_OF_WEEK, -weekIndex);
        return Instant.ofEpochMilli(c.getTimeInMillis());
    }

    public Stream<NTask> findWeekTasks(Instant date) {
        return dal.search(NTask.class).filter(x -> {
            NDay d = NDay.MONDAY;
            if (x.getProject() != null) {
                NDay d0 = dal.load(NProject.class, x.getProject()).getStartWeekDay();
                if (d0 != null) {
                    d = d0;
                }
            }
            return getStartWeek(date, d).equals(getStartWeek(x.getStartTime(), d));
        });
    }

    public Stream<NTask> findLastTasks(Instant endTime, int lastCount, ChronoUnit lastUnit) {
        Instant endTime0 = endTime == null ? Instant.now() : endTime;
        Stream<NTask> s = dal.search(NTask.class).filter(x -> {
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
        return s;
    }

    public Stream<NTask> findMonthTasks(Instant date) {
        return dal.search(NTask.class).filter(x -> {
            return getStartMonth(date).equals(getStartMonth(x.getStartTime()));
        });
    }

    public Stream<NTask> tailWeekTasks(int count) {
        Instant w0 = getStartWeek(subWeek(count), NDay.SUNDAY);
        Instant w1 = getStartWeek(subWeek(count - 1), NDay.SUNDAY);
        return dal.search(NTask.class).filter(x -> {
            return x.getStartTime().compareTo(w0) >= 0 && x.getStartTime().compareTo(w1) < 0;
        });
    }

    public NTask getTask(String taskId) {
        return dal.load(NTask.class, taskId);
    }

    public NProject getProject(String taskId) {
        return dal.load(NProject.class, taskId);
    }

    public boolean removeTask(String taskId) {
        return dal.delete(NTask.class, taskId);
    }

    public boolean removeProject(String projectName) {
        if (dal.search(NTask.class).anyMatch(x -> projectName.equals(x.getProject()))) {
            throw new IllegalArgumentException("Project is used in on or multiple tasks. It cannot e removed.");
        }
        return dal.delete(NProject.class, projectName);
    }

    public Stream<NProject> findProjects() {
        return dal.search(NProject.class);
    }
}
