//package net.thevpc.nuts.runtime.standalone.extensions.apiimpl;
//
//import net.thevpc.nuts.NutsVal;
//import net.thevpc.nuts.runtime.standalone.xtra.vals.DefaultNutsVal;
//import net.thevpc.nuts.runtime.standalone.extensions.ImplHelper;
//
//public class NutsValImplHelper implements ImplHelper<NutsVal> {
//    @Override
//    public NutsVal createApiTypeInstance(String name, Class[] argTypes, Object[] args) {
//        switch (name){
//            case "default":{
//                switch (args.length){
//                    case 1:{
//                        return new DefaultNutsVal(args[0]);
//                    }
//                }
//                break;
//            }
//        }
//        return null;
//    }
//}
