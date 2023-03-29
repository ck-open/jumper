package com.ck.qr_code.swetake.jp.sourceforge.reedsolomon;

public class RsEncode {
    public static final int RS_PERM_ERROR = -1;
    private static final Galois galois = Galois.getInstance();
    private int npar;
    private int[] encodeGx;

    public RsEncode(int npar) {
        this.npar = npar;
        this.makeEncodeGx();
    }

    private void makeEncodeGx() {
        this.encodeGx = new int[this.npar];
        this.encodeGx[this.npar - 1] = 1;

        for(int kou = 0; kou < this.npar; ++kou) {
            int ex = galois.toExp(kou);

            for(int i = 0; i < this.npar - 1; ++i) {
                this.encodeGx[i] = galois.mul(this.encodeGx[i], ex) ^ this.encodeGx[i + 1];
            }

            this.encodeGx[this.npar - 1] = galois.mul(this.encodeGx[this.npar - 1], ex);
        }

    }

    public int encode(int[] data, int length, int[] parity, int parityStartPos) {
        if (length >= 0 && length + this.npar <= 255) {
            int[] wr = new int[this.npar];

            for(int idx = 0; idx < length; ++idx) {
                int code = data[idx];
                int ib = wr[0] ^ code;

                for(int i = 0; i < this.npar - 1; ++i) {
                    wr[i] = wr[i + 1] ^ galois.mul(ib, this.encodeGx[i]);
                }

                wr[this.npar - 1] = galois.mul(ib, this.encodeGx[this.npar - 1]);
            }

            if (parity != null) {
                System.arraycopy(wr, 0, parity, parityStartPos, this.npar);
            }

            return 0;
        } else {
            return -1;
        }
    }

    public int encode(int[] data, int length, int[] parity) {
        return this.encode(data, length, parity, 0);
    }

    public int encode(int[] data, int[] parity) {
        return this.encode(data, data.length, parity, 0);
    }
}
