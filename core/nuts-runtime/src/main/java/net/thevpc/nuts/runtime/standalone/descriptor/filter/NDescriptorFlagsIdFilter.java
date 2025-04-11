///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.runtime.standalone.descriptor.filter;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.util.NFilterOp;
//
//import java.util.LinkedHashSet;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// *
// * @author thevpc
// */
//public class NDescriptorFlagsIdFilter extends AbstractDescriptorFilter {
//
//    private final Set<NDescriptorFlag> flags;
//
//    public NDescriptorFlagsIdFilter(NDescriptorFlag...flags) {
//        super(NFilterOp.CUSTOM);
//        this.flags = new LinkedHashSet<>();
//        for (NDescriptorFlag flag : flags) {
//            if(flag!=null){
//                this.flags.add(flag);
//            }
//        }
//    }
//
//    @Override
//    public boolean acceptDescriptor(NDescriptor other) {
//        Set<NDescriptorFlag> available = other.getFlags();
//        for (NDescriptorFlag flag : this.flags) {
//            if(!available.contains(flag)){
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public NDescriptorFilter simplify() {
//        if (flags.isEmpty()) {
//            return null;
//        }
//        return this;
//    }
//
//    @Override
//    public String toString() {
//        if(flags.isEmpty()){
//            return "any";
//        }
//        if(flags.size()==1){
//            return "hasFlag("+flags.toArray(new NDescriptorFlag[0])[0].id()+")";
//        }
//        return "hasFlags("+
//                flags.stream().map(NDescriptorFlag::id).collect(Collectors.joining(","))
//                +")";
//    }
//
//}
