/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd.cmdline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import net.vpc.app.nuts.ArgumentCandidate;
import net.vpc.app.nuts.NutsCommandAutoComplete;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

/**
 * Created by vpc on 12/7/16.
 */
public class CmdLine {

    private List<String> args;
    private int wordIndex = 0;
    private NutsCommandAutoComplete autoComplete;
    private HashSet<String> visitedSequences = new HashSet<>();

    public CmdLine(NutsCommandAutoComplete autoComplete, String[] args) {
        this.args = new ArrayList<>(Arrays.asList(args));
        this.autoComplete = autoComplete;
    }

    public boolean isExecMode(){
        return autoComplete==null;
    }

    public boolean isAutoCompleteMode(){
        return autoComplete!=null;
    }

//    public CmdLine skip(int count){
//        int min = Math.min(0, args.size()-count);
//        if(min<=0){
//            return this;
//        }
//        String[] args2=new String[min];
//        System.arraycopy(args,min,args2,0,args.length);
//        return new CmdLine(args2);
//    }
    public boolean isOption(int index) {
        return (index < args.size() && args.get(index).startsWith("-"));
    }

    public boolean isOption() {
        return isOption(0);
    }

//    public Val tryRemoveNonOptionOrError(String name, String defaultValue) {
//        if (args.size() > 0 && !isOption()) {
//            String r = args.get(0);
//            readHead(1);
//            return new Val(r);
//        } else {
//            return new Val(defaultValue);
//        }
//    }

    public Val readOptionOrError(String name) {
        if (args.size() > 0 && isOption()) {
            String r = args.get(0);
            readHead(1);
            return new Val(r);
        } else {
            throw new IllegalArgumentException("Missing argument " + name);
        }
    }

    public Val readNonOptionOrError(NonOption name) {
        return readNonOption(name,true);
    }

    public Val readNonOption(NonOption name) {
        return readNonOption(name,false);
    }

    public Val readNonOption(NonOption name, boolean error) {
        if (args.size() > 0 && !isOption()) {
            if (isAutoComplete()) {
                List<ArgumentCandidate> values = name.getValues();
                if (values == null || values.isEmpty()) {
                    autoComplete.addExpectedTypedValue(null, name.getName());
                } else {
                    for (ArgumentCandidate value : name.getValues()) {
                        autoComplete.addCandidate(value);
                    }
                }
            }
            String r = args.get(0);
            readHead(1);
            return new Val(r);
        } else {
            if (autoComplete!=null) {
                if (isAutoComplete()) {
                    List<ArgumentCandidate> values = name.getValues();
                    if (values == null || values.isEmpty()) {
                        autoComplete.addExpectedTypedValue(null, name.getName());
                    } else {
                        for (ArgumentCandidate value : name.getValues()) {
                            autoComplete.addCandidate(value);
                        }
                    }
                }
                return new Val("");
            }
            if(error){
                return new Val("");
            }
            throw new IllegalArgumentException("Missing argument " + name);
        }
    }

    public Val readHead() {
        Val val = getVal(0);
        readHead(1);
        return val;
    }

    public void readHead(int count) {
        for (int i = 0; i < count; i++) {
            args.remove(0);
            wordIndex++;
        }
    }

    private boolean isAutoCompleteContext() {
        return autoComplete != null;
    }

    private boolean isAutoComplete() {
        if (autoComplete != null && getWordIndex() == autoComplete.getCurrentWordIndex()) {
            return true;
        }
        return false;
    }

    public boolean acceptAndRemove(boolean acceptDuplicates, String... vals) {
        String[][] vals2 = new String[vals.length][];
        for (int i = 0; i < vals2.length; i++) {
            List<String> split = CoreStringUtils.split(vals[i], " ");
            vals2[i] = split.toArray(new String[split.size()]);
        }
        return acceptAndRemove(acceptDuplicates, vals2);
    }

    public boolean acceptAndRemove(String... vals) {
        return acceptAndRemove(true, vals);
    }

    public boolean acceptAndRemoveNoDuplicates(String... vals) {
        return acceptAndRemove(false, vals);
    }

//    public boolean acceptAndRemove(String[]... vals) {
//        return acceptAndRemove(true, vals);
//    }
//
//    public boolean acceptAndRemoveNoDuplicates(String[]... vals) {
//        return acceptAndRemove(false, vals);
//    }

