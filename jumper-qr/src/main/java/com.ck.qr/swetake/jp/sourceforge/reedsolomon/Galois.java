package com.ck.qr.swetake.jp.sourceforge.reedsolomon;

import java.util.Arrays;

public final class Galois {
    public static final int POLYNOMIAL = 29;
    private static final Galois instance = new Galois();
    private int[] expTbl = new int[510];
    private int[] logTbl = new int[256];

    private Galois() {
        this.initGaloisTable();
    }

    public static Galois getInstance() {
        return instance;
    }

    private void initGaloisTable() {
        int d = 1;

        for(int i = 0; i < 255; ++i) {
            this.expTbl[i] = this.expTbl[255 + i] = d;
            this.logTbl[d] = i;
            d <<= 1;
            if ((d & 256) != 0) {
                d = (d ^ 29) & 255;
            }
        }

    }

    public int toExp(int a) {
        return this.expTbl[a];
    }

    public int toLog(int a) {
        return this.logTbl[a];
    }

    public int toPos(int length, int a) {
        return length - 1 - this.logTbl[a];
    }

    public int mul(int a, int b) {
        return a != 0 && b != 0 ? this.expTbl[this.logTbl[a] + this.logTbl[b]] : 0;
    }

    public int mulExp(int a, int b) {
        return a == 0 ? 0 : this.expTbl[this.logTbl[a] + b];
    }

    public int div(int a, int b) {
        return a == 0 ? 0 : this.expTbl[this.logTbl[a] - this.logTbl[b] + 255];
    }

    public int divExp(int a, int b) {
        return a == 0 ? 0 : this.expTbl[this.logTbl[a] - b + 255];
    }

    public int inv(int a) {
        return this.expTbl[255 - this.logTbl[a]];
    }

    public void mulPoly(int[] seki, int[] a, int[] b) {
        Arrays.fill(seki, 0);

        for(int ia = 0; ia < a.length; ++ia) {
            if (a[ia] != 0) {
                int loga = this.logTbl[a[ia]];
                int ib2 = Math.min(b.length, seki.length - ia);

                for(int ib = 0; ib < ib2; ++ib) {
                    if (b[ib] != 0) {
                        seki[ia + ib] ^= this.expTbl[loga + this.logTbl[b[ib]]];
                    }
                }
            }
        }

    }

    public boolean calcSyndrome(int[] data, int length, int[] syn) {
        int hasErr = 0;

        for(int i = 0; i < syn.length; ++i) {
            int wk = 0;

            for(int idx = 0; idx < length; ++idx) {
                wk = data[idx] ^ (wk == 0 ? 0 : this.expTbl[this.logTbl[wk] + i]);
            }

            syn[i] = wk;
            hasErr |= wk;
        }

        if (hasErr == 0) {
            return true;
        } else {
            return false;
        }
    }
}
