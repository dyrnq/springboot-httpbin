package com.dyrnq.httpbin.api.entity;

public class AuthInfo {
    private final String type;
    private final String content;

    private AuthInfo(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public static AuthInfo create(String headerValue) {
        if (headerValue == null) {
            return null;
        }

        try {
            String[] tempArray = headerValue.split(" ", 2);
            return new AuthInfo(tempArray[0].toLowerCase().trim(), tempArray[1].trim());
        } catch (Throwable t) {
            return null;
        }
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
