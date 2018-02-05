package com.intendia.reactivity.client;

import com.google.gwt.http.client.URL;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UrlUtils {
    @Inject public UrlUtils() {}
    public String decodeQueryString(String encodedUrlComponent) { return URL.decodeQueryString(encodedUrlComponent); }
    public String encodeQueryString(String decodedUrlComponent) { return URL.encodeQueryString(decodedUrlComponent); }
    public String decodePathSegment(String encodedPathSegment) { return URL.decodePathSegment(encodedPathSegment); }
    public String encodePathSegment(String decodedPathSegment) { return URL.encodePathSegment(decodedPathSegment); }
}
