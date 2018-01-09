package net.vpc.app.nuts.extensions.util;

public class JsonStringBuffer {

    private StringBuilder sb = new StringBuilder();
    private JsonStatus status = new JsonStatus();

    public boolean append(String line) {
        CoreJsonUtils.readJsonPartialString(line, status);
        status.checkPartialValid(true);
        sb.append(line);
        if (status.countBraces > 0 && status.checkValid(false)) {
            return true;
        }
        return true;
    }

    public String getValidString() {
        status.checkValid(true);
        return sb.toString();
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
