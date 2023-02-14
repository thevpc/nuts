/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog.model;


import net.thevpc.nuts.toolbox.nlog.filter.AndLineFilter;

import java.nio.file.Path;

/**
 * @author vpc
 */
public class NLogFilterConfig implements Cloneable {

    private Long from;
    private Long to;
    private Path path;
    private Integer windowMin;
    private Integer windowMax;
    private boolean caseInsensitive;
    private boolean lineNumber;
    private LineFilter filter;

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public LineFilter getFilter() {
        return filter;
    }

    public void setFilter(LineFilter filter) {
        this.filter = filter;
    }

    public Integer getWindowMin() {
        return windowMin;
    }

    public void setWindowMin(Integer windowMin) {
        this.windowMin = windowMin;
    }

    public Integer getWindowMax() {
        return windowMax;
    }

    public void setWindowMax(Integer windowMax) {
        this.windowMax = windowMax;
    }

    public NLogFilterConfig copy() {
        NLogFilterConfig c = clone();
        c.filter = filter == null ? new AndLineFilter() : (AndLineFilter) c.filter.copy();
        return c;
    }

    @Override
    protected NLogFilterConfig clone() {
        try {
            return (NLogFilterConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public NLogFilterConfig setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
        return this;
    }

    public boolean isLineNumber() {
        return lineNumber;
    }

    public NLogFilterConfig setLineNumber(boolean lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }
}
