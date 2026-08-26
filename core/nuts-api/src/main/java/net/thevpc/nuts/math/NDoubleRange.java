package net.thevpc.nuts.math;

/**
 * NDoubleRange class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NDoubleRange {
    private double min = Double.NaN;
    private double max = Double.NaN;
    private final boolean finite;

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NDoubleRange of() {
        return new NDoubleRange(false);
    }

    /**
     * Creates a new instance of of finite.
     *
     * @return of finite result
     */
    public static NDoubleRange ofFinite() {
        return new NDoubleRange(true);
    }

    /**
     * N double range.
     *
     * @param finite finite
     * @return n double range result
     */
    public NDoubleRange(boolean finite) {
        this.finite = finite;
    }

    /**
     * Checks if is finite.
     *
     * @return is finite result
     */
    public boolean isFinite() {
        return finite;
    }

    /**
     * Adds the specified abs.
     *
     * @param d d
     */
    public void addAbs(double d) {
      /**
       * Adds add.
       *
       * @param Math.abs(d) math.abs(d)
       */
        add(Math.abs(d));
    }

    /**
     * Adds the specified abs.
     *
     * @param d d
     */
    public void addAbs(double[] d) {
        for (double aD : d) {
          /**
           * Adds the specified abs.
           *
           * @param aD a d
           */
            addAbs(aD);
        }
    }

    /**
     * Adds the specified abs.
     *
     * @param d d
     */
    public void addAbs(double[][] d) {
        for (double[] aD : d) {
          /**
           * Adds the specified abs.
           *
           * @param aD a d
           */
            addAbs(aD);
        }
    }

    /**
     * Adds the specified abs.
     *
     * @param d d
     */
    public void addAbs(double[][][] d) {
        for (double[][] aD : d) {
          /**
           * Adds the specified abs.
           *
           * @param aD a d
           */
            addAbs(aD);
        }
    }


    /**
     * Adds add.
     *
     * @param d d
     */
    public void add(double[] d) {
        for (double aD : d) {
          /**
           * Adds add.
           *
           * @param aD a d
           */
            add(aD);
        }
    }

    /**
     * Adds add.
     *
     * @param d d
     */
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

    /**
     * Adds add.
     *
     * @param d d
     */
    public void add(double[][] d) {
        for (double[] aD : d) {
          /**
           * Adds add.
           *
           * @param aD a d
           */
            add(aD);
        }
    }

    /**
     * Adds add.
     *
     * @param d d
     */
    public void add(double[][][] d) {
        for (double[][] z : d) {
          /**
           * Adds add.
           *
           * @param z z
           */
            add(z);
        }
    }


    /**
     * Adds add.
     *
     * @param other other
     */
    public void add(NDoubleRange other) {
        if (other == null || !other.isSet()) return;
      /**
       * Adds add.
       *
       * @param other.min other.min
       */
        add(other.min);
      /**
       * Adds add.
       *
       * @param other.max other.max
       */
        add(other.max);
    }

    /**
     * Ratio.
     *
     * @param z z
     * @return ratio result
     */
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

    /**
     * Checks if is set.
     *
     * @return is set result
     */
    public boolean isSet() {
        return !Double.isNaN(min);
    }

    /**
     * Min.
     *
     * @return min result
     */
    public double min() {
        return min;
    }

    /**
     * Length.
     *
     * @return length result
     */
    public double length() {
        return isSet() ? (max - min) : Double.NaN;
    }

    /**
     * Max.
     *
     * @return max result
     */
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

    /**
     * Contains.
     *
     * @param d d
     * @return contains result
     */
    public boolean contains(double d) {
        if (!isSet() || Double.isNaN(d)) return false;
        return d >= min && d <= max;
    }

}
