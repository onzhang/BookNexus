package com.zjw.booknexus.common;

public final class ErrorCode {

    private ErrorCode() {}

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String USERNAME_EXISTS = "USERNAME_EXISTS";
    public static final String EMAIL_EXISTS = "EMAIL_EXISTS";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String TOKEN_INVALID = "TOKEN_INVALID";

    public static final String BOOK_NOT_FOUND = "BOOK_NOT_FOUND";
    public static final String BOOK_NOT_AVAILABLE = "BOOK_NOT_AVAILABLE";
    public static final String DUPLICATE_ISBN = "DUPLICATE_ISBN";

    public static final String BORROW_LIMIT_EXCEEDED = "BORROW_LIMIT_EXCEEDED";
    public static final String ALREADY_BORROWED = "ALREADY_BORROWED";
    public static final String RECORD_NOT_FOUND = "RECORD_NOT_FOUND";
    public static final String INVALID_STATUS = "INVALID_STATUS";
    public static final String RENEW_LIMIT_EXCEEDED = "RENEW_LIMIT_EXCEEDED";

    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
}