    private boolean acceptAndRemove(boolean acceptDuplicates, String[]... vals) {
        if (autoComplete != null) {
            for (String[] val : vals) {
                if ((acceptDuplicates || !isVisitedSequence(val))) {
                    if (acceptSequence(0, val)) {
                        setVisitedSequence(val);
                        readHead(val.length);
                        return true;
                    } else {
                        setVisitedSequence(val);
                        for (int i = 0; i < val.length; i++) {
                            String v = val[i];
                            if (getWordIndex() + i == autoComplete.getCurrentWordIndex()) {
                                autoComplete.addCandidate(new DefaultArgumentCandidate(v));
                            } else if (!getVal(i).getString("").equals(v)) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        for (String[] val : vals) {
            if ((acceptDuplicates || !isVisitedSequence(val)) && acceptSequence(0, val)) {
                setVisitedSequence(val);
                readHead(val.length);
                return true;
            }
        }
        return false;
    }

    public boolean acceptSequence(int pos, String... vals) {
        for (int i = 0; i < vals.length; i++) {
            if (!getVal(pos + i).getString("").equals(vals[i])) {
                return false;
            }
        }
        return true;
    }

    public String getOptionValueNonEmpty(String... option) {
        for (String anOption : option) {
            String s = getOptionValueNonEmpty(anOption);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    public Val getOptionVal(String option) {
        int index = findOption(option);
        if (index >= 0) {
            return getVal(index + 1);
        }
        return new Val(null);
    }

    public String getOptionValueNonEmpty(String option) {
        int index = findOption(option);
        if (index >= 0) {
            return getNonEmpty(index + 1);
        }
        return null;
    }

    public String getOptionValue(String option) {
        int index = findOption(option);
        if (index >= 0) {
            return get(index + 1);
        }
        return null;
    }

    public Val getVal(int i) {
        String s = get(i);
        return new Val(s);
    }

    public String getNonEmpty(int i) {
        String s = get(i);
        if (s != null) {
            if (s.length() == 0) {
                return null;
            }
        }
        return s;
    }

    public String get(int i) {
        if (i >= 0 && i < args.size()) {
            return args.get(i);
        }
        return null;
    }

    public boolean containOption(String name) {
        return findOption(name) >= 0;
    }

    public int findOption(String name) {
        if (name.startsWith("-") || name.startsWith("--")) {
            for (int i = 0; i < args.size(); i++) {
                if (args.get(i).equals(name)) {
                    return i;
                }
            }
        } else {
            throw new IllegalArgumentException("Not an option " + name);
        }
        return -1;
    }

    public int length() {
        return args.size();
    }

    public void requireEmpty() {
        if (!isEmpty()) {
            if (autoComplete!=null) {
                args.clear();
                return;
            }
            throw new IllegalArgumentException("Too Many arguments");
        }
    }

    public void requireNonEmpty() {
        if (isEmpty()) {
            if (autoComplete!=null) {
                args.clear();
                return;
            }
            throw new IllegalArgumentException("Missing Arguments");
        }
    }

    public boolean isEmpty() {
        return args.isEmpty();
    }

    public String[] toArray() {
        return args.toArray(new String[args.size()]);
    }

    @Override
    public String toString() {
        return "CmdLine{"
                + (args)
                + '}';
    }

    public static class Val {

        private String value;

        public boolean isOption() {
            return value.startsWith("-");
        }

        boolean isNull() {
            return value == null;
        }

        public Val(String value) {
            this.value = value;
        }

        public String getString() {
            return value;
        }

        public String getString(String s) {
            return value == null ? s : value;
        }

        public boolean isAny(String... any) {
            for (String s : any) {
                if (s == null) {
                    if (value == null) {
                        return true;
                    }
                } else if (s.equals(value)) {
                    return true;
                }
            }
            return false;
        }

        public String getStringOrError() {
            if (value == null) {
                throw new RuntimeException("Not Found");
            }
            return value;
        }

        public int getInt(int value) {
            try {
                return Integer.parseInt(getString(String.valueOf(value)));
            } catch (NumberFormatException e) {
                return value;
            }
        }

        public boolean getBoolean() {
            return getBoolean(false);
        }

        public boolean getBoolean(boolean value) {
            try {
                return Boolean.parseBoolean(getString(String.valueOf(value)));
            } catch (NumberFormatException e) {
                return value;
            }
        }

        public int getIntOrError() {
            return Integer.parseInt(getStringOrError());
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public int getWordIndex() {
        return wordIndex;
    }

    private boolean isVisitedSequence(String[] aaa) {
        return visitedSequences.contains(flattenSequence(aaa));
    }

    private boolean setVisitedSequence(String[] aaa) {
        return visitedSequences.add(flattenSequence(aaa));
    }

    private String flattenSequence(String[] aaa) {
        StringBuilder sb = new StringBuilder();
        sb.append(aaa[0]);
        for (int i = 1; i < aaa.length; i++) {
            sb.append("\n").append(aaa[i]);
        }
        return sb.toString();
    }

    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

}
