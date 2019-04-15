/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Map;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsTerminalProvider extends NutsPropertiesProvider{
    NutsSessionTerminal getTerminal();
    @Override
    NutsTerminalProvider setProperty(String key, Object value);

    @Override
    NutsTerminalProvider setProperties(Map<String, Object> properties);
}
