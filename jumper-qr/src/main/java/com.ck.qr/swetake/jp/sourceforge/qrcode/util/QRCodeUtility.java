package com.ck.qr.swetake.jp.sourceforge.qrcode.util;

public class QRCodeUtility {
    public QRCodeUtility() {
    }

    public static int sqrt(int val) {
        int g = 0;
        int b = 32768;
        int var4 = 15;

        do {
            int temp;
            if (val >= (temp = (g << 1) + b << var4--)) {
                g += b;
                val -= temp;
            }
        } while((b >>= 1) > 0);

        return g;
    }
}
