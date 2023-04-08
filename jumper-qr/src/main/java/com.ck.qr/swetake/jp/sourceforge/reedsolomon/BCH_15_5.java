package com.ck.qr.swetake.jp.sourceforge.reedsolomon;

public final class BCH_15_5 {
    private static final int GX = 311;
    private static final BCH_15_5 instance = new BCH_15_5();
    private int[] trueCodes = new int[32];

    private BCH_15_5() {
        this.makeTrueCodes();
    }

    public static BCH_15_5 getInstance() {
        return instance;
    }

    private void makeTrueCodes() {
        for(int i = 0; i < this.trueCodes.length; ++i) {
            this.trueCodes[i] = this.slowEncode(i);
        }

    }

    private int slowEncode(int data) {
        int wk = 0;
        data <<= 5;

        for(int i = 0; i < 5; ++i) {
            wk <<= 1;
            data <<= 1;
            if (((wk ^ data) & 1024) != 0) {
                wk ^= 311;
            }
        }

        return data & 31744 | wk & 1023;
    }

    public int encode(int data) {
        return this.trueCodes[data & 31];
    }

    private static int calcDistance(int c1, int c2) {
        int n = 0;

        for(int wk = c1 ^ c2; wk != 0; wk >>= 1) {
            if ((wk & 1) != 0) {
                ++n;
            }
        }

        return n;
    }

    public int decode(int data) {
        data &= 32767;

        for(int i = 0; i < this.trueCodes.length; ++i) {
            int code = this.trueCodes[i];
            if (calcDistance(data, code) <= 3) {
                return code;
            }
        }

        return -1;
    }
}
