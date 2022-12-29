package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NTreeVisitResult;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.runtime.standalone.util.iter.NIteratorBase;
import net.thevpc.nuts.util.NStream;

import java.util.Iterator;
import java.util.Stack;

public class NPathSPIHelper {
    private static class Data{
        NPath p;
        int depth;
        boolean folder;
        boolean visited;

        public Data(NPath p, int depth, boolean folder) {
            this.p = p;
            this.depth = depth;
            this.folder = folder;
        }
    }
    public static NStream<NPath> walk(NSession session, NPath basePath, int maxDepth, NPathOption[] options) {
        boolean noMax=maxDepth<=0 || maxDepth==Integer.MAX_VALUE;
        Iterator<NPath> it = new NPathIterator(basePath, noMax, maxDepth);
        return NStream.of(it, session);
    }

    public static void walkDfs(NSession session, NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        boolean noMax=maxDepth<=0 || maxDepth==Integer.MAX_VALUE;

        Stack<Data> stack=new Stack<>();
        stack.push(new Data(basePath,0,true));
        while(!stack.isEmpty()){
            Data i = stack.peek();
            if(i.folder){
                if(!i.visited){
                    i.visited=true;
                    NTreeVisitResult r = visitor.preVisitDirectory(i.p, session);
                    switch (r){
                        case TERMINATE:return;
                        case SKIP_SUBTREE: {
                            break;
                        }
                        case SKIP_SIBLINGS:
                        case CONTINUE:
                        {
                            if(noMax || i.depth<maxDepth) {
                                for (NPath c : i.p.stream()) {
                                    stack.push(new Data(c, i.depth + 1, c.isDirectory()));
                                }
                            }
                        }
                    }
                }else{
                    stack.pop();
                    NTreeVisitResult r = visitor.postVisitDirectory(i.p, null,session);
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

    private static class NPathIterator extends NIteratorBase<NPath> {
        private final boolean noMax;
        private final int maxDepth;
        private final NPath basePath;
        Stack<Data> stack;

        public NPathIterator(NPath basePath, boolean noMax, int maxDepth) {
            this.noMax = noMax;
            this.maxDepth = maxDepth;
            this.basePath = basePath;
            stack = new Stack<>();
            stack.push(new Data(basePath, 0, basePath.isDirectory()));
        }

        @Override
        public NElement describe(NSession session) {
            return NElements.of(session).ofObject()
                    .set("type","ScanPath")
                    .set("path", NElements.of(session).toElement(basePath))
                    .set("maxDepth",maxDepth)
                    .build();
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public NPath next() {
            Data i = stack.pop();
            if(i.folder){
                if(noMax || i.depth< maxDepth) {
                    for (NPath c : i.p.stream()) {
                        stack.push(new Data(c, i.depth + 1, c.isDirectory()));
                    }
                }
            }
            return i.p;
        }
    }
}
