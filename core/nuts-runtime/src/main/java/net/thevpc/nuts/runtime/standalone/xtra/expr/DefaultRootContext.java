package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NOperatorAssociativity;
import net.thevpc.nuts.internal.expr.NExprRPI;
import net.thevpc.nuts.runtime.standalone.reflect.NReflectSignatureImpl;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.expr.*;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultRootContext extends NExprContextBase {
    final Map<String, NExprFunction> defaultFunctions = new HashMap<>();
    final Map<String, NExprFunction> defaultConstructs = new HashMap<>();
    final Map<NExprOpNameAndType, NExprOperator> ops = new HashMap<>();
    final Map<String, NExprVar> defaultVars = new HashMap<>();
    private final NReflectRepository reflectRepository;

    public DefaultRootContext(NExprRPI nExprRPI) {
        super(nExprRPI);
        reflectRepository = NReflectRepository.of();

    }





    @Override
    public NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue... args) {
        return NOptional.of(
                defaultFunctions.get(fctName),
                () -> NMsg.ofC("function not found %s", fctName)
        );
    }

    @Override
    public NOptional<NExprFunction> getConstruct(String constructName, NExprNodeValue... args) {
        return NOptional.of(
                defaultConstructs.get(constructName),
                () -> NMsg.ofC("construct not found %s", constructName)
        );
    }

    @Override
    public NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprNodeValue... args) {
        return NOptional.of(
                ops.get(new NExprOpNameAndType(opName, type)),
                () -> NMsg.ofC("operator not found %s", opName)
        );
    }

    @Override
    public NOptional<NExprVar> getVar(String varName) {
        return NOptional.of(
                defaultVars.get(varName),
                () -> NMsg.ofC("expr var not found %s", varName)
        );
    }

    @Override
    public List<NExprOperator> getOperators() {
        List<NExprOperator> all = new ArrayList<>();
        all.addAll(ops.values());
        return all;
    }

}
