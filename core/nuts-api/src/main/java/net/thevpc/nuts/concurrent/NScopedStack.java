package net.thevpc.nuts.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

public class NScopedStack<T> {
    private InheritableThreadLocal<Stack<T>> holder = new InheritableThreadLocal<>();
    private Supplier<T> defaultSupplier;

    public NScopedStack(Supplier<T> defaultSupplier) {
        this.defaultSupplier = defaultSupplier;
    }

    public T get() {
        Stack<T> s = holder.get();
        T h=null;
        if(s!=null) {
            h = s.peek();
            if (h != null) {
                return h;
            }
        }
        if (defaultSupplier != null) {
            h = defaultSupplier.get();
        }
        return h;
    }

    public void runWith(T value, Runnable r) {
        Stack<T> s = holder.get();

        if (s!=null && !s.isEmpty()) {
            T old = s.peek();
            if (old == value) {
                if (r != null) {
                    r.run();
                }
            }else{
                s.push(value);
                try {
                    if (r != null) {
                        r.run();
                    }
                } finally {
                    s.pop();
                    if (s.isEmpty()) {
                        holder.remove();
                    }
                }
            }
        }else{
            if(s==null){
                holder.set(s=new Stack<>());
            }
            s.push(value);
            try {
                if (r != null) {
                    r.run();
                }
            } finally {
                s.pop();
                if (s.isEmpty()) {
                    holder.remove();
                }
            }
        }
    }

    public <V> V callWith(T value, NCallable<V> c) {
        Stack<T> s = holder.get();

        if (s!=null && !s.isEmpty()) {
            T old = s.peek();
            if (old == value) {
                if (c != null) {
                    return c.call();
                }
                return null;
            }else{
                s.push(value);
                try {
                    if (c != null) {
                        return c.call();
                    }
                    return null;
                } finally {
                    s.pop();
                    if (s.isEmpty()) {
                        holder.remove();
                    }
                }
            }
        }else{
            if(s==null){
                holder.set(s=new Stack<>());
            }
            s.push(value);
            try {
                if (c != null) {
                    return c.call();
                }
                return null;
            } finally {
                s.pop();
                if (s.isEmpty()) {
                    holder.remove();
                }
            }
        }
    }
    public boolean isEmpty() {
        Stack<T> s = holder.get();
        return s == null || s.isEmpty();
    }

    public int depth() {
        Stack<T> s = holder.get();
        return s == null ? 0 : s.size();
    }

    public List<T> getStackSnapshot() {
        Stack<T> s = holder.get();
        return s == null ? Collections.emptyList() : new ArrayList<>(s);
    }

    public T peek(int levelFromTop) {
        Stack<T> s = holder.get();
        if (s == null || s.isEmpty()) return null;
        int index = s.size() - 1 - levelFromTop;
        return (index >= 0) ? s.get(index) : null;
    }
}
