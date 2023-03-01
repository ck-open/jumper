package com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception;


public class InvalidVersionInfoException extends VersionInformationException {
    String message = null;

    public InvalidVersionInfoException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
