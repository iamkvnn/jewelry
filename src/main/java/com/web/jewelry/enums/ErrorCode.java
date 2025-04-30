package com.web.jewelry.enums;

public enum ErrorCode {
    USER_EXISTED(400, "User existed"),
    USER_NOT_EXISTED(400, "User not existed"),
    UNAUTHORIZED(401, "Unauthorized"),
    ACCESS_DINED(403, "Access denied"),
    TOKEN_INVALID(400, "Token invalid"),
    BOOK_NOT_FOUND(404, "Book not found"),
    ROLE_EXISTED(400, "Role existed"),
    ROLE_NOT_EXISTED(400, "Role not existed"),
    CART_NOT_FOUND(404, "Cart not found"),
    REFRESH_TOKEN_EXPIRED(401, "Refresh token expired"),
    REFRESH_TOKEN_INVALID(401, "Refresh token invalid"),
    TOKEN_BLACK_LIST(400, "Token black list"),
    SIGN_OUT_FAILED(400, "Sign out failed"),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}