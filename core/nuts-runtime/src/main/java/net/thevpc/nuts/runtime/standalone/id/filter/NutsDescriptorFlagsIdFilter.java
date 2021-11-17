/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.filter.AbstractDescriptorFilter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class NutsDescriptorFlagsIdFilter extends AbstractDescriptorFilter {

    private final Set<NutsDescriptorFlag> flags;

    public NutsDescriptorFlagsIdFilter(NutsSession session, NutsDescriptorFlag ...flags) {
        super(session, NutsFilterOp.CUSTOM);
        this.flags = new LinkedHashSet<>();
        for (NutsDescriptorFlag flag : flags) {
            if(flag!=null){
                this.flags.add(flag);
            }
        }
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor other, NutsSession session) {
        Set<NutsDescriptorFlag> available = other.getFlags();
        for (NutsDescriptorFlag flag : this.flags) {
            if(!available.contains(flag)){
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        if (flags.isEmpty()) {
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        if(flags.isEmpty()){
            return "any";
        }
        if(flags.size()==1){
            return "hasFlag("+flags.toArray(new NutsDescriptorFlag[0])[0].id()+")";
        }
        return "hasFlags("+
                flags.stream().map(NutsDescriptorFlag::id).collect(Collectors.joining(","))
                +")";
    }

}
