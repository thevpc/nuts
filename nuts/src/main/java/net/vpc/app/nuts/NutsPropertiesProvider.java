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
 */
public interface NutsPropertiesProvider {
    Map<String, Object> getProperties();

    Object getProperty(String key);

    NutsPropertiesProvider setProperty(String key, Object value);

    NutsPropertiesProvider setProperties(Map<String, Object> properties);
}
