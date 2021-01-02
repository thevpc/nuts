package net.thevpc.nuts;

/**
 * @category Format
 */
public class NutsTextNodeStyle {
    private NutsTextNodeStyleType type;
    private int variant;

    public NutsTextNodeStyle(NutsTextNodeStyleType type, int variant) {
        this.type = type;
        this.variant = variant;
    }

    public static NutsTextNodeStyle of(NutsTextNodeStyleType style) {
        return of(style, 0);
    }

    public static NutsTextNodeStyle of(NutsTextNodeStyleType style, int variant) {
        return new NutsTextNodeStyle(style, variant);
    }

    public static NutsTextNodeStyle primary(int variant) {
        return of(NutsTextNodeStyleType.PRIMARY, variant);
    }

    public static NutsTextNodeStyle fail(int variant) {
        return of(NutsTextNodeStyleType.FAIL, variant);
    }

    public static NutsTextNodeStyle fail() {
        return of(NutsTextNodeStyleType.FAIL);
    }

    public static NutsTextNodeStyle danger(int variant) {
        return of(NutsTextNodeStyleType.DANGER, variant);
    }

    public static NutsTextNodeStyle danger() {
        return of(NutsTextNodeStyleType.DANGER);
    }

    public static NutsTextNodeStyle title(int variant) {
        return of(NutsTextNodeStyleType.TITLE, variant);
    }

    public static NutsTextNodeStyle secondary(int variant) {
        return of(NutsTextNodeStyleType.SECONDARY, variant);
    }

    public static NutsTextNodeStyle error() {
        return of(NutsTextNodeStyleType.ERROR);
    }

    public static NutsTextNodeStyle error(int variant) {
        return of(NutsTextNodeStyleType.ERROR, variant);
    }

    public static NutsTextNodeStyle option() {
        return of(NutsTextNodeStyleType.OPTION);
    }

    public static NutsTextNodeStyle option(int variant) {
        return of(NutsTextNodeStyleType.OPTION, variant);
    }

    public static NutsTextNodeStyle separator() {
        return of(NutsTextNodeStyleType.SEPARATOR);
    }

    public static NutsTextNodeStyle separator(int variant) {
        return of(NutsTextNodeStyleType.SEPARATOR, variant);
    }

    public static NutsTextNodeStyle version() {
        return of(NutsTextNodeStyleType.VERSION);
    }

    public static NutsTextNodeStyle version(int variant) {
        return of(NutsTextNodeStyleType.VERSION, variant);
    }

    public static NutsTextNodeStyle keyword() {
        return of(NutsTextNodeStyleType.KEYWORD);
    }

    public static NutsTextNodeStyle keyword(int variant) {
        return of(NutsTextNodeStyleType.KEYWORD, variant);
    }

    public static NutsTextNodeStyle reversed() {
        return of(NutsTextNodeStyleType.REVERSED);
    }

    public static NutsTextNodeStyle underlined() {
        return of(NutsTextNodeStyleType.UNDERLINED);
    }

    public static NutsTextNodeStyle striked() {
        return of(NutsTextNodeStyleType.STRIKED);
    }

    public static NutsTextNodeStyle italic() {
        return of(NutsTextNodeStyleType.ITALIC);
    }

    public static NutsTextNodeStyle bold() {
        return of(NutsTextNodeStyleType.BOLD);
    }

    public static NutsTextNodeStyle bool() {
        return of(NutsTextNodeStyleType.BOOLEAN);
    }

    public static NutsTextNodeStyle bool(int variant) {
        return of(NutsTextNodeStyleType.BOOLEAN, variant);
    }

    public static NutsTextNodeStyle blink() {
        return of(NutsTextNodeStyleType.BLINK);
    }

    public static NutsTextNodeStyle pale() {
        return of(NutsTextNodeStyleType.PALE);
    }

    public static NutsTextNodeStyle success() {
        return of(NutsTextNodeStyleType.SUCCESS);
    }

    public static NutsTextNodeStyle success(int variant) {
        return of(NutsTextNodeStyleType.SUCCESS, variant);
    }

    public static NutsTextNodeStyle path() {
        return of(NutsTextNodeStyleType.PATH);
    }

    public static NutsTextNodeStyle path(int variant) {
        return of(NutsTextNodeStyleType.PATH, variant);
    }

    public static NutsTextNodeStyle warn() {
        return of(NutsTextNodeStyleType.WARN);
    }

    public static NutsTextNodeStyle warn(int variant) {
        return of(NutsTextNodeStyleType.WARN, variant);
    }

    public static NutsTextNodeStyle config() {
        return of(NutsTextNodeStyleType.CONFIG);
    }

    public static NutsTextNodeStyle config(int variant) {
        return of(NutsTextNodeStyleType.CONFIG, variant);
    }

    public static NutsTextNodeStyle info() {
        return of(NutsTextNodeStyleType.INFO);
    }

    public static NutsTextNodeStyle info(int variant) {
        return of(NutsTextNodeStyleType.INFO, variant);
    }

    public static NutsTextNodeStyle string() {
        return of(NutsTextNodeStyleType.STRING);
    }

    public static NutsTextNodeStyle string(int variant) {
        return of(NutsTextNodeStyleType.STRING, variant);
    }

    public static NutsTextNodeStyle operator() {
        return of(NutsTextNodeStyleType.OPERATOR);
    }

    public static NutsTextNodeStyle operator(int variant) {
        return of(NutsTextNodeStyleType.OPERATOR, variant);
    }

    public static NutsTextNodeStyle userInput() {
        return of(NutsTextNodeStyleType.USER_INPUT);
    }

    public static NutsTextNodeStyle userInput(int variant) {
        return of(NutsTextNodeStyleType.USER_INPUT, variant);
    }

    public static NutsTextNodeStyle comments() {
        return of(NutsTextNodeStyleType.COMMENTS);
    }

    public static NutsTextNodeStyle comments(int variant) {
        return of(NutsTextNodeStyleType.COMMENTS, variant);
    }

    public static NutsTextNodeStyle variable() {
        return of(NutsTextNodeStyleType.VAR);
    }

    public static NutsTextNodeStyle variable(int variant) {
        return of(NutsTextNodeStyleType.VAR, variant);
    }

    public static NutsTextNodeStyle number() {
        return of(NutsTextNodeStyleType.VAR);
    }

    public static NutsTextNodeStyle number(int variant) {
        return of(NutsTextNodeStyleType.VAR, variant);
    }

    public static NutsTextNodeStyle foregroundColor(int variant) {
        return of(NutsTextNodeStyleType.FORE_COLOR, variant);
    }

    public static NutsTextNodeStyle foregroundTrueColor(int variant) {
        return of(NutsTextNodeStyleType.FORE_TRUE_COLOR, variant);
    }

    public static NutsTextNodeStyle backgroundColor(int variant) {
        return of(NutsTextNodeStyleType.BACK_COLOR, variant);
    }

    public static NutsTextNodeStyle backgroundTrueColor(int variant) {
        return of(NutsTextNodeStyleType.BACK_TRUE_COLOR, variant);
    }

    public NutsTextNodeStyleType getType() {
        return type;
    }

    public int getVariant() {
        return variant;
    }

}
