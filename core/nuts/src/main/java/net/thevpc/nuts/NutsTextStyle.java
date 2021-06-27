package net.thevpc.nuts;

/**
 * @category Format
 */
public class NutsTextStyle {
    private NutsTextStyleType type;
    private int variant;

    public NutsTextStyle(NutsTextStyleType type, int variant) {
        this.type = type;
        this.variant = variant;
    }

    public static NutsTextStyle of(NutsTextStyleType style) {
        return of(style, 0);
    }

    public static NutsTextStyle of(NutsTextStyleType style, int variant) {
        return new NutsTextStyle(style, variant);
    }

    public static NutsTextStyle primary1() {
        return primary(1);
    }
    public static NutsTextStyle primary2() {
        return primary(2);
    }
    public static NutsTextStyle primary3() {
        return primary(3);
    }
    public static NutsTextStyle primary4() {
        return primary(4);
    }
    public static NutsTextStyle primary5() {
        return primary(5);
    }
    public static NutsTextStyle primary6() {
        return primary(6);
    }
    public static NutsTextStyle primary7() {
        return primary(7);
    }
    public static NutsTextStyle primary8() {
        return primary(8);
    }
    public static NutsTextStyle primary9() {
        return primary(9);
    }
    public static NutsTextStyle primary(int variant) {
        return of(NutsTextStyleType.PRIMARY, variant);
    }

    public static NutsTextStyle fail(int variant) {
        return of(NutsTextStyleType.FAIL, variant);
    }

    public static NutsTextStyle fail() {
        return of(NutsTextStyleType.FAIL);
    }

    public static NutsTextStyle danger(int variant) {
        return of(NutsTextStyleType.DANGER, variant);
    }

    public static NutsTextStyle danger() {
        return of(NutsTextStyleType.DANGER);
    }

    public static NutsTextStyle title(int variant) {
        return of(NutsTextStyleType.TITLE, variant);
    }

    public static NutsTextStyle secondary(int variant) {
        return of(NutsTextStyleType.SECONDARY, variant);
    }

    public static NutsTextStyle error() {
        return of(NutsTextStyleType.ERROR);
    }

    public static NutsTextStyle error(int variant) {
        return of(NutsTextStyleType.ERROR, variant);
    }

    public static NutsTextStyle option() {
        return of(NutsTextStyleType.OPTION);
    }

    public static NutsTextStyle option(int variant) {
        return of(NutsTextStyleType.OPTION, variant);
    }

    public static NutsTextStyle separator() {
        return of(NutsTextStyleType.SEPARATOR);
    }

    public static NutsTextStyle separator(int variant) {
        return of(NutsTextStyleType.SEPARATOR, variant);
    }

    public static NutsTextStyle version() {
        return of(NutsTextStyleType.VERSION);
    }

    public static NutsTextStyle version(int variant) {
        return of(NutsTextStyleType.VERSION, variant);
    }

    public static NutsTextStyle keyword() {
        return of(NutsTextStyleType.KEYWORD);
    }

    public static NutsTextStyle keyword(int variant) {
        return of(NutsTextStyleType.KEYWORD, variant);
    }

    public static NutsTextStyle reversed() {
        return of(NutsTextStyleType.REVERSED);
    }

    public static NutsTextStyle reversed(int variant) {
        return of(NutsTextStyleType.REVERSED,variant);
    }

    public static NutsTextStyle underlined() {
        return of(NutsTextStyleType.UNDERLINED);
    }

    public static NutsTextStyle striked() {
        return of(NutsTextStyleType.STRIKED);
    }

    public static NutsTextStyle striked(int variant) {
        return of(NutsTextStyleType.STRIKED,variant);
    }

    public static NutsTextStyle italic() {
        return of(NutsTextStyleType.ITALIC);
    }
    public static NutsTextStyle italic(int variant) {
        return of(NutsTextStyleType.ITALIC,variant);
    }

    public static NutsTextStyle bold() {
        return of(NutsTextStyleType.BOLD);
    }

    public static NutsTextStyle bool() {
        return of(NutsTextStyleType.BOOLEAN);
    }

