/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.io;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;
import net.vpc.app.nuts.NutsTerminalFormat;
import net.vpc.app.nuts.core.util.fprint.util.FormattedPrintStreamUtils;

/**
 *
 * @author vpc
 */
public class SimpleNutsTerminalFormat implements NutsTerminalFormat {

    public static final SimpleNutsTerminalFormat INSTANCE = new SimpleNutsTerminalFormat();

    @Override
    public int textLength(String value) {
        return filterText(value).length();
    }

    @Override
    public String filterText(String value) {
        return String.valueOf(value);
    }

    @Override
    public String escapeText(String value) {
        return String.valueOf(value);
    }

    @Override
    public String formatText(Locale locale, String format, Object... args) {
        return FormattedPrintStreamUtils.format0(locale, format, args);
    }

    @Override
    public String formatText(String format, Object... args) {
        return formatText(Locale.getDefault(), format, args);
    }

    @Override
    public PrintStream prepare(PrintStream out) {
        return out;
    }

    @Override
    public PrintWriter prepare(PrintWriter out) {
        return out;
    }

    @Override
    public boolean isFormatted(OutputStream out) {
        return false;
    }

    @Override
    public boolean isFormatted(Writer out) {
        return false;
    }
    

}
