/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.props;

import java.util.LinkedHashMap;
import java.util.Map;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.NFormatUtils;
import net.thevpc.nuts.util.NPropsTransformer;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatProps extends DefaultSearchFormatBase {

    public DefaultSearchFormatProps(NPrintStream writer, NFetchDisplayOptions options) {
        super(writer, NContentType.PROPS,options);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        if (getDisplayOptions().configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public void start() {
    }

    @Override
    public void complete(long count) {

    }

    @Override
    public void next(Object object, long index) {
        Map<String, String> p = new LinkedHashMap<>();
        NFormatUtils.putAllInProps(String.valueOf(index + 1), p,
                NElements.of()
                        .toElement(object)
        );
        NPropsTransformer.storeProperties(p, getWriter().asPrintStream(), false);
        getWriter().flush();
    }

}
