package net.vpc.app.nuts.extensions.util;

public class JsonStatus {

    public int countBraces;
    public int openBraces;
    public int openBrackets;
    public boolean openAntiSlash;
    public boolean openSimpleQuotes;
    public boolean openDoubleQuotes;

    public boolean checkValid(boolean throwError) {
        if (!checkPartialValid(throwError)) {
            return false;
        }
        if (countBraces == 0) {
            if (throwError) {
                throw new RuntimeException("not an object");
            }
            return false;
        }
        if (openBrackets > 0) {
            if (throwError) {
                throw new RuntimeException("Unbalanced brackets");
            }
            return false;
        }
        if (openBraces > 0) {
            if (throwError) {
                throw new RuntimeException("Unbalanced braces");
            }
            return false;
        }
        if (openAntiSlash) {
            if (throwError) {
                throw new RuntimeException("Unbalanced anti-slash");
            }
        }
        if (openSimpleQuotes) {
            if (throwError) {
                throw new RuntimeException("Unbalanced simple quotes");
            }
            return false;
        }
        if (openDoubleQuotes) {
            if (throwError) {
                throw new RuntimeException("Unbalanced double quotes");
            }
            return false;
        }
        return true;
    }

    public boolean checkPartialValid(boolean throwError) {
        if (openBrackets < 0) {
            if (throwError) {
                throw new RuntimeException("Unbalanced brackets");
            }
            return false;
        }
        if (openBraces < 0) {
            if (throwError) {
                throw new RuntimeException("Unbalanced braces");
            }
            return false;
        }
        return true;
    }
}
