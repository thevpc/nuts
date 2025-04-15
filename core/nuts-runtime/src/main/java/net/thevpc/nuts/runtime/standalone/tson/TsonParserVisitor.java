package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonParserVisitor {
    default void visitComments(TsonComment comments) {
    }

    default void visitAnnotationStart(String annotationName) {
    }

    default void visitPrimitiveEnd(TsonElement primitiveElement) {
    }

    default void visitObjectStart() {
    }

    default void visitArrayStart() {
    }

    default void visitNamedStart(String id) {
    }

    default void visitParamsStart() {
    }

    default void visitUpletEnd() {
    }

    default void visitFunctionEnd() {
    }

    default void visitParamsEnd() {
    }

    default void visitObjectEnd() {

    }

    default void visitBinOpEnd(String op) {

    }

    default void visitKeyValueEnd() {

    }

    default void visitElementStart() {

    }

    default void visitDocumentEnd() {

    }

    default void visitAnnotationEnd() {

    }

    default void visitNamedObjectEnd() {

    }

    default void visitNamedArrayEnd() {

    }


    default void visitAnnotationParamsStart() {
    }
    default void visitAnnotationParamsEnd() {
    }
    default void visitAnnotationParamStart() {
    }

    default void visitAnnotationParamEnd() {
    }

    default void visitParamElementStart() {
    }

    default void visitParamElementEnd() {
    }

    default void visitObjectElementStart() {
    }

    default void visitObjectElementEnd() {
    }

    default void visitArrayElementStart() {
    }

    default void visitArrayElementEnd() {
    }

    default void visitInstructionStart() {
    }

    default void visitSimpleEnd() {
    }

    default void visitArrayEnd(){

    }

    default void visitNamedObjectStart(){

    }
    default void visitNamedArrayStart(){

    }

    default void listSeparator(){

    }

    default void matrixRowSeparator(){

    }

    default void visitCustomEnd(TsonCustom custom) {
    }

}
