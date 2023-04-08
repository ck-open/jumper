package com.ck.qr.swetake.jp.sourceforge.qrcode.util;

import com.ck.qr.swetake.jp.sourceforge.qrcode.geom.Line;
import com.ck.qr.swetake.jp.sourceforge.qrcode.geom.Point;


public interface DebugCanvas {
    void println(String var1);

    void drawPoint(Point var1, int var2);

    void drawCross(Point var1, int var2);

    void drawPoints(Point[] var1, int var2);

    void drawLine(Line var1, int var2);

    void drawLines(Line[] var1, int var2);

    void drawPolygon(Point[] var1, int var2);

    void drawMatrix(boolean[][] var1);
}
