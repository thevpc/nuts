package net.vpc.toolbox.ntasks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NTimePeriods {
    private Map<TimeUnit, Double> values = new HashMap<>();

    public NTimePeriods add(NTimePeriod p) {
        Double d = values.get(p.getUnit());
        if (d == null) {
            d = 0.0;
        }
        d += p.getCount();
        values.put(p.getUnit(), d);
        return this;
    }

    public NTimePeriod[] getPeriodArray() {
        NTimePeriod[] nTimePeriods = values.entrySet().stream().map(x -> new NTimePeriod(x.getValue(), x.getKey())).toArray(NTimePeriod[]::new);
        Arrays.sort(nTimePeriods);
        return nTimePeriods;
    }

    public NTimePeriod getPart(TimeUnit a) {
        Double r = values.get(a);
        return r == null ? new NTimePeriod(0, a) : new NTimePeriod(r, a);
    }

    public NTimePeriod toUnit(TimeUnit a, double dayHours) {
        NTimePeriods e = new NTimePeriods();
        for (NTimePeriod v : getPeriodArray()) {
            e.add(v.toUnit(a, dayHours));
        }
        Double r = e.values.get(a);
        return new NTimePeriod(r == null ? 0 : r, a);
    }
}
