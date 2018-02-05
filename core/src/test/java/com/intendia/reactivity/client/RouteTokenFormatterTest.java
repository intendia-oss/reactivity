package com.intendia.reactivity.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.EventBus;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;

public class RouteTokenFormatterTest {

    static class UrlUtilsTestImpl extends UrlUtils {
        @Override public String decodePathSegment(String str) { return decode(str); }
        @Override public String decodeQueryString(String str) { return decode(str); }
        @Override public String encodePathSegment(String str) { return encode(str); }
        @Override public String encodeQueryString(String str) { return encode(str); }
        private String decode(String str) {
            try { return URLDecoder.decode(str, "UTF-8"); } catch (UnsupportedEncodingException e) { return null; }
        }
        private String encode(String str) {
            try { return URLEncoder.encode(str, "UTF-8"); } catch (UnsupportedEncodingException e) { return null; }
        }
    }

    EventBus bus;
    RouteTokenFormatter tokenFormatter;

    @Before public void init() {
        bus = new SimpleEventBus();
        tokenFormatter = new RouteTokenFormatter(new UrlUtilsTestImpl(), Stream.of(
                "/user/{userId}/groups/{groupId}",
                "/user/{userId}/albums/{albumId}",
                "/user/{userId}/albums/staticAlbumId",
                "/user/staticUserId/albums/{albumId}",
                "/user/staticUserId/albums/staticAlbumId",
                "/{vanityId}",
                "!/crawl/{vanityId}",
                "/privacy",
                "/")
                .<Proxy<?>>map(n -> new Proxy<>(() -> null, bus, new Place(n)))
                .collect(Collectors.toSet()));
    }

    @Test public void testToPlaceTokenWithoutQueryString() {
        // Given
        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken("/user/{userId}/albums/{albumId}")
                .with("userId", "0x42")
                .with("albumId", "0xAFFE")
                .build();
        String expectedPlacePattern = "^\\/user\\/0x42\\/albums\\/0xAFFE$";
        Map<String, String> expectedQueryParameters = null;

        // When
        String placeToken = tokenFormatter.toPlaceToken(placeRequest);
        Map<String, String> queryParameters = (placeToken.indexOf('?') != -1) ? tokenFormatter.parseQueryString(
                placeToken.substring(placeToken.indexOf('?') + 1), null) : null;
        // Then
        assertTrue(placeToken.matches(expectedPlacePattern));
        assertEquals(expectedQueryParameters, queryParameters);
    }

    @Test public void testToPlaceTokenWithEmbeddedUrlUnsafeParam() {
        // Given
        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken("/user/{userId}/albums/{albumId}")
                .with("userId", "Two Words")
                .with("albumId", "Then Three Words")
                .build();
        String expectedPlacePattern = "/user/Two+Words/albums/Then+Three+Words";

        // When
        String placeToken = tokenFormatter.toPlaceToken(placeRequest);
        // Then
        assertTrue(placeToken.equals(expectedPlacePattern));

        // And in reverse
        PlaceRequest placeRequest1 = tokenFormatter.toPlaceRequest(placeToken);
        // Then
        assertEquals(placeRequest1.getParameter("userId", ""), "Two Words");
    }

    @Test public void testToPlaceTokenWithOneQueryStringParameter() {
        // Given
        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken("/user/{userId}/albums/{albumId}")
                .with("userId", "0x42")
                .with("albumId", "0xAFFE")
                .with("start", "0")
                .build();
        String expectedPlacePattern = "^\\/user\\/0x42\\/albums\\/0xAFFE\\?\\w*=\\d*$";
        Map<String, String> expectedQueryParameters = ImmutableMap.<String, String>builder().put("start", "0").build();

        // When
        String placeToken = tokenFormatter.toPlaceToken(placeRequest);
        Map<String, String> queryParameters = (placeToken.indexOf('?') != -1) ? tokenFormatter.parseQueryString(
                placeToken.substring(placeToken.indexOf('?') + 1), null) : null;
        // Then
        assertTrue(placeToken.matches(expectedPlacePattern));
        assertEquals(expectedQueryParameters, queryParameters);
    }

