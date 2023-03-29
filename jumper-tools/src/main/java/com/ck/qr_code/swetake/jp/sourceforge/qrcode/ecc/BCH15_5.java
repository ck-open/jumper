package com.ck.qr_code.swetake.jp.sourceforge.qrcode.ecc;

public class BCH15_5 {
    int[][] gf16 = this.createGF16();
    boolean[] receiveData;
    int numCorrectedError;
    static String[] bitName = new String[]{"c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "d0", "d1", "d2", "d3", "d4"};

    public BCH15_5(boolean[] source) {
        this.receiveData = source;
    }

    public boolean[] correct() {
        int[] s = this.calcSyndrome(this.receiveData);
        int[] errorPos = this.detectErrorBitPosition(s);
        boolean[] output = this.correctErrorBit(this.receiveData, errorPos);
        return output;
    }

    int[][] createGF16() {
        this.gf16 = new int[16][4];
        int[] seed = new int[]{1, 1, 0, 0};

        int i;
        for(i = 0; i < 4; ++i) {
            this.gf16[i][i] = 1;
        }

        for(i = 0; i < 4; ++i) {
            this.gf16[4][i] = seed[i];
        }

        for(i = 5; i < 16; ++i) {
            int j;
            for(j = 1; j < 4; ++j) {
                this.gf16[i][j] = this.gf16[i - 1][j - 1];
            }

            if (this.gf16[i - 1][3] == 1) {
                for(j = 0; j < 4; ++j) {
                    this.gf16[i][j] = (this.gf16[i][j] + seed[j]) % 2;
                }
            }
        }

        return this.gf16;
    }

    int searchElement(int[] x) {
        int k;
        for(k = 0; k < 15 && (x[0] != this.gf16[k][0] || x[1] != this.gf16[k][1] || x[2] != this.gf16[k][2] || x[3] != this.gf16[k][3]); ++k) {
        }

        return k;
    }

    int[] getCode(int input) {
        int[] f = new int[15];
        int[] r = new int[8];

        for(int i = 0; i < 15; ++i) {
            int w1 = r[7];
            int w2;
            int yin;
            if (i < 7) {
                yin = (input >> 6 - i) % 2;
                w2 = (yin + w1) % 2;
            } else {
                yin = w1;
                w2 = 0;
            }

            r[7] = (r[6] + w2) % 2;
            r[6] = (r[5] + w2) % 2;
            r[5] = r[4];
            r[4] = (r[3] + w2) % 2;
            r[3] = r[2];
            r[2] = r[1];
            r[1] = r[0];
            r[0] = w2;
            f[14 - i] = yin;
        }

        return f;
    }

    int addGF(int arg1, int arg2) {
        int[] p = new int[4];

        for(int m = 0; m < 4; ++m) {
            int w1 = arg1 >= 0 && arg1 < 15 ? this.gf16[arg1][m] : 0;
            int w2 = arg2 >= 0 && arg2 < 15 ? this.gf16[arg2][m] : 0;
            p[m] = (w1 + w2) % 2;
        }

        return this.searchElement(p);
    }

    int[] calcSyndrome(boolean[] y) {
        int[] s = new int[5];
        int[] p = new int[4];

        int k;
        int m;
        for(k = 0; k < 15; ++k) {
            if (y[k]) {
                for(m = 0; m < 4; ++m) {
                    p[m] = (p[m] + this.gf16[k][m]) % 2;
                }
            }
        }

        k = this.searchElement(p);
        s[0] = k >= 15 ? -1 : k;
        s[1] = s[0] < 0 ? -1 : s[0] * 2 % 15;
        p = new int[4];

        for(k = 0; k < 15; ++k) {
            if (y[k]) {
                for(m = 0; m < 4; ++m) {
                    p[m] = (p[m] + this.gf16[k * 3 % 15][m]) % 2;
                }
            }
        }

        k = this.searchElement(p);
        s[2] = k >= 15 ? -1 : k;
        s[3] = s[1] < 0 ? -1 : s[1] * 2 % 15;
        p = new int[4];

        for(k = 0; k < 15; ++k) {
            if (y[k]) {
                for(m = 0; m < 4; ++m) {
                    p[m] = (p[m] + this.gf16[k * 5 % 15][m]) % 2;
                }
            }
        }

        k = this.searchElement(p);
        s[4] = k >= 15 ? -1 : k;
        return s;
    }

    int[] calcErrorPositionVariable(int[] s) {
        int[] e = new int[4];
        e[0] = s[0];
        int t = (s[0] + s[1]) % 15;
        int mother = this.addGF(s[2], t);
        mother = mother >= 15 ? -1 : mother;
        t = (s[2] + s[1]) % 15;
        int child = this.addGF(s[4], t);
        child = child >= 15 ? -1 : child;
        e[1] = child < 0 && mother < 0 ? -1 : (child - mother + 15) % 15;
        t = (s[1] + e[0]) % 15;
        int t1 = this.addGF(s[2], t);
        t = (s[0] + e[1]) % 15;
        e[2] = this.addGF(t1, t);
        return e;
    }

    int[] detectErrorBitPosition(int[] s) {
        int[] e = this.calcErrorPositionVariable(s);
        int[] errorPos = new int[4];
        if (e[0] == -1) {
            return errorPos;
        } else if (e[1] == -1) {
            errorPos[0] = 1;
            errorPos[1] = e[0];
            return errorPos;
        } else {
            for(int i = 0; i < 15; ++i) {
                int x3 = i * 3 % 15;
                int x2 = i * 2 % 15;
                int t = (e[0] + x2) % 15;
                int t1 = this.addGF(x3, t);
                t = (e[1] + i) % 15;
                int t2 = this.addGF(t, e[2]);
                int anError = this.addGF(t1, t2);
                if (anError >= 15) {
                    int var10002 = errorPos[0]++;
                    errorPos[errorPos[0]] = i;
                }
            }

            return errorPos;
        }
    }

    boolean[] correctErrorBit(boolean[] y, int[] errorPos) {
        for(int i = 1; i <= errorPos[0]; ++i) {
            y[errorPos[i]] = !y[errorPos[i]];
        }

        this.numCorrectedError = errorPos[0];
        return y;
    }

    public int getNumCorrectedError() {
        return this.numCorrectedError;
    }
}
