package com.intendia.reactivity.client;

/** Formats tokens from {@code String} values to {@link PlaceRequest} and vice-versa. */
public interface TokenFormatter {

    /** Converts a place token into a {@link PlaceRequest}. */
    PlaceRequest toPlaceRequest(String placeToken) throws TokenFormatException;

    /**
     * Converts a {@link PlaceRequest} into a place token. The place token will not be fully query encoded, it may
     * still contain some unescaped separators. However, these separators can be encoded and it will still work,
     * making the scheme robust to systems that force encoding these characters like some email apps.
     */
    String toPlaceToken(PlaceRequest placeRequest);

    final class TokenFormatException extends RuntimeException {
        public TokenFormatException() {}
        public TokenFormatException(String message) { super(message); }
    }
}
