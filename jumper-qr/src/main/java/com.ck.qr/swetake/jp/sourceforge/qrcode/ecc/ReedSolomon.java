package com.ck.qr.swetake.jp.sourceforge.qrcode.ecc;

public class ReedSolomon {
    int[] y;
    int[] gexp = new int[512];
    int[] glog = new int[256];
    int NPAR;
    int MAXDEG;
    int[] synBytes;
    int[] Lambda;
    int[] Omega;
    int[] ErrorLocs = new int[256];
    int NErrors;
    int[] ErasureLocs = new int[256];
    int NErasures = 0;
    boolean correctionSucceeded = true;

    public ReedSolomon(int[] source, int NPAR) {
        this.initializeGaloisTables();
        this.y = source;
        this.NPAR = NPAR;
        this.MAXDEG = this.NPAR * 2;
        this.synBytes = new int[this.MAXDEG];
        this.Lambda = new int[this.MAXDEG];
        this.Omega = new int[this.MAXDEG];
    }

    void initializeGaloisTables() {
        int p8 = 0;
        int p7 = 0;
        int p6 = 0;
        int p5 = 0;
        int p4 = 0;
        int p3 = 0;
        int p2 = 0;
        int p1 = 1;
        this.gexp[0] = 1;
        this.gexp[255] = this.gexp[0];
        this.glog[0] = 0;

        int i;
        for (i = 1; i < 256; ++i) {
            int pinit = p8;
            p8 = p7;
            p7 = p6;
            p6 = p5;
            p5 = p4 ^ pinit;
            p4 = p3 ^ pinit;
            p3 = p2 ^ pinit;
            p2 = p1;
            p1 = pinit;
            this.gexp[i] = pinit + p2 * 2 + p3 * 4 + p4 * 8 + p5 * 16 + p6 * 32 + p7 * 64 + p8 * 128;
            this.gexp[i + 255] = this.gexp[i];
        }

        for (i = 1; i < 256; ++i) {
            for (int z = 0; z < 256; ++z) {
                if (this.gexp[z] == i) {
                    this.glog[i] = z;
                    break;
                }
            }
        }

    }

    int gmult(int a, int b) {
        if (a != 0 && b != 0) {
            int i = this.glog[a];
            int j = this.glog[b];
            return this.gexp[i + j];
        } else {
            return 0;
        }
    }

    int ginv(int elt) {
        return this.gexp[255 - this.glog[elt]];
    }

    void decode_data(int[] data) {
        for (int j = 0; j < this.MAXDEG; ++j) {
            int sum = 0;

            for (int i = 0; i < data.length; ++i) {
                sum = data[i] ^ this.gmult(this.gexp[j + 1], sum);
            }

            this.synBytes[j] = sum;
        }

    }

    public void correct() {
        this.decode_data(this.y);
        this.correctionSucceeded = true;
        boolean hasError = false;

        for (int i = 0; i < this.synBytes.length; ++i) {
            if (this.synBytes[i] != 0) {
                hasError = true;
            }
        }

        if (hasError) {
            this.correctionSucceeded = this.correct_errors_erasures(this.y, this.y.length, 0, new int[1]);
        }

    }

    public boolean isCorrectionSucceeded() {
        return this.correctionSucceeded;
    }

    public int getNumCorrectedErrors() {
        return this.NErrors;
    }

    void Modified_Berlekamp_Massey() {
        int[] psi = new int[this.MAXDEG];
        int[] psi2 = new int[this.MAXDEG];
        int[] D = new int[this.MAXDEG];
        int[] gamma = new int[this.MAXDEG];
        this.init_gamma(gamma);
        this.copy_poly(D, gamma);
        this.mul_z_poly(D);
        this.copy_poly(psi, gamma);
        int k = -1;
        int L = this.NErasures;

        int i;
        for (int n = this.NErasures; n < 8; ++n) {
            int d = this.compute_discrepancy(psi, this.synBytes, L, n);
            if (d != 0) {
                for (i = 0; i < this.MAXDEG; ++i) {
                    psi2[i] = psi[i] ^ this.gmult(d, D[i]);
                }

                if (L < n - k) {
                    int L2 = n - k;
                    k = n - L;

                    for (i = 0; i < this.MAXDEG; ++i) {
                        D[i] = this.gmult(psi[i], this.ginv(d));
                    }

                    L = L2;
                }

                for (i = 0; i < this.MAXDEG; ++i) {
                    psi[i] = psi2[i];
                }
            }

            this.mul_z_poly(D);
        }

        for (i = 0; i < this.MAXDEG; ++i) {
            this.Lambda[i] = psi[i];
        }

        this.compute_modified_omega();
    }

