package com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception;

public class InvalidVersionException extends VersionInformationException {
    String message;

    public InvalidVersionException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
