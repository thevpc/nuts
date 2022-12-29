package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.NTitleNumber;

public class BulletNTitleNumber implements NTitleNumber {
    private final char bulletChar;
    private final boolean empty;

    public BulletNTitleNumber(char bulletChar) {
        this(bulletChar, false);
    }

    public BulletNTitleNumber(char bulletChar, boolean empty) {
        this.bulletChar = bulletChar;
        this.empty = empty;
    }

    public static NTitleNumber ofBullet(char bulletChar) {
        return new BulletNTitleNumber(bulletChar,true);
    }

    @Override
    public NTitleNumber next() {
        if(isNone()){
            return new BulletNTitleNumber(bulletChar);
        }
        return this;
    }

    @Override
    public NTitleNumber first() {
        if(isNone()){
            return new BulletNTitleNumber(bulletChar);
        }
        return this;
    }

    @Override
    public NTitleNumber none() {
        if (isNone()) {
            return this;
        }
        return new BulletNTitleNumber(bulletChar);
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