    public static NutsTextStyle bool(int variant) {
        return of(NutsTextStyleType.BOOLEAN, variant);
    }

    public static NutsTextStyle blink() {
        return of(NutsTextStyleType.BLINK);
    }

    public static NutsTextStyle pale() {
        return of(NutsTextStyleType.PALE);
    }
    public static NutsTextStyle pale(int variant) {
        return of(NutsTextStyleType.PALE,variant);
    }

    public static NutsTextStyle success() {
        return of(NutsTextStyleType.SUCCESS);
    }

    public static NutsTextStyle success(int variant) {
        return of(NutsTextStyleType.SUCCESS, variant);
    }

    public static NutsTextStyle path() {
        return of(NutsTextStyleType.PATH);
    }

    public static NutsTextStyle path(int variant) {
        return of(NutsTextStyleType.PATH, variant);
    }

    public static NutsTextStyle warn() {
        return of(NutsTextStyleType.WARN);
    }

    public static NutsTextStyle warn(int variant) {
        return of(NutsTextStyleType.WARN, variant);
    }

    public static NutsTextStyle config() {
        return of(NutsTextStyleType.CONFIG);
    }

    public static NutsTextStyle config(int variant) {
        return of(NutsTextStyleType.CONFIG, variant);
    }

    public static NutsTextStyle info() {
        return of(NutsTextStyleType.INFO);
    }

    public static NutsTextStyle info(int variant) {
        return of(NutsTextStyleType.INFO, variant);
    }

    public static NutsTextStyle string() {
        return of(NutsTextStyleType.STRING);
    }

    public static NutsTextStyle string(int variant) {
        return of(NutsTextStyleType.STRING, variant);
    }

    public static NutsTextStyle operator() {
        return of(NutsTextStyleType.OPERATOR);
    }

    public static NutsTextStyle operator(int variant) {
        return of(NutsTextStyleType.OPERATOR, variant);
    }

    public static NutsTextStyle input() {
        return of(NutsTextStyleType.INPUT);
    }

    public static NutsTextStyle input(int variant) {
        return of(NutsTextStyleType.INPUT, variant);
    }

    public static NutsTextStyle comments() {
        return of(NutsTextStyleType.COMMENTS);
    }

    public static NutsTextStyle comments(int variant) {
        return of(NutsTextStyleType.COMMENTS, variant);
    }

    public static NutsTextStyle variable() {
        return of(NutsTextStyleType.VAR);
    }

    public static NutsTextStyle variable(int variant) {
        return of(NutsTextStyleType.VAR, variant);
    }

    public static NutsTextStyle number() {
        return of(NutsTextStyleType.NUMBER);
    }

    public static NutsTextStyle date() {
        return of(NutsTextStyleType.DATE);
    }

    public static NutsTextStyle date(int variant) {
        return of(NutsTextStyleType.DATE,variant);
    }

    public static NutsTextStyle number(int variant) {
        return of(NutsTextStyleType.VAR, variant);
    }

    public static NutsTextStyle foregroundColor(int variant) {
        return of(NutsTextStyleType.FORE_COLOR, variant);
    }

    public static NutsTextStyle foregroundTrueColor(int variant) {
        return of(NutsTextStyleType.FORE_TRUE_COLOR, variant);
    }

    public static NutsTextStyle backgroundColor(int variant) {
        return of(NutsTextStyleType.BACK_COLOR, variant);
    }

    public static NutsTextStyle backgroundTrueColor(int variant) {
        return of(NutsTextStyleType.BACK_TRUE_COLOR, variant);
    }

    public NutsTextStyles append(NutsTextStyle other) {
        return NutsTextStyles.of(this,other);
    }

    public NutsTextStyles append(NutsTextStyles other) {
        return NutsTextStyles.of(this).append(other);
    }

    public NutsTextStyleType getType() {
        return type;
    }

    public int getVariant() {
        return variant;
    }

    @Override
    public String toString() {
        return "NutsTextNodeStyle{" +
                "type=" + type +
                ", variant=" + variant +
                '}';
    }
}
