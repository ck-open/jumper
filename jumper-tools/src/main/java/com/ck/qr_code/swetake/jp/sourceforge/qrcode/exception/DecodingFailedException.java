package com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception;

public class DecodingFailedException extends IllegalArgumentException {
    String message = null;

    public DecodingFailedException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}