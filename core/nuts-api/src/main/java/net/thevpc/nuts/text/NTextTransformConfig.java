/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.io.NPath;

import java.util.Objects;
import java.util.function.Function;

/**
 * @app.category Format
 */
public class NTextTransformConfig implements Cloneable, NBlankable {

    /**
     * when true all styles are removed, transform any node to PLAIN, or LIST only
     */
    private boolean filtered;

    /**
     * when true, every node is split over newlines. Newlines will remain as separate plain tokens
     */
    private boolean flatten;

    /**
     * when true, transform any node to PLAIN, STYLED or LIST (to basic)
     */
    private boolean normalize;

    /**
     * when true all styles are transformed to plain colors
     */
    private boolean applyTheme;
    
    private boolean basicTrueStyles;

    /**
     * when true any title will be prefixed with an incremental number according to {@code titleNumberSequence}
     */
    private boolean processTitleNumbers;

    /**
     * sequence to use when {@code processTitleNumbers==true}
     */
    private NTitleSequence titleNumberSequence;

    /**
     * when true, include commands are processed, resources are loaded to replace the given command
     */
    private boolean processIncludes;
    /**
     * current directory to load from included files
     */
    private NPath currentDir;

    /**
     * when true, replace all {@code "${varName}" } literal by evaluating  {@code varProvider.apply("${varName}") }.
     * default vars are available
     */
    private boolean processVars;
    private Function<String, String> varProvider;
    private String anchor;
    /**
     * when provided, ensure that to root level of the document is translated to {@code rootLevel}
     */
    private Integer rootLevel;

    private String themeName;

    /**
     * when provided, try to use this class loader for 'classpath:' resources
     */
    private ClassLoader importClassLoader;

    /**
     * Theme name.
     *
     * @return theme name result
     */
    public String themeName() {
        return themeName;
    }

    /**
     * Theme name.
     *
     * @param themeName theme name
     * @return theme name result
     */
    public NTextTransformConfig themeName(String themeName) {
        this.themeName = themeName;
        return this;
    }

    /**
     * Checks if is basic true styles.
     *
     * @return is basic true styles result
     */
    public boolean isBasicTrueStyles() {
        return basicTrueStyles;
    }

    /**
     * Basic true styles.
     *
     * @param basicTrueStyles basic true styles
     * @return basic true styles result
     */
    public NTextTransformConfig basicTrueStyles(boolean basicTrueStyles) {
        this.basicTrueStyles = basicTrueStyles;
        return this;
    }

    /**
     * Checks if is apply theme.
     *
     * @return is apply theme result
     */
    public boolean isApplyTheme() {
        return applyTheme;
    }

    /**
     * Apply theme.
     *
     * @param applyTheme apply theme
     * @return apply theme result
     */
    public NTextTransformConfig applyTheme(boolean applyTheme) {
        this.applyTheme = applyTheme;
        return this;
    }

    /**
     * Checks if is process title numbers.
     *
     * @return is process title numbers result
     */
    public boolean isProcessTitleNumbers() {
        return processTitleNumbers;
    }

    /**
     * Process title numbers.
     *
     * @param processTitleNumbers process title numbers
     * @return process title numbers result
     */
    public NTextTransformConfig processTitleNumbers(boolean processTitleNumbers) {
        this.processTitleNumbers = processTitleNumbers;
        return this;
    }

    /**
     * Title number sequence.
     *
     * @return title number sequence result
     */
    public NTitleSequence titleNumberSequence() {
        return titleNumberSequence;
    }

    /**
     * Title number sequence.
     *
     * @param titleNumberSequence title number sequence
     * @return title number sequence result
     */
    public NTextTransformConfig titleNumberSequence(NTitleSequence titleNumberSequence) {
        this.titleNumberSequence = titleNumberSequence;
        return this;
    }

    /**
     * Checks if is filtered.
     *
     * @return is filtered result
     */
    public boolean isFiltered() {
        return filtered;
    }

    /**
     * Filtered.
     *
     * @param filtered filtered
     * @return filtered result
     */
    public NTextTransformConfig filtered(boolean filtered) {
        this.filtered = filtered;
        return this;
    }

    /**
     * Copy.
     *
     * @return copy result
     */
    public NTextTransformConfig copy() {
        /**
         * Clone.
         *
         * @return clone result
         */
        return clone();
    }

