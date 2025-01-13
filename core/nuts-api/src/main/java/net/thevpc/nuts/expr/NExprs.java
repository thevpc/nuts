/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.expr;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NFunction2;
import net.thevpc.nuts.util.NOptional;

/**
 * Simple Expression Parser Module used in multiple syb-systems of nuts (such as search)
 */
public interface NExprs extends NComponent {
    static NExprs of() {
        return NExtensions.of(NExprs.class);
    }

    static NExprVar ofVar(String name) {
        return of().newVar(name);
    }

    static NExprVar ofConst(String name, Object value) {
        return of().newConst(name, value);
    }

    NExprVar newVar(String var);

    NExprVar newConst(String name, Object value);

    NExprDeclarations newDeclarations();

    NExprDeclarations newDeclarations(boolean includeDefaults);

    NExprDeclarations newDeclarations(boolean includeDefaults, NExprEvaluator evaluator);

    NExprMutableDeclarations newMutableDeclarations(NExprEvaluator evaluator);

    NExprMutableDeclarations newMutableDeclarations(boolean includeDefaults, NExprEvaluator evaluator);

    NExprMutableDeclarations newMutableDeclarations(boolean includeDefaults);

    NExprMutableDeclarations newMutableDeclarations();

    <A, B> NOptional<NFunction2<A, B, ?>> findCommonInfixOp(NExprCommonOp op, Class<? extends A> firstArgType, Class<? extends B> secondArgType);

    <A> NOptional<NFunction<A, ?>> findCommonPrefixOp(NExprCommonOp op, Class<? extends A> argType);

    <A> NOptional<NFunction<A, ?>> findCommonPostfixOp(NExprCommonOp op, Class<? extends A> argType);
}
