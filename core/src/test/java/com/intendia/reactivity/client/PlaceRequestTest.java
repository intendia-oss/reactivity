package com.intendia.reactivity.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class PlaceRequestTest {
    @Test
    public void shouldBuildEmptyRequest() {
        // when
        PlaceRequest request = new PlaceRequest.Builder().build();

        // then
        assertNotNull(request);
        assertEquals(request.getNameToken(), "");
        Set<String> emptySet = Collections.emptySet();
        assertEquals(request.getParameterNames(), emptySet);
    }

    @Test
    public void shouldBuildRequestWithSeveralParameters() {
        // when
        PlaceRequest request = new PlaceRequest.Builder().nameToken("nameToken").with("name1", "value1")
                .with("name2", "value2").build();

        // then
        assertNotNull(request);
        assertEquals("nameToken", request.getNameToken());
        assertEquals("value1", request.getParameter("name1", ""));
        assertEquals("value2", request.getParameter("name2", ""));
    }

    @Test
    public void shouldBuildRequestWithParameterMap() {
        // given
        Map<String, String> existingParameters = new HashMap<>();
        existingParameters.put("name1", "value1");
        existingParameters.put("name2", "value2");

        // when
        PlaceRequest request = new PlaceRequest.Builder().nameToken("nameToken").with(existingParameters)
                .with("name3", "value3").build();

        // then
        assertNotNull(request);
        assertEquals("nameToken", request.getNameToken());
        assertEquals("value1", request.getParameter("name1", ""));
        assertEquals("value2", request.getParameter("name2", ""));
        assertEquals("value3", request.getParameter("name3", ""));
    }

    @Test
    public void shouldBuildRequestFromExistingRequest() {
        // given
        PlaceRequest request = PlaceRequest.of("nameToken").build();

        // when
        PlaceRequest copyOfRequest = new PlaceRequest.Builder(request).build();

        // then
        assertEquals(request, copyOfRequest);
    }

    @Test
    public void testToString() {
        // given
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken("nameToken")
                .with("name1", "value1")
                .with("name2", "value2")
                .build();

        // when
        String result = request.toString();

        // then
        assertNotNull(result);
        assertEquals("PlaceRequest(nameToken=nameToken, params={name1=value1, name2=value2})", result);
    }

    @Test
    public void builderFromPlaceRequestShouldNotShareParams() {
        // given
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken("nameToken")
                .with("name1", "value1")
                .with("name2", "value2")
                .build();
        PlaceRequest.Builder copyBuilder = new PlaceRequest.Builder(request);

        // when
        copyBuilder.with("name3", "value3").build();

        // then
        assertNull(request.getParameter("name3", null));
    }

    @Test
    public void builderWithoutDoesRemoveParam() {
        // given
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken("nameToken")
                .with("name1", "value1")
                .with("name2", "value2")
                .build();
        PlaceRequest.Builder copyBuilder = new PlaceRequest.Builder(request);

        // when
        PlaceRequest copy = copyBuilder.without("name2").build();

        // then
        assertNull(copy.getParameter("name2", null));
    }
}
