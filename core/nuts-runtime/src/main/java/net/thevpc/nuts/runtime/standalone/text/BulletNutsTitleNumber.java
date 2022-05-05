package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.NutsTitleNumber;

public class BulletNutsTitleNumber implements NutsTitleNumber {
    private final char bulletChar;
    private final boolean empty;

    public BulletNutsTitleNumber(char bulletChar) {
        this(bulletChar, false);
    }

    public BulletNutsTitleNumber(char bulletChar, boolean empty) {
        this.bulletChar = bulletChar;
        this.empty = empty;
    }

    public static NutsTitleNumber ofBullet(char bulletChar) {
        return new BulletNutsTitleNumber(bulletChar,true);
    }

    @Override
    public NutsTitleNumber next() {
        if(isNone()){
            return new BulletNutsTitleNumber(bulletChar);
        }
        return this;
    }

    @Override
    public NutsTitleNumber first() {
        if(isNone()){
            return new BulletNutsTitleNumber(bulletChar);
        }
        return this;
    }

    @Override
    public NutsTitleNumber none() {
        if (isNone()) {
            return this;
        }
        return new BulletNutsTitleNumber(bulletChar);
    }

    @Override
    public boolean isNone() {
        return empty;
    }

    @Override
    public String toString() {
        if (isNone()) {
            return "";
        }
        return String.valueOf(bulletChar);
    }

    @Override
    public boolean isBullet() {
        return true;
    }
}
