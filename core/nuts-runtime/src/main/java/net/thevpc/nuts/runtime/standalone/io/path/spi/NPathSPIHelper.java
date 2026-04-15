package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.text.NTreeVisitResult;
import net.thevpc.nuts.text.NTreeVisitor;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.util.NIteratorBase;
import net.thevpc.nuts.util.NStream;

import java.util.*;

public class NPathSPIHelper {
    private static class Data {
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

    public static NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
        boolean noMax = maxDepth <= 0 || maxDepth == Integer.MAX_VALUE;
        Iterator<NPath> it = new NPathIterator(basePath, noMax, maxDepth, options);
        return NStream.ofIterator(it);
    }

    public static void walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        boolean noMax = maxDepth <= 0 || maxDepth == Integer.MAX_VALUE;
        // Check for the SORTED option
        boolean sorted = options != null && Arrays.asList(options).contains(NPathOption.SORTED);

        Stack<Data> stack = new Stack<>();
        stack.push(new Data(basePath, 0, basePath.isDirectory()));

        while (!stack.isEmpty()) {
            Data i = stack.peek();

            if (i.folder) {
                if (!i.visited) {
                    i.visited = true;
                    NTreeVisitResult r = visitor.preVisitDirectory(i.p);
                    switch (r) {
                        case TERMINATE:
                            return;
                        case SKIP_SUBTREE:
                            break;
                        case SKIP_SIBLINGS:
                        case CONTINUE: {
                            if (noMax || i.depth < maxDepth) {
                                List<NPath> children = i.p.stream().toList();

                                if (sorted) {
                                    // Deterministic sort by name
                                    children.sort(Comparator.comparing(NPath::getName));
                                }

                                // PUSH IN REVERSE:
                                // If sorted list is [A, B, C], pushing C then B then A
                                // ensures that A is the first one popped and visited.
                                for (int j = children.size() - 1; j >= 0; j--) {
                                    NPath c = children.get(j);
                                    stack.push(new Data(c, i.depth + 1, c.isDirectory()));
                                }
                            }
                            break;
                        }
                    }
                } else {
                    stack.pop();
                    if (visitor.postVisitDirectory(i.p, null) == NTreeVisitResult.TERMINATE) {
                        return;
                    }
                }
            } else {
                // It's a file
                stack.pop();
                if (visitor.visitFile(i.p) == NTreeVisitResult.TERMINATE) {
                    return;
                }
            }
        }
    }

    private static class NPathIterator extends NIteratorBase<NPath> {
        private final boolean noMax;
        private final int maxDepth;
        private final NPath basePath;
        private final boolean sorted; // Store sorted state
        private final Stack<Data> stack;

        public NPathIterator(NPath basePath, boolean noMax, int maxDepth, NPathOption[] options) {
            this.noMax = noMax;
            this.maxDepth = maxDepth;
            this.basePath = basePath;
            this.sorted = options != null && Arrays.asList(options).contains(NPathOption.SORTED);
            this.stack = new Stack<>();
            this.stack.push(new Data(basePath, 0, basePath.isDirectory()));
        }

        @Override
        public NElement describe() {
            return NElement.ofObjectBuilder()
                    .name("ScanPath")
                    .set("path", NElements.of().toElement(basePath))
                    .set("maxDepth", maxDepth)
                    .set("sorted", sorted) // Good for Nuts debugging
                    .build();
        }

        @Override
        public boolean hasNextImpl() {
            return !stack.isEmpty();
        }

        @Override
        public NPath next() {
            Data i = stack.pop();
            if (i.folder) {
                if (noMax || i.depth < maxDepth) {
                    List<NPath> children = i.p.stream().toList();

                    if (sorted) {
                        children.sort(Comparator.comparing(NPath::getName));
                    }
                    for (int j = children.size() - 1; j >= 0; j--) {
                        NPath c = children.get(j);
                        stack.push(new Data(c, i.depth + 1, c.isDirectory()));
                    }
                }
            }
            return i.p;
        }
    }
}
