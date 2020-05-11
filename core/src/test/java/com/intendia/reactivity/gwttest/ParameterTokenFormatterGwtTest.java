package com.intendia.reactivity.gwttest;

import com.google.gwt.http.client.URL;
import com.google.gwt.junit.client.GWTTestCase;
import com.intendia.reactivity.client.ParameterTokenFormatter;
import com.intendia.reactivity.client.PlaceRequest;
import com.intendia.reactivity.client.TokenFormatter;
import com.intendia.reactivity.client.UrlUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/** Unit tests for {@link ParameterTokenFormatter}. */
public class ParameterTokenFormatterGwtTest extends GWTTestCase {

    ParameterTokenFormatter tokenFormatter;

    @Override public String getModuleName() { return "com.intendia.reactivity.gwttest.ReactivityGwtTest"; }

    @Override protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        tokenFormatter = new ParameterTokenFormatter(new UrlUtils());
    }

    //------------------------------------------
    // Utility methods
    //------------------------------------------
    private String urlEncodeEverything(String input) {
        return URL.encodeQueryString(URL.decodeQueryString(input));
    }

    //------------------------------------------
    // Tests for toPlaceRequest
    //------------------------------------------

    public void testToPlaceRequestEmptyPlaceToken() {
        // Given
        String placeToken = "";

        // When
        PlaceRequest placeRequest = tokenFormatter.toPlaceRequest(placeToken);

        // Then
        assertEquals(0, placeRequest.getParameterNames().size());
    }

    public void testToPlaceRequestMissingNameToken() {
        try {
            tokenFormatter.toPlaceRequest(";key1=value1;key2=value2");
            fail("TokenFormatException (Place history token is missing) was expected");
        } catch (TokenFormatter.TokenFormatException ignore) {}
    }

    public void testToPlaceRequestRegularPlaceToken() {
        // Given
        String placeToken = "nameToken;key1=value1;key2=value2";

        // When
        PlaceRequest placeRequest = tokenFormatter.toPlaceRequest(placeToken);

        // Then
        assertTrue(placeRequest.matchesNameToken("nameToken"));
        assertEquals(2, placeRequest.getParameterNames().size());
        assertEquals("value1", placeRequest.getParameter("key1", null));
        assertEquals("value2", placeRequest.getParameter("key2", null));
    }

    public void testToPlaceRequestPlaceTokenWithSameKeysKeepsLastValue() {
        // Given
        String placeToken = "nameToken;key1=value1;key1=value2";

        // When
        PlaceRequest placeRequest = tokenFormatter.toPlaceRequest(placeToken);

        // Then
        assertTrue(placeRequest.matchesNameToken("nameToken"));
        assertEquals(1, placeRequest.getParameterNames().size());
        assertEquals("value2", placeRequest.getParameter("key1", null));
    }

    public void testToPlaceRequestPlaceTokenWithSameValues() {
        // Given
        String placeToken = "nameToken;key1=value1;key2=value1";

        // When
        PlaceRequest placeRequest = tokenFormatter.toPlaceRequest(placeToken);

        // Then
        assertTrue(placeRequest.matchesNameToken("nameToken"));
        assertEquals(2, placeRequest.getParameterNames().size());
        assertEquals("value1", placeRequest.getParameter("key1", null));
        assertEquals("value1", placeRequest.getParameter("key2", null));
    }

    public void testToPlaceRequestPlaceTokenWithEmptyValue() {
        // Given
        String placeToken = "nameToken;key1=;key2=value2";

        // When
        PlaceRequest placeRequest = tokenFormatter.toPlaceRequest(placeToken);

        // Then
        assertTrue(placeRequest.matchesNameToken("nameToken"));
        assertEquals(2, placeRequest.getParameterNames().size());
        assertEquals("", placeRequest.getParameter("key1", null));
        assertEquals("value2", placeRequest.getParameter("key2", null));
    }

    public void testToPlaceRequestPlaceTokenWithEmptyTrailingValue() {
        // Given
        String placeToken = "nameToken;key1=value1;key2=";

        // When
        PlaceRequest placeRequest = tokenFormatter.toPlaceRequest(placeToken);

        // Then
        assertTrue(placeRequest.matchesNameToken("nameToken"));
        assertEquals(2, placeRequest.getParameterNames().size());
        assertEquals("value1", placeRequest.getParameter("key1", null));
        assertEquals("", placeRequest.getParameter("key2", null));
    }

    public void testToPlaceRequestPlaceTokenWithEmptyParamValues() {
        // Given
        String placeToken = "nameToken;key1=;key2=";

        // When
        PlaceRequest placeRequest = tokenFormatter.toPlaceRequest(placeToken);

        // Then
        assertTrue(placeRequest.matchesNameToken("nameToken"));
        assertEquals(2, placeRequest.getParameterNames().size());
        assertEquals("", placeRequest.getParameter("key1", null));
        assertEquals("", placeRequest.getParameter("key2", null));
    }

    public void testToPlaceRequestPlaceTokenWithEmptyKey() {
        // Given
        String placeToken = "nameToken;=value1;key2=value2";

        // When
        PlaceRequest placeRequest = tokenFormatter.toPlaceRequest(placeToken);

        // Then
        assertEquals(2, placeRequest.getParameterNames().size());
        assertEquals(null, placeRequest.getParameter("key1", null));
        assertEquals("value1", placeRequest.getParameter("", null));
        assertEquals("value2", placeRequest.getParameter("key2", null));
    }

    public void testToPlaceRequestDiscardsTrailingParamSeparators() {
        // Given
        String placeToken = "token;a=b;;";

        // When
        PlaceRequest placeRequest = tokenFormatter.toPlaceRequest(placeToken);

        // Then
        assertEquals(1, placeRequest.getParameterNames().size());
    }

    public void testToPlaceRequestPlaceTokenKeyMissingValue() {
        // Given
        String placeToken = "nameToken;key1;key2=value2";

        try {
            // When
            tokenFormatter.toPlaceRequest(placeToken);
            fail("TokenFormatException (Bad parameter) was expected.");
        } catch (TokenFormatter.TokenFormatException e) {
            // Then
        }
    }

    public void testToPlaceRequestPlaceTokenWithUnescapedValueSeparators() {
        // Given
        String[] placeTokens = { "token;=a=b", "token;a==b", "token;a=b=" };

        for (String placeToken : placeTokens) {
            try {
                // When
                tokenFormatter.toPlaceRequest(placeToken);
                fail("TokenFormatException was expected for '" + placeToken + "'");
            } catch (Exception ex) {
                // Then
            }
        }
    }

    public void testToPlaceRequestPlaceTokenWithUnescapedParamSeparators() {
        // Given
        String[] placeTokens = { "token;;a=b", "token;a=b;;c=d" };

        for (String placeToken : placeTokens) {
            try {
                // When
                tokenFormatter.toPlaceRequest(placeToken);
                fail("TokenFormatException was expected for '" + placeToken + "'");
            } catch (Exception ex) {
                // Then
            }
        }
    }

    public void testToPlaceRequestIsReverseOfToPlaceToken() {

        PlaceRequest[] testCases = {
                PlaceRequest.of("token").with("=a=b=", "=c=d=").with("x", "y").build(),
                PlaceRequest.of("token").with("==a==b==", "==c==d==").with("x", "y").build(),
                PlaceRequest.of("token").with(";a;b;", ";c;d;").with("x", "y").build(),
                PlaceRequest.of("token").with(";;a;;b;;", ";;c;;d;;").with("x", "y").build(),
                PlaceRequest.of("token").with("/a/b/", "/c/d/").with("x", "y").build(),
                PlaceRequest.of("token").with("//a//b//", "//c//d//").with("x", "y").build(),
                PlaceRequest.of("token").with("\\a\\b\\", "\\c\\d\\").with("x", "y").build(),
                PlaceRequest.of("token").with("\\\\a\\\\b\\\\", "\\\\c\\\\d\\\\").with("x", "y")
                        .build()
        };

        for (PlaceRequest placeRequestA : testCases) {
            PlaceRequest placeRequestB = tokenFormatter.toPlaceRequest(
                    tokenFormatter.toPlaceToken(placeRequestA));

            for (String key : placeRequestB.getParameterNames()) {
                assertEquals(placeRequestA.getParameter(key, null), placeRequestB.getParameter(key, null));
            }

            assertEquals(placeRequestA.getParameterNames().size(), placeRequestB.getParameterNames().size());
        }
    }

    public void testToPlaceRequestIsReverseOfToPlaceTokenAfterFullUrlEncode() {

        PlaceRequest[] testCases = {
                PlaceRequest.of("token").with("=a=b=", "=c=d=").with("x", "y").build(),
                PlaceRequest.of("token").with("==a==b==", "==c==d==").with("x", "y").build(),
                PlaceRequest.of("token").with(";a;b;", ";c;d;").with("x", "y").build(),
                PlaceRequest.of("token").with(";;a;;b;;", ";;c;;d;;").with("x", "y").build(),
                PlaceRequest.of("token").with("/a/b/", "/c/d/").with("x", "y").build(),
                PlaceRequest.of("token").with("//a//b//", "//c//d//").with("x", "y").build(),
                PlaceRequest.of("token").with("\\a\\b\\", "\\c\\d\\").with("x", "y").build(),
                PlaceRequest.of("token").with("\\\\a\\\\b\\\\", "\\\\c\\\\d\\\\").with("x", "y")
                        .build()
        };

        for (PlaceRequest placeRequestA : testCases) {
            PlaceRequest placeRequestB = tokenFormatter.toPlaceRequest(
                    urlEncodeEverything(tokenFormatter.toPlaceToken(placeRequestA)));

            for (String key : placeRequestB.getParameterNames()) {
                assertEquals(placeRequestA.getParameter(key, null), placeRequestB.getParameter(key, null));
            }

            assertEquals(placeRequestA.getParameterNames().size(), placeRequestB.getParameterNames().size());
        }
    }
    //------------------------------------------
    // Tests for toPlaceToken
    //------------------------------------------

    public void testToPlaceTokenValidPlaceRequest() {
        // Given
        String expectedPlaceToken = "token;a=b";
        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken("token").with("a", "b").build();

        // When
        String placeToken = tokenFormatter.toPlaceToken(placeRequest);

        // Then
        assertEquals(expectedPlaceToken, placeToken);
    }

    public void testToPlaceTokenShouldEscapeSeparators() {
        // Given
        String expectedPlaceToken = "token;%5C1c%5C2=%5C2d%5C1;%5C1a%5C2=%5C2b";
        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken("token").with(";c=", "=d;").with(";a=", "=b")
                .build();

        // When
        String placeToken = tokenFormatter.toPlaceToken(placeRequest);

        // Then
        assertEquals(expectedPlaceToken, placeToken);
    }

    public void testToPlaceTokenIsReverseOfToPlaceRequest() {
        // Setup
        Map<String, String> params = new HashMap<>();
        params.put(" a b ", " c d ");
        params.put("a", "b=c=d");
        params.put("a=b", "c=d");
        params.put("a=b=c", "d");
        params.put("=a", "b");
        params.put("a=", "b");
        params.put("a", "b=");
        params.put("a", "=b");
        params.put("a", "b;c;d");
        params.put("a;b", "c;d");
        params.put("a;b;c", "d");
        params.put(";;a", "b");
        params.put("a", "b;;");
        params.put("a", "b//c//d");
        params.put("a//b", "c//d");
        params.put("a//b//c", "d");
        params.put("//a", "d");
        params.put("a", "d//");
        params.put("a", "b\\\\c\\\\d");
        params.put("a\\\\b", "c\\\\d");
        params.put("a\\\\b\\\\c", "d");
        params.put("\\\\a", "d");
        params.put("a", "d\\\\");

        // Given
        ArrayList<String> testPlaceTokens = new ArrayList<>();

        for (Entry<String, String> entry : params.entrySet()) {
            // Escape separators
            String placeToken = "token;"
                    + tokenFormatter.customEscape(entry.getKey()) + "=" + tokenFormatter.customEscape(entry.getValue());

            testPlaceTokens.add(placeToken);
        }

        for (String placeRequestA : testPlaceTokens) {
            // When
            String placeRequestB = tokenFormatter.toPlaceToken(
                    tokenFormatter.toPlaceRequest(placeRequestA));

            // Then
            assertEquals(placeRequestA, placeRequestB);
        }
    }
}
