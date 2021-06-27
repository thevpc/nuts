/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.bundles.strings;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * @author taha.bensalah@gmail.com
 */
public class StringTransforms {
    public static String normalizeString(String expression) {
        if (expression == null) {
            return null;
        }
        String nfdNormalizedString = Normalizer.normalize(expression, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }
    public static final StringTransform LOWER = new LowerStringTransform();


    public static final StringTransform UNIFORM = new UniformStringTransform();


    public static final StringTransform TRIM = new TrimStringTransform();

    public static final StringTransform NOT_NULL = new NotNullStringTransform();

    public static final StringTransform TRIMMED_NOT_NULL = NOT_NULL.apply(TRIM);


    private static class LowerStringTransform extends AbstractStringTransform {

        public LowerStringTransform() {
        }

        @Override
        public String transform(String s) {
            return s == null ? null : s.toLowerCase();
        }

        @Override
        public String toString() {
            return "lower";
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && getClass().equals(obj.getClass());
        }

    }

    private static class UniformStringTransform extends AbstractStringTransform {

        public UniformStringTransform() {
        }

        @Override
        public String transform(String s) {
            s=normalizeString(s);
            StringBuilder b = new StringBuilder();
            if (s != null) {
                boolean wasWhite = false;
                for (char c : s.toCharArray()) {
                    if (Character.isWhitespace(c)) {
                        if (!wasWhite) {
                            b.append(" ");
                        }
                        wasWhite = true;
                    } else {
                        wasWhite = false;
                        c = Character.toLowerCase(c);
                        switch (c) {
                            case 'è':
                            case 'ê':
                            case 'é':
                            case 'ë': {
                                b.append('e');
                                break;
                            }
                            case 'à':
                            case 'â':
                            case 'ä': {
                                b.append('a');
                                break;
                            }
                            case 'ù':
                            case 'û':
                            case 'ü': {
                                b.append('u');
                                break;
                            }
                            case 'ì': //i grave
                            case 'î': //i lfex
                            case 'ï': //i trema
                            {
                                b.append('i');
                                break;
                            }
                            default: {
                                b.append(c);
                            }
                        }
                    }
                }
            }
            return b.toString().trim();
        }

        @Override
        public String toString() {
            return "uniform";
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && getClass().equals(obj.getClass());
        }
    }

    private static class TrimStringTransform extends AbstractStringTransform {

        public TrimStringTransform() {
        }

        @Override
        public String transform(String s) {
            return s == null ? null : s.trim();
        }

        @Override
        public String toString() {
            return "trim";
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && getClass().equals(obj.getClass());
        }
    }

    private static class NotNullStringTransform extends AbstractStringTransform {

        public NotNullStringTransform() {
        }

        @Override
        public String transform(String s) {
            return s == null ? "" : s;
        }

        @Override
        public String toString() {
            return "notnull";
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && getClass().equals(obj.getClass());
        }
    }
}