    @Test public void testToPlaceTokenWithSeveralQueryStringParameter() {
        // Given
        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken("/user/{userId}/albums/{albumId}")
                .with("userId", "0x42")
                .with("albumId", "0xAFFE")
                .with("start", "15")
                .with("limit", "20")
                .build();
        String expectedPlacePattern = "^\\/user\\/0x42\\/albums\\/0xAFFE\\?\\w*=\\d*&\\w*=\\d*$";
        Map<String, String> expectedQueryParameters = ImmutableMap.<String, String>builder()
                .put("start", "15")
                .put("limit", "20")
                .build();

        // When
        String placeToken = tokenFormatter.toPlaceToken(placeRequest);
        Map<String, String> queryParameters = (placeToken.indexOf('?') != -1) ? tokenFormatter.parseQueryString(
                placeToken.substring(placeToken.indexOf('?') + 1), null) : null;
        // Then
        assertTrue(placeToken.matches(expectedPlacePattern));
        assertEquals(expectedQueryParameters, queryParameters);
    }

    @Test public void testToPlaceRequestStaticVsParameterMatch() {
        // When
        PlaceRequest placeRequest01 = tokenFormatter.toPlaceRequest("/user/0x42/albums/0xAFFE");
        PlaceRequest placeRequest02 = tokenFormatter.toPlaceRequest("/user/staticUserId/albums/staticAlbumId");
        PlaceRequest placeRequest03 = tokenFormatter.toPlaceRequest("/user/0x42/albums/staticAlbumId");
        PlaceRequest placeRequest04 = tokenFormatter.toPlaceRequest("/user/staticUserId/albums/0xAFFE");
        PlaceRequest placeRequest05 = tokenFormatter.toPlaceRequest("/privacy");
        PlaceRequest placeRequest06 = tokenFormatter.toPlaceRequest("/vanity");
        PlaceRequest placeRequest07 = tokenFormatter.toPlaceRequest("/user/0x42/albums/0xAFFE?start=0");
        PlaceRequest placeRequest08 = tokenFormatter.toPlaceRequest("/vanity?a=valueA&b=valueB");
        PlaceRequest placeRequest09 = tokenFormatter.toPlaceRequest("vanity?a=valueA&b=valueB");
        PlaceRequest placeRequest10 = tokenFormatter.toPlaceRequest("!/crawl/vanity");
        PlaceRequest placeRequest11 = tokenFormatter.toPlaceRequest("!/crawl/vanity?a=valueA&b=valueB");

        // Then
        assertEquals("/user/{userId}/albums/{albumId}", placeRequest01.getNameToken());
        assertEquals(2, placeRequest01.getParameterNames().size());
        assertEquals("0x42", placeRequest01.getParameter("userId", null));
        assertEquals("0xAFFE", placeRequest01.getParameter("albumId", null));

        assertEquals("/user/staticUserId/albums/staticAlbumId", placeRequest02.getNameToken());
        assertEquals(0, placeRequest02.getParameterNames().size());

        assertEquals("/user/{userId}/albums/staticAlbumId", placeRequest03.getNameToken());
        assertEquals(1, placeRequest03.getParameterNames().size());
        assertEquals("0x42", placeRequest03.getParameter("userId", null));

        assertEquals("/user/staticUserId/albums/{albumId}", placeRequest04.getNameToken());
        assertEquals(1, placeRequest04.getParameterNames().size());
        assertEquals("0xAFFE", placeRequest04.getParameter("albumId", null));

        assertEquals("/privacy", placeRequest05.getNameToken());
        assertEquals(0, placeRequest05.getParameterNames().size());

        assertEquals("/{vanityId}", placeRequest06.getNameToken());
        assertEquals(1, placeRequest06.getParameterNames().size());
        assertEquals("vanity", placeRequest06.getParameter("vanityId", null));

        assertEquals("/user/{userId}/albums/{albumId}", placeRequest07.getNameToken());
        assertEquals(3, placeRequest07.getParameterNames().size());
        assertEquals("0x42", placeRequest07.getParameter("userId", null));
        assertEquals("0xAFFE", placeRequest07.getParameter("albumId", null));
        assertEquals("0", placeRequest07.getParameter("start", null));

        assertEquals("/{vanityId}", placeRequest08.getNameToken());
        assertEquals(3, placeRequest08.getParameterNames().size());
        assertEquals("vanity", placeRequest08.getParameter("vanityId", null));
        assertEquals("valueA", placeRequest08.getParameter("a", null));
        assertEquals("valueB", placeRequest08.getParameter("b", null));

        assertEquals("/{vanityId}", placeRequest09.getNameToken());
        assertEquals(3, placeRequest09.getParameterNames().size());
        assertEquals("vanity", placeRequest09.getParameter("vanityId", null));
        assertEquals("valueA", placeRequest09.getParameter("a", null));
        assertEquals("valueB", placeRequest09.getParameter("b", null));

        assertEquals("!/crawl/{vanityId}", placeRequest10.getNameToken());
        assertEquals(1, placeRequest10.getParameterNames().size());
        assertEquals("vanity", placeRequest10.getParameter("vanityId", null));

        assertEquals("!/crawl/{vanityId}", placeRequest11.getNameToken());
        assertEquals(3, placeRequest11.getParameterNames().size());
        assertEquals("vanity", placeRequest10.getParameter("vanityId", null));
        assertEquals("valueA", placeRequest11.getParameter("a", null));
        assertEquals("valueB", placeRequest11.getParameter("b", null));
    }

