package net.thevpc.nuts.toolbox.ndiff.jar;

import java.util.List;

public class DefaultDiffItem extends AbstractDiffItem{
    public DefaultDiffItem(String kind, String name, DiffStatus status, String description, List<DiffItem> details) {
        super(kind, name, status, description, details);
    }
}
