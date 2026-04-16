package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.util.NDecision;

import java.lang.reflect.Type;

public interface GetterNameHeuristicFilter {
    NDecision decide(String name, String[] nameParts, Type returnType, Type declaringType);
}
