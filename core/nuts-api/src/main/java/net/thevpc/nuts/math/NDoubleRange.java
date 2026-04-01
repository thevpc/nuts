package net.thevpc.nuts.math;

public class NDoubleRange {
    private double min = Double.NaN;
    private double max = Double.NaN;
    private final boolean finite;

    public static NDoubleRange of() {
        return new NDoubleRange(false);
    }

    public static NDoubleRange ofFinite() {
        return new NDoubleRange(true);
    }

    public NDoubleRange(boolean finite) {
        this.finite = finite;
    }

    public boolean isFinite() {
        return finite;
    }

    public void addAbs(double d) {
        add(Math.abs(d));
    }

    public void addAbs(double[] d) {
        for (double aD : d) {
            addAbs(aD);
        }
    }

    public void addAbs(double[][] d) {
        for (double[] aD : d) {
            addAbs(aD);
        }
    }

    public void addAbs(double[][][] d) {
        for (double[][] aD : d) {
            addAbs(aD);
        }
    }


    public void add(double[] d) {
        for (double aD : d) {
            add(aD);
        }
    }

    public void add(double d) {
        if (Double.isNaN(d)) return;
        if (finite && !Double.isFinite(d)) return;

        if (Double.isNaN(min) || d < min) {
            min = d;
        }
        if (Double.isNaN(max) || d > max) {
            max = d;
        }
    }

    public void add(double[][] d) {
        for (double[] aD : d) {
            add(aD);
        }
    }

    public void add(double[][][] d) {
        for (double[][] z : d) {
            add(z);
        }
    }


    public void add(NDoubleRange other) {
        if (other == null || !other.isSet()) return;
        add(other.min);
        add(other.max);
    }

    public double ratio(double z) {
        if (Double.isNaN(z)) {
            return Double.NaN;
        }
        double w = (max - min);
        if (w == 0) {
            return 0;
        }
        return (z - min) / w;
    }

    public boolean isSet() {
        return !Double.isNaN(min);
    }

    public double min() {
        return min;
    }

    public double length() {
        return isSet() ? (max - min) : Double.NaN;
    }

    public double max() {
        return max;
    }

    /**
     * Constraints the value d to the current [min, max] range.
     * If the range is not set, returns the original value.
     */
    public double clamp(double d) {
        if (!isSet() || Double.isNaN(d)) {
            return d;
        }
        if (d < min) return min;
        if (d > max) return max;
        return d;
    }

    @Override
    public int hashCode() {
        int result;
        result = Double.hashCode(min);
        result = 31 * result + Double.hashCode(max);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NDoubleRange minMax = (NDoubleRange) o;

        if (Double.compare(minMax.min, min) != 0) return false;
        return Double.compare(minMax.max, max) == 0;
    }

    @Override
    public String toString() {
        return "{" + "min=" + min + ", max=" + max + '}';
    }

    public boolean contains(double d) {
        if (!isSet() || Double.isNaN(d)) return false;
        return d >= min && d <= max;
    }

}
