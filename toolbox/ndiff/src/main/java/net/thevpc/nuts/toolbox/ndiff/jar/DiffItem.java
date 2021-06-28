/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndiff.jar;

import java.util.List;

/**
 * @author thevpc
 */
public interface DiffItem {

    String getName();

    String getKind();

    DiffStatus getStatus();

    String getDescription();

    List<DiffItem> children();
}
