package net.vpc.app.nuts.toolbox.njob.time;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TimePeriods {
    private Map<TimeUnit, Double> values = new HashMap<>();

    public TimePeriods add(double v, TimeUnit t) {
        if(v!=0){
            Double d = values.get(t);
            if (d == null) {
                d = 0.0;
            }
            d += v;
            values.put(t, d);
        }
        return this;
    }

    public TimePeriods add(TimePeriod p) {
        return add(p.getCount(),p.getUnit());
    }

    public TimePeriod[] getPeriodArray() {
        TimePeriod[] timePeriods = values.entrySet().stream().map(x -> new TimePeriod(x.getValue(), x.getKey())).toArray(TimePeriod[]::new);
        Arrays.sort(timePeriods);
        return timePeriods;
    }

    public TimePeriod getPart(TimeUnit a) {
        Double r = values.get(a);
        return r == null ? new TimePeriod(0, a) : new TimePeriod(r, a);
    }

    public TimePeriod toUnit(TimeUnit a, TimespanPattern timespanPattern) {
        TimePeriods e = new TimePeriods();
        for (TimePeriod v : getPeriodArray()) {
            e.add(v.toUnit(a, timespanPattern));
        }
        Double r = e.values.get(a);
        return new TimePeriod(r == null ? 0 : r, a);
    }

    @Override
    public String toString() {
        if(values.isEmpty()){
            return "0";
        }
        TreeSet<TimeUnit> r = new TreeSet<>(new Comparator<TimeUnit>() {
            @Override
            public int compare(TimeUnit o1, TimeUnit o2) {
                return o2.compareTo(o1);
            }
        });
        r.addAll(values.keySet());
        return r.stream().map(x->{
            return new TimePeriod(values.get(x),x).toString();
        }).collect(Collectors.joining(" "));
    }
}
