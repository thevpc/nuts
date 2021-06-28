/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndiff.jar;

import java.util.Collections;
import java.util.List;

/**
 * @author thevpc
 */
public abstract class AbstractDiffItem implements DiffItem {

    private final String name;
    private final String kind;
    private final DiffStatus status;
    private final String description;
    private final List<DiffItem> details;

    public AbstractDiffItem(String kind, String name, DiffStatus status, String description,List<DiffItem> details) {
        this.kind = kind;
        this.name = name;
        this.status = status;
        this.description = description;
        this.details = details==null?Collections.EMPTY_LIST:details;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getKind() {
        return kind;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DiffStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        DiffStatus kind = getStatus();
        String c = kind == DiffStatus.ADDED ? "+ " : kind == DiffStatus.REMOVED ? "- " : kind == DiffStatus.CHANGED ? "~ " : "? ";
        return c + getKind() + " : " + getName() + (description == null ? "" : (" (" + description+")"));
    }

    @Override
    public List<DiffItem> children() {
        return details;
    }

}
