/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.io;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

import net.vpc.app.nuts.NutsTextFormatStyle;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceAware;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.fprint.*;
import net.vpc.app.nuts.runtime.util.fprint.util.FormattedPrintStreamUtils;
import net.vpc.app.nuts.NutsTerminalFormat;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;

/**
 * @author vpc
 */
public class DefaultNutsTerminalFormat implements NutsTerminalFormat, NutsWorkspaceAware {
    private NutsWorkspace ws;

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.ws=workspace;
    }

    @Override
    public int textLength(String value) {
        return filterText(value).length();
    }

    @Override
    public String filterText(String value) {
        return FormattedPrintStreamUtils.filterText(value);
    }

    @Override
    public String escapeText(String str) {
        if (str == null) {
            return "";
        }
        return FormattedPrintStreamUtils.escapeText(str);
    }

    /**
     * @param style
     * @param locale
     * @param format
     * @param args
     * @return
     */
    @Override
    public String formatText(NutsTextFormatStyle style, Locale locale, String format, Object... args) {
        if (style == NutsTextFormatStyle.CSTYLE) {
            return FormattedPrintStreamUtils.formatCStyle(ws,locale, format, args);
        } else {
            return FormattedPrintStreamUtils.formatPositionalStyle(ws,locale, format, args);
        }
    }

    @Override
    public String formatText(NutsTextFormatStyle style, String format, Object... args) {
        if (style == NutsTextFormatStyle.CSTYLE) {
            return FormattedPrintStreamUtils.formatCStyle(ws,Locale.getDefault(), format, args);
        } else {
            return FormattedPrintStreamUtils.formatPositionalStyle(ws,Locale.getDefault(), format, args);
        }
    }

    @Override
    public PrintStream prepare(PrintStream out) {
        return CoreIOUtils.toPrintStream(out,ws);
    }

    @Override
    public PrintWriter prepare(PrintWriter out) {
        return CoreIOUtils.toPrintWriter(out,ws);
    }

    @Override
    public boolean isFormatted(OutputStream out) {
        if (out instanceof ExtendedFormatAware) {
            NutsTerminalModeOp op = ((ExtendedFormatAware) out).getModeOp();
            return op!=NutsTerminalModeOp.NOP;
        }
        return false;
    }

    @Override
    public boolean isFormatted(Writer out) {
        if (out instanceof ExtendedFormatAware) {
            NutsTerminalModeOp op = ((ExtendedFormatAware) out).getModeOp();
            return op!=NutsTerminalModeOp.NOP;
        }
        return false;
    }

}
