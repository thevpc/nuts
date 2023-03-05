/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.eval;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

/**
 *
 * @author thevpc
 */
public class JShellResult implements Serializable{

    private final String message;
    private final int code;
    private final String stackTrace;

    public JShellResult(int code, String message, Throwable throwable) {
        this.code = code;
        this.message = message;
        if (throwable != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(bos);
            throwable.printStackTrace(out);
            out.flush();
            this.stackTrace = bos.toString();
        } else {
            this.stackTrace = "";
        }
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public String getStackTrace() {
        return stackTrace;
    }

}
