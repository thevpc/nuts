package net.vpc.app.nuts.toolbox.njob.time;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class TimePeriods {
    private Map<ChronoUnit, Double> values = new HashMap<>();

//    public static TimePeriods between(Instant a,Instant b){
//        TimePeriods timePeriods = new TimePeriods();
//        if(a.equals(b)){
//            return timePeriods;
//        }
//        int mul=1;
//        if(a.compareTo(b)>0){
//            Instant _b=b;
//            b=a;
//            a=_b;
//            mul=-1;
//        }
//        Calendar ac = Calendar.getInstance();
//        ac.setTimeInMillis(a.toEpochMilli());
//        Calendar bc = Calendar.getInstance();
//        bc.setTimeInMillis(a.toEpochMilli());
//
//        int ya = ac.get(Calendar.YEAR);
//        int yb = bc.get(Calendar.YEAR);
//        int da = ac.get(Calendar.MONTH);
//        int db = bc.get(Calendar.MONTH);
//        int diff_y=yb-ya;
//        if(db<da){
//            diff_y--;
//        }
//        ac.add(Calendar.YEAR,diff_y);
//        timePeriods.add(diff_y,ChronoUnit.YEAR);
//        ya = ac.get(Calendar.YEAR);
//        yb = bc.get(Calendar.YEAR);
//        da = ac.get(Calendar.MONTH);
//        db = bc.get(Calendar.MONTH);
//        int diff_y=yb-ya;
//        if(db<da){
//            diff_y--;
//        }
//        ac.add(Calendar.YEAR,diff_y);
//    }
    public TimePeriods add(double v, ChronoUnit t) {
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

    public TimePeriod getPart(ChronoUnit a) {
        Double r = values.get(a);
        return r == null ? new TimePeriod(0, a) : new TimePeriod(r, a);
    }

    public TimePeriod toUnit(ChronoUnit a, TimespanPattern timespanPattern) {
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
        TreeSet<ChronoUnit> r = new TreeSet<>(new Comparator<ChronoUnit>() {
            @Override
            public int compare(ChronoUnit o1, ChronoUnit o2) {
                return o2.compareTo(o1);
            }
        });
        r.addAll(values.keySet());
        return r.stream().map(x->{
            return new TimePeriod(values.get(x),x).toString();
        }).collect(Collectors.joining(" "));
    }
}
