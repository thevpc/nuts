package net.thevpc.nuts.runtime.standalone.text.art.table;

public class Interval {

    int from;
    int to;

    public Interval(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public int hashCode() {
        int result = from;
        result = 31 * result + to;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Interval interval = (Interval) o;

        if (from != interval.from) {
            return false;
        }
        return to == interval.to;

    }

    @Override
    public String toString() {
        return "Interval{"
                + "from=" + from
                + ", to=" + to
                + '}';
    }
}
