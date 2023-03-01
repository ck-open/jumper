package com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception;

public class InvalidDataBlockException extends IllegalArgumentException {
    String message = null;

    public InvalidDataBlockException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
