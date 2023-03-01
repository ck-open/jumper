package com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom;

import com.ck.qr_code.swetake.jp.sourceforge.qrcode.reader.QRCodeImageReader;

public class Axis {
    int sin;
    int cos;
    int modulePitch;
    Point origin;

    public Axis(int[] angle, int modulePitch) {
        this.sin = angle[0];
        this.cos = angle[1];
        this.modulePitch = modulePitch;
        this.origin = new Point();
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
    }

    public void setModulePitch(int modulePitch) {
        this.modulePitch = modulePitch;
    }

    public Point translate(Point offset) {
        int moveX = offset.getX();
        int moveY = offset.getY();
        return this.translate(moveX, moveY);
    }

    public Point translate(Point origin, Point offset) {
        this.setOrigin(origin);
        int moveX = offset.getX();
        int moveY = offset.getY();
        return this.translate(moveX, moveY);
    }

    public Point translate(Point origin, int moveX, int moveY) {
        this.setOrigin(origin);
        return this.translate(moveX, moveY);
    }

    public Point translate(Point origin, int modulePitch, int moveX, int moveY) {
        this.setOrigin(origin);
        this.modulePitch = modulePitch;
        return this.translate(moveX, moveY);
    }

    public Point translate(int moveX, int moveY) {
        long dp = (long) QRCodeImageReader.DECIMAL_POINT;
        Point point = new Point();
        int dx = moveX == 0 ? 0 : this.modulePitch * moveX >> (int)dp;
        int dy = moveY == 0 ? 0 : this.modulePitch * moveY >> (int)dp;
        point.translate(dx * this.cos - dy * this.sin >> (int)dp, dx * this.sin + dy * this.cos >> (int)dp);
        point.translate(this.origin.getX(), this.origin.getY());
        return point;
    }
}
