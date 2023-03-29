package com.ck.qr_code.swetake.jp.sourceforge.reedsolomon;

public class RsDecode {
    public static final int RS_PERM_ERROR = -1;
    public static final int RS_CORRECT_ERROR = -2;
    private static final Galois galois = Galois.getInstance();
    private int npar;

    public RsDecode(int npar) {
        this.npar = npar;
    }

    public int calcSigmaMBM(int[] sigma, int[] omega, int[] syn) {
        int[] sg0 = new int[this.npar];
        int[] sg1 = new int[this.npar];
        sg0[1] = 1;
        sg1[0] = 1;
        int jisu0 = 1;
        int jisu1 = 0;
        int m = -1;

        for(int n = 0; n < this.npar; ++n) {
            int d = syn[n];

            int logd;
            for(logd = 1; logd <= jisu1; ++logd) {
                d ^= galois.mul(sg1[logd], syn[n - logd]);
            }

            if (d != 0) {
                logd = galois.toLog(d);
                int[] wk = new int[this.npar];

                int js;
                for(js = 0; js <= n; ++js) {
                    wk[js] = sg1[js] ^ galois.mulExp(sg0[js], logd);
                }

                js = n - m;
                if (js > jisu1) {
                    m = n - jisu1;
                    jisu1 = js;
                    if (js > this.npar / 2) {
                        return -1;
                    }

                    for(int i = 0; i <= jisu0; ++i) {
                        sg0[i] = galois.divExp(sg1[i], logd);
                    }

                    jisu0 = js;
                }

                sg1 = wk;
            }

            System.arraycopy(sg0, 0, sg0, 1, Math.min(sg0.length - 1, jisu0));
            sg0[0] = 0;
            ++jisu0;
        }

        galois.mulPoly(omega, sg1, syn);
        System.arraycopy(sg1, 0, sigma, 0, Math.min(sg1.length, sigma.length));
        return jisu1;
    }

    private int chienSearch(int[] pos, int n, int jisu, int[] sigma) {
        int last = sigma[1];
        if (jisu == 1) {
            if (galois.toLog(last) >= n) {
                return -2;
            } else {
                pos[0] = last;
                return 0;
            }
        } else {
            int posIdx = jisu - 1;

            for(int i = 0; i < n; ++i) {
                int z = 255 - i;
                int wk = 1;

                int pv;
                for(pv = 1; pv <= jisu; ++pv) {
                    wk ^= galois.mulExp(sigma[pv], z * pv % 255);
                }

                if (wk == 0) {
                    pv = galois.toExp(i);
                    last ^= pv;
                    pos[posIdx--] = pv;
                    if (posIdx == 0) {
                        if (galois.toLog(last) >= n) {
                            return -2;
                        }

                        pos[0] = last;
                        return 0;
                    }
                }
            }

            return -2;
        }
    }

    private void doForney(int[] data, int length, int jisu, int[] pos, int[] sigma, int[] omega) {
        for(int i = 0; i < jisu; ++i) {
            int ps = pos[i];
            int zlog = 255 - galois.toLog(ps);
            int ov = omega[0];

            int dv;
            for(dv = 1; dv < jisu; ++dv) {
                ov ^= galois.mulExp(omega[dv], zlog * dv % 255);
            }

            dv = sigma[1];

            for(int j = 2; j < jisu; j += 2) {
                dv ^= galois.mulExp(sigma[j + 1], zlog * j % 255);
            }

            int var10001 = galois.toPos(length, ps);
            data[var10001] ^= galois.mul(ps, galois.div(ov, dv));
        }

    }

    public int decode(int[] data, int length, boolean noCorrect) {
        if (length >= this.npar && length <= 255) {
            int[] syn = new int[this.npar];
            if (galois.calcSyndrome(data, length, syn)) {
                return 0;
            } else {
                int[] sigma = new int[this.npar / 2 + 2];
                int[] omega = new int[this.npar / 2 + 1];
                int jisu = this.calcSigmaMBM(sigma, omega, syn);
                if (jisu <= 0) {
                    return -2;
                } else {
                    int[] pos = new int[jisu];
                    int r = this.chienSearch(pos, length, jisu, sigma);
                    if (r < 0) {
                        return r;
                    } else {
                        if (!noCorrect) {
                            this.doForney(data, length, jisu, pos, sigma, omega);
                        }

                        return jisu;
                    }
                }
            }
        } else {
            return -1;
        }
    }

    public int decode(int[] data, int length) {
        return this.decode(data, length, false);
    }

    public int decode(int[] data) {
        return this.decode(data, data.length, false);
    }
}