    @Override
    protected NTextTransformConfig clone() {
        try {
          /**
           * Return.
           *
           * @param super.clone( super.clone(
           */
            return (NTextTransformConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            /**
             * Illegal argument exception.
             *
             * @param e e
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Checks if is process includes.
     *
     * @return is process includes result
     */
    public boolean isProcessIncludes() {
        return processIncludes;
    }

    /**
     * Process includes.
     *
     * @param processIncludes process includes
     * @return process includes result
     */
    public NTextTransformConfig processIncludes(boolean processIncludes) {
        this.processIncludes = processIncludes;
        return this;
    }


    /**
     * Var provider.
     *
     * @return var provider result
     */
    public Function<String, String> varProvider() {
        return varProvider;
    }

    /**
     * Checks if is process vars.
     *
     * @return is process vars result
     */
    public boolean isProcessVars() {
        return processVars;
    }

    /**
     * Process vars.
     *
     * @param processVars process vars
     * @return process vars result
     */
    public NTextTransformConfig processVars(boolean processVars) {
        this.processVars = processVars;
        return this;
    }

    /**
     * Var provider.
     *
     * @param varProvider var provider
     * @return var provider result
     */
    public NTextTransformConfig varProvider(Function<String, String> varProvider) {
        this.varProvider = varProvider;
        return this;
    }

    /**
     * Anchor.
     *
     * @return anchor result
     */
    public String anchor() {
        return anchor;
    }

    /**
     * Anchor.
     *
     * @param anchor anchor
     * @return anchor result
     */
    public NTextTransformConfig anchor(String anchor) {
        this.anchor = anchor;
        return this;
    }

    /**
     * Process all.
     *
     * @param enable enable
     * @return process all result
     */
    public NTextTransformConfig processAll(boolean enable) {
      /**
       * Process title numbers.
       *
       * @param enable enable
       */
        processTitleNumbers(enable);
      /**
       * Process vars.
       *
       * @param enable enable
       */
        processVars(enable);
      /**
       * Process includes.
       *
       * @param enable enable
       */
        processIncludes(enable);
        return this;
    }

    /**
     * Root level.
     *
     * @return root level result
     */
    public Integer rootLevel() {
        return rootLevel;
    }

    /**
     * Root level.
     *
     * @param rootLevel root level
     * @return root level result
     */
    public NTextTransformConfig rootLevel(Integer rootLevel) {
        this.rootLevel = rootLevel;
        return this;
    }

    /**
     * Checks if is blank.
     *
     * @return is blank result
     */
    public boolean isBlank() {
        return !filtered
                && !flatten
                && !normalize
                && !processTitleNumbers
                && !processIncludes
                && !processVars
                && !applyTheme
                && titleNumberSequence == null
                && currentDir == null
                && varProvider == null
                && NBlankable.isBlank(anchor)
                && rootLevel == null
                && importClassLoader == null;
    }


    /**
     * Current dir.
     *
     * @return current dir result
     */
    public NPath currentDir() {
        return currentDir;
    }

    /**
     * Current dir.
     *
     * @param currentDir current dir
     * @return current dir result
     */
    public NTextTransformConfig currentDir(NPath currentDir) {
        this.currentDir = currentDir;
        return this;
    }


    /**
     * Checks if is flatten.
     *
     * @return is flatten result
     */
    public boolean isFlatten() {
        return flatten;
    }

    /**
     * Flatten.
     *
     * @param flatten flatten
     * @return flatten result
     */
    public NTextTransformConfig flatten(boolean flatten) {
        this.flatten = flatten;
        return this;
    }

    /**
     * Checks if is normalize.
     *
     * @return is normalize result
     */
    public boolean isNormalize() {
        return normalize;
    }

    /**
     * Normalize.
     *
     * @param normalize normalize
     * @return normalize result
     */
    public NTextTransformConfig normalize(boolean normalize) {
        this.normalize = normalize;
        return this;
    }

    /**
     * Import class loader.
     *
     * @return import class loader result
     */
    public ClassLoader importClassLoader() {
        return importClassLoader;
    }

    /**
     * Import class loader.
     *
     * @param importClassLoader import class loader
     * @return import class loader result
     */
    public NTextTransformConfig importClassLoader(ClassLoader importClassLoader) {
        this.importClassLoader = importClassLoader;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NTextTransformConfig that = (NTextTransformConfig) o;
        return filtered == that.filtered && flatten == that.flatten && normalize == that.normalize && applyTheme == that.applyTheme && basicTrueStyles == that.basicTrueStyles && processTitleNumbers == that.processTitleNumbers && processIncludes == that.processIncludes && processVars == that.processVars && Objects.equals(titleNumberSequence, that.titleNumberSequence) && Objects.equals(currentDir, that.currentDir) && Objects.equals(varProvider, that.varProvider) && Objects.equals(anchor, that.anchor) && Objects.equals(rootLevel, that.rootLevel) && Objects.equals(themeName, that.themeName) && Objects.equals(importClassLoader, that.importClassLoader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filtered, flatten, normalize, applyTheme, basicTrueStyles, processTitleNumbers, titleNumberSequence, processIncludes, currentDir, processVars, varProvider, anchor, rootLevel, themeName, importClassLoader);
    }
}
