package com.intendia.reactivity.client;

public class PlaceException extends RuntimeException {
    public PlaceException() { super(); }
    public PlaceException(String message) { super(message); }
    public PlaceException(String message, Throwable cause) { super(message, cause); }

    public static class PlaceRequestException extends PlaceException {
        private final PlaceRequest request;

        public PlaceRequestException(PlaceRequest request) { this(request, null, null); }
        public PlaceRequestException(PlaceRequest request, String message) { this(request, message, null); }
        public PlaceRequestException(PlaceRequest request, String message, Throwable cause) {
            super(message, cause);
            this.request = request;
        }

        public PlaceRequest getRequest() {
            return request;
        }
    }

    public static class PlaceNotFoundException extends PlaceRequestException {
        public PlaceNotFoundException(PlaceRequest request) { super(request); }
    }

    public static class BadPlaceTokenException extends PlaceException {
        public BadPlaceTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
