package com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom;


import com.ck.qr_code.swetake.jp.sourceforge.qrcode.util.QRCodeUtility;

public class Point {
    public static final int RIGHT = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT = 4;
    public static final int TOP = 8;
    int x;
    int y;

    public Point() {
        this.x = 0;
        this.y = 0;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + this.x + "," + this.y + ")";
    }

    public static Point getCenter(Point p1, Point p2) {
        return new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);
    }

    public boolean equals(Point compare) {
        return this.x == compare.x && this.y == compare.y;
    }

    public int distanceOf(Point other) {
        int x2 = other.getX();
        int y2 = other.getY();
        return QRCodeUtility.sqrt((this.x - x2) * (this.x - x2) + (this.y - y2) * (this.y - y2));
    }
}