/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.mvn;

import java.net.URL;

/**
 *
 * @author vpc
 */
interface URLFilter {

    public boolean accept(URL path);
    
}
