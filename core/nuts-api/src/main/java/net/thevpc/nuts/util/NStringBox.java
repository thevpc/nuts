package net.thevpc.nuts.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

class NStringBox {
    private String value;
    private int len;
    private int cols;
    private int rows;

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
                throw new RuntimeException(e);
            }
            cols = Math.max(cols, l.length());
            rows++;
        }
        this.cols = cols;
        this.rows = rows;
    }

    public String value() {
        return value;
    }

    public int length() {
        return len;
    }

    public int columns() {
        return cols;
    }

    public int rows() {
        return rows;
    }
}
