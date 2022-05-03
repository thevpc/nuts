///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.runtime.standalone.io.util;
//
//import net.thevpc.nuts.io.NutsInputSource;
//import net.thevpc.nuts.io.NutsInputSourceMetadata;
//
//import java.io.FilterInputStream;
//import java.io.InputStream;
//
///**
// *
// * @author thevpc
// */
//public class InputStreamMetadataAwareImpl extends FilterInputStream implements NutsInputSource {
//
//    private NutsInputSourceMetadata metadata;
//
//
//    public static InputStreamMetadataAwareImpl of(InputStream in, NutsInputSourceMetadata metadata) {
//        if(in instanceof InputStreamMetadataAwareImpl){
//            return new InputStreamMetadataAwareImpl(((InputStreamMetadataAwareImpl) in).in,metadata);
//        }else{
//            return new InputStreamMetadataAwareImpl(in,metadata);
//        }
//    }
//
//    public InputStreamMetadataAwareImpl(InputStream in, NutsInputSourceMetadata metadata) {
//        super(in);
//        this.metadata = metadata;
//    }
//
//    @Override
//    public NutsInputSourceMetadata getInputMetaData() {
//        return metadata;
//    }
//
//    @Override
//    public String toString() {
//        NutsInputSourceMetadata md = getInputSourceMetadata();
//        if(md!=null) {
//            String n = md.getName();
//            if (n != null) {
//                return n;
//            }
//        }
//        return super.toString();
//    }
//}
