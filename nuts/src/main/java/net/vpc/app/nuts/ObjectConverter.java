package net.vpc.app.nuts;

public interface ObjectConverter<A,B>{
    B convert(A from);
}
