package com.ck.qr.swetake.jp.sourceforge.qrcode.geom;

import com.ck.qr_code.swetake.jp.sourceforge.qrcode.util.QRCodeUtility;

public class Line {
    int x1;
    int y1;
    int x2;
    int y2;

    public Line() {
        this.x1 = this.y1 = this.x2 = this.y2 = 0;
    }

    public Line(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Line(Point p1, Point p2) {
        this.x1 = p1.getX();
        this.y1 = p1.getY();
        this.x2 = p2.getX();
        this.y2 = p2.getY();
    }

    public Point getP1() {
        return new Point(this.x1, this.y1);
    }

    public Point getP2() {
        return new Point(this.x2, this.y2);
    }

    public void setLine(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public void setP1(Point p1) {
        this.x1 = p1.getX();
        this.y1 = p1.getY();
    }

    public void setP1(int x1, int y1) {
        this.x1 = x1;
        this.y1 = y1;
    }

    public void setP2(Point p2) {
        this.x2 = p2.getX();
        this.y2 = p2.getY();
    }

    public void setP2(int x2, int y2) {
        this.x2 = x2;
        this.y2 = y2;
    }

    public void translate(int dx, int dy) {
        this.x1 += dx;
        this.y1 += dy;
        this.x2 += dx;
        this.y2 += dy;
    }

    public static boolean isNeighbor(Line line1, Line line2) {
        return Math.abs(line1.getP1().getX() - line2.getP1().getX()) < 2 && Math.abs(line1.getP1().getY() - line2.getP1().getY()) < 2 && Math.abs(line1.getP2().getX() - line2.getP2().getX()) < 2 && Math.abs(line1.getP2().getY() - line2.getP2().getY()) < 2;
    }

    public boolean isHorizontal() {
        return this.y1 == this.y2;
    }

    public boolean isVertical() {
        return this.x1 == this.x2;
    }

    public static boolean isCross(Line line1, Line line2) {
        if (line1.isHorizontal() && line2.isVertical()) {
            if (line1.getP1().getY() > line2.getP1().getY() && line1.getP1().getY() < line2.getP2().getY() && line2.getP1().getX() > line1.getP1().getX() && line2.getP1().getX() < line1.getP2().getX()) {
                return true;
            }
        } else if (line1.isVertical() && line2.isHorizontal() && line1.getP1().getX() > line2.getP1().getX() && line1.getP1().getX() < line2.getP2().getX() && line2.getP1().getY() > line1.getP1().getY() && line2.getP1().getY() < line1.getP2().getY()) {
            return true;
        }

        return false;
    }

    public Point getCenter() {
        int x = (this.x1 + this.x2) / 2;
        int y = (this.y1 + this.y2) / 2;
        return new Point(x, y);
    }

    public int getLength() {
        int x = Math.abs(this.x2 - this.x1);
        int y = Math.abs(this.y2 - this.y1);
        int r = QRCodeUtility.sqrt(x * x + y * y);
        return r;
    }

    public static Line getLongest(Line[] lines) {
        Line longestLine = new Line();

        for(int i = 0; i < lines.length; ++i) {
            if (lines[i].getLength() > longestLine.getLength()) {
                longestLine = lines[i];
            }
        }

        return longestLine;
    }

    public String toString() {
        return "(" + this.x1 + "," + this.y1 + ")-(" + this.x2 + "," + this.y2 + ")";
    }
}
