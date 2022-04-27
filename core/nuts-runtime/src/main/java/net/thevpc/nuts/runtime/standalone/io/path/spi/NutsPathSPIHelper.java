package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.iter.NutsIteratorBase;

import java.util.Iterator;
import java.util.Stack;

public class NutsPathSPIHelper {
    private static class Data{
        NutsPath p;
        int depth;
        boolean folder;
        boolean visited;

        public Data(NutsPath p, int depth, boolean folder) {
            this.p = p;
            this.depth = depth;
            this.folder = folder;
        }
    }
    public static NutsStream<NutsPath> walk(NutsSession session, NutsPath basePath, int maxDepth, NutsPathOption[] options) {
        boolean noMax=maxDepth<=0 || maxDepth==Integer.MAX_VALUE;
        Iterator<NutsPath> it = new NutsPathIterator(basePath, noMax, maxDepth);
        return NutsStream.of(it, session);
    }

    public static void walkDfs(NutsSession session,NutsPath basePath, NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options) {
        boolean noMax=maxDepth<=0 || maxDepth==Integer.MAX_VALUE;

        Stack<Data> stack=new Stack<>();
        stack.push(new Data(basePath,0,true));
        while(!stack.isEmpty()){
            Data i = stack.peek();
            if(i.folder){
                if(!i.visited){
                    i.visited=true;
                    NutsTreeVisitResult r = visitor.preVisitDirectory(i.p, session);
                    switch (r){
                        case TERMINATE:return;
                        case SKIP_SUBTREE: {
                            break;
                        }
                        case SKIP_SIBLINGS:
                        case CONTINUE:
                        {
                            if(noMax || i.depth<maxDepth) {
                                for (NutsPath c : i.p.list()) {
                                    stack.push(new Data(c, i.depth + 1, c.isDirectory()));
                                }
                            }
                        }
                    }
                }else{
                    stack.pop();
                    NutsTreeVisitResult r = visitor.postVisitDirectory(i.p, null,session);
                    switch (r){
                        case TERMINATE:return;
                        case SKIP_SUBTREE: {
                            break;
                        }
                        case SKIP_SIBLINGS:
                        case CONTINUE:
                        {
                            break;
                        }
                    }
                }
            }else {
                stack.pop();
            }
        }
    }

    private static class NutsPathIterator extends NutsIteratorBase<NutsPath> {
        private final boolean noMax;
        private final int maxDepth;
        private final NutsPath basePath;
        Stack<Data> stack;

        public NutsPathIterator(NutsPath basePath, boolean noMax, int maxDepth) {
            this.noMax = noMax;
            this.maxDepth = maxDepth;
            this.basePath = basePath;
            stack = new Stack<>();
            stack.push(new Data(basePath, 0, basePath.isDirectory()));
        }

        @Override
        public NutsElement describe(NutsSession session) {
            return NutsElements.of(session).ofObject()
                    .set("type","ScanPath")
                    .set("path", NutsElements.of(session).toElement(basePath))
                    .set("maxDepth",maxDepth)
                    .build();
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public NutsPath next() {
            Data i = stack.pop();
            if(i.folder){
                if(noMax || i.depth< maxDepth) {
                    for (NutsPath c : i.p.list()) {
                        stack.push(new Data(c, i.depth + 1, c.isDirectory()));
                    }
                }
            }
            return i.p;
        }
    }
}