    @Test public void testToPlaceRequestEmptyRoute() {
        // When
        PlaceRequest placeRequest10 = tokenFormatter.toPlaceRequest("/");
        PlaceRequest placeRequest11 = tokenFormatter.toPlaceRequest("/?a=valueA&b=valueB");
        PlaceRequest placeRequest12 = tokenFormatter.toPlaceRequest("");
        PlaceRequest placeRequest13 = tokenFormatter.toPlaceRequest("?a=valueA&b=valueB");

        // Then
        assertEquals("/", placeRequest10.getNameToken());
        assertEquals(0, placeRequest10.getParameterNames().size());

        assertEquals("/", placeRequest11.getNameToken());
        assertEquals(2, placeRequest11.getParameterNames().size());
        assertEquals("valueA", placeRequest11.getParameter("a", null));
        assertEquals("valueB", placeRequest11.getParameter("b", null));

        assertEquals("/", placeRequest12.getNameToken());
        assertEquals(0, placeRequest12.getParameterNames().size());

        assertEquals("/", placeRequest13.getNameToken());
        assertEquals(2, placeRequest13.getParameterNames().size());
        assertEquals("valueA", placeRequest13.getParameter("a", null));
        assertEquals("valueB", placeRequest13.getParameter("b", null));
    }

    @Test public void testToPlaceRequestNotExistingRoute() {
        // When
        PlaceRequest placeRequest13 = tokenFormatter.toPlaceRequest("/not/existing");
        PlaceRequest placeRequest14 = tokenFormatter.toPlaceRequest("/not/existing?a=valueA&b=valueB");
        PlaceRequest placeRequest15 = tokenFormatter.toPlaceRequest("not/existing");

        // Then
        assertEquals("/not/existing", placeRequest13.getNameToken());
        assertEquals(0, placeRequest13.getParameterNames().size());

        assertEquals("/not/existing", placeRequest14.getNameToken());
        assertEquals(2, placeRequest14.getParameterNames().size());
        assertEquals("valueA", placeRequest14.getParameter("a", null));
        assertEquals("valueB", placeRequest14.getParameter("b", null));

        assertEquals("/not/existing", placeRequest15.getNameToken());
        assertEquals(0, placeRequest15.getParameterNames().size());
    }
}