    void compute_modified_omega() {
        int[] product = new int[this.MAXDEG * 2];
        this.mult_polys(product, this.Lambda, this.synBytes);
        this.zero_poly(this.Omega);

        for (int i = 0; i < this.NPAR; ++i) {
            this.Omega[i] = product[i];
        }

    }

    void mult_polys(int[] dst, int[] p1, int[] p2) {
        int[] tmp1 = new int[this.MAXDEG * 2];

        int i;
        for (i = 0; i < this.MAXDEG * 2; ++i) {
            dst[i] = 0;
        }

        for (i = 0; i < this.MAXDEG; ++i) {
            int j;
            for (j = this.MAXDEG; j < this.MAXDEG * 2; ++j) {
                tmp1[j] = 0;
            }

            for (j = 0; j < this.MAXDEG; ++j) {
                tmp1[j] = this.gmult(p2[j], p1[i]);
            }

            for (j = this.MAXDEG * 2 - 1; j >= i; --j) {
                tmp1[j] = tmp1[j - i];
            }

            for (j = 0; j < i; ++j) {
                tmp1[j] = 0;
            }

            for (j = 0; j < this.MAXDEG * 2; ++j) {
                dst[j] ^= tmp1[j];
            }
        }

    }

    void init_gamma(int[] gamma) {
        int[] tmp = new int[this.MAXDEG];
        this.zero_poly(gamma);
        this.zero_poly(tmp);
        gamma[0] = 1;

        for (int e = 0; e < this.NErasures; ++e) {
            this.copy_poly(tmp, gamma);
            this.scale_poly(this.gexp[this.ErasureLocs[e]], tmp);
            this.mul_z_poly(tmp);
            this.add_polys(gamma, tmp);
        }

    }

    void compute_next_omega(int d, int[] A, int[] dst, int[] src) {
        for (int i = 0; i < this.MAXDEG; ++i) {
            dst[i] = src[i] ^ this.gmult(d, A[i]);
        }

    }

    int compute_discrepancy(int[] lambda, int[] S, int L, int n) {
        int sum = 0;

        for (int i = 0; i <= L; ++i) {
            sum ^= this.gmult(lambda[i], S[n - i]);
        }

        return sum;
    }

    void add_polys(int[] dst, int[] src) {
        for (int i = 0; i < this.MAXDEG; ++i) {
            dst[i] ^= src[i];
        }

    }

    void copy_poly(int[] dst, int[] src) {
        for (int i = 0; i < this.MAXDEG; ++i) {
            dst[i] = src[i];
        }

    }

    void scale_poly(int k, int[] poly) {
        for (int i = 0; i < this.MAXDEG; ++i) {
            poly[i] = this.gmult(k, poly[i]);
        }

    }

    void zero_poly(int[] poly) {
        for (int i = 0; i < this.MAXDEG; ++i) {
            poly[i] = 0;
        }

    }

    void mul_z_poly(int[] src) {
        for (int i = this.MAXDEG - 1; i > 0; --i) {
            src[i] = src[i - 1];
        }

        src[0] = 0;
    }

    void Find_Roots() {
        this.NErrors = 0;

        for (int r = 1; r < 256; ++r) {
            int sum = 0;

            for (int k = 0; k < this.NPAR + 1; ++k) {
                sum ^= this.gmult(this.gexp[k * r % 255], this.Lambda[k]);
            }

            if (sum == 0) {
                this.ErrorLocs[this.NErrors] = 255 - r;
                ++this.NErrors;
            }
        }

    }

    boolean correct_errors_erasures(int[] codeword, int csize, int nerasures, int[] erasures) {
        this.NErasures = nerasures;

        int i;
        for (i = 0; i < this.NErasures; ++i) {
            this.ErasureLocs[i] = erasures[i];
        }

        this.Modified_Berlekamp_Massey();
        this.Find_Roots();
        if (this.NErrors > this.NPAR && this.NErrors <= 0) {
            return false;
        } else {
            int r;
            for (r = 0; r < this.NErrors; ++r) {
                if (this.ErrorLocs[r] >= csize) {
                    return false;
                }
            }

            for (r = 0; r < this.NErrors; ++r) {
                i = this.ErrorLocs[r];
                int num = 0;

                int j;
                for (j = 0; j < this.MAXDEG; ++j) {
                    num ^= this.gmult(this.Omega[j], this.gexp[(255 - i) * j % 255]);
                }

                int denom = 0;

                for (j = 1; j < this.MAXDEG; j += 2) {
                    denom ^= this.gmult(this.Lambda[j], this.gexp[(255 - i) * (j - 1) % 255]);
                }

                int err = this.gmult(num, this.ginv(denom));
                codeword[csize - i - 1] ^= err;
            }

            return true;
        }
    }
}
