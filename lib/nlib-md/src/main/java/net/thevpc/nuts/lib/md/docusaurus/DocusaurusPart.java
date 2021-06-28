/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.docusaurus;

/**
 *
 * @author thevpc
 */
public class DocusaurusPart {

    private String title;
    private DocusaurusFile[] pages;

    public DocusaurusPart(String title, DocusaurusFile[] pages) {
        this.title = title;
        this.pages = pages;
    }

    public String getTitle() {
        return title;
    }

    public DocusaurusFile[] getPages() {
        return pages;
    }

    @Override
    public String toString() {
        return "DocusaurusPart{" +title + '}';
    }
    

}
