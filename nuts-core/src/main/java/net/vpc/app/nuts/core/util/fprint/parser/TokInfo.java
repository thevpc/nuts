/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.fprint.parser;

/**
 *
 * @author vpc
 */
public class TokInfo {
    
    int min;
    int max;
    char end;

    public TokInfo(int min, int max, char end) {
        this.min = min;
        this.max = max;
        this.end = end;
    }
    
}
