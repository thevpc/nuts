/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NFilterOp;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class NDefinitionFlagsIdFilter extends AbstractDefinitionFilter {

    private final Set<NDescriptorFlag> flags;
    private final boolean effectiveFlag;

    public NDefinitionFlagsIdFilter(boolean effectiveFlag, NDescriptorFlag... flags) {
        super(NFilterOp.CUSTOM);
        this.flags = new LinkedHashSet<>();
        this.effectiveFlag = effectiveFlag;
        for (NDescriptorFlag flag : flags) {
            if (flag != null) {
                this.flags.add(flag);
            }
        }
    }

    @Override
    public boolean acceptDefinition(NDefinition other) {
        Set<NDescriptorFlag> available = other.getDescriptor().getFlags();
        if (effectiveFlag) {
            for (NDescriptorFlag flag : this.flags) {
                if (!available.contains(flag)) {
                    return false;
                }
            }
            Set<NDescriptorFlag> af = other.getEffectiveFlags().get();
            for (NDescriptorFlag flag : this.flags) {
                if (!af.contains(flag)) {
                    return false;
                }
            }
        } else {
            for (NDescriptorFlag flag : this.flags) {
                if (!available.contains(flag)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public NDefinitionFilter simplify() {
        if (flags.isEmpty()) {
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        if (flags.isEmpty()) {
            return "any";
        }
        if (effectiveFlag) {
            if (flags.size() == 1) {
                return "hasEffectiveFlag(" + flags.toArray(new NDescriptorFlag[0])[0].id() + ")";
            }
            return "hasEffectiveFlag(" +
                    flags.stream().map(NDescriptorFlag::id).collect(Collectors.joining(","))
                    + ")";
        } else {
            if (flags.size() == 1) {
                return "hasFlag(" + flags.toArray(new NDescriptorFlag[0])[0].id() + ")";
            }
            return "hasFlags(" +
                    flags.stream().map(NDescriptorFlag::id).collect(Collectors.joining(","))
                    + ")";
        }
    }

}
