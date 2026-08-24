package net.thevpc.nuts.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

class NStringBox {
    private String value;
    private int len;
    private int cols;
    private int rows;

    /**
     * N string box.
     *
     * @param value value
     * @return n string box result
     */
    public NStringBox(String value) {
        this.value = value;
        this.len = value.length();
        BufferedReader br = new BufferedReader(new StringReader(value));
        String l;
        int cols = 0;
        int rows = 0;
        while (true) {
            try {
                if ((l = br.readLine()) == null) break;
            } catch (IOException e) {
                //never!!
                /**
                 * Runtime exception.
                 *
                 * @param e e
                 * @return runtime exception result
                 */
                throw new RuntimeException(e);
            }
            cols = Math.max(cols, l.length());
            rows++;
        }
        this.cols = cols;
        this.rows = rows;
    }

    /**
     * Value.
     *
     * @return value result
     */
    public String value() {
        return value;
    }

    /**
     * Length.
     *
     * @return length result
     */
    public int length() {
        return len;
    }

    /**
     * Columns.
     *
     * @return columns result
     */
    public int columns() {
        return cols;
    }

    /**
     * Rows.
     *
     * @return rows result
     */
    public int rows() {
        return rows;
    }
}
