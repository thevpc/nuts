/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

/**
 *
 * @author vpc
 */
public class CypherInfo {

    private String algo;
    private String value;

    public CypherInfo() {
    }

    public CypherInfo(CypherInfo other) {
        if (other != null) {
            this.algo = other.algo;
            this.value = other.value;
        }
    }

    public CypherInfo(String algo, String value) {
        this.algo = algo;
        this.value = value;
    }

    public String getAlgo() {
        return algo;
    }

    public CypherInfo setAlgo(String algo) {
        this.algo = algo;
        return this;
    }

    public String getValue() {
        return value;
    }

    public CypherInfo setValue(String value) {
        this.value = value;
        return this;
    }

    public CypherInfo copy() {
        return new CypherInfo(this);
    }

}
