package com.intendia.reactivity.client;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RouteTokenFormatter implements TokenFormatter {
    private static class RouteMatch implements Comparable<RouteMatch> {
        final String route;
        final int nestLevel;
        final Map<String, String> parameters;

        RouteMatch(String route, int nestLevel, Map<String, String> parameters) {
            this.route = route; this.nestLevel = nestLevel; this.parameters = parameters;
        }

        @Override
        public int compareTo(@Nullable RouteMatch other) {
            return other == null ? -1 : Integer.compare(nestLevel, other.nestLevel);
        }
    }

    private final UrlUtils urlUtils;
    private final Set<Place> places;

    @Inject RouteTokenFormatter(UrlUtils urlUtils, Set<Place> places) {
        this.urlUtils = urlUtils;
        this.places = places;
    }

    @Override
    public String toPlaceToken(PlaceRequest placeRequest) throws TokenFormatException {
        String placeToken = placeRequest.getNameToken();
        Stream.Builder<String> queryStringBuilder = Stream.builder();

        for (String parameterName : placeRequest.getParameterNames()) {
            String parameterValue = placeRequest.getParameter(parameterName, null);
            if (parameterValue != null) {
                String encodedParameterValue = urlUtils.encodeQueryString(parameterValue);
                boolean routeParam = placeToken.contains("/{" + parameterName + "}");
                if (routeParam) placeToken = placeToken.replace("{" + parameterName + "}", encodedParameterValue);
                else queryStringBuilder.add(parameterName + "=" + encodedParameterValue);
            }
        }

        String queryString = queryStringBuilder.build().collect(Collectors.joining("&"));
        return queryString.isEmpty() ? placeToken : placeToken + "?" + queryString;
    }

    @Override
    public PlaceRequest toPlaceRequest(String placeToken) throws TokenFormatException {
        // To support the native GWT history as well as HTML push-state a slash is added when needed
        if (!placeTokenIsValid(placeToken)) return toPlaceRequest("/" + placeToken);

        int split = placeToken.indexOf('?');
        String place = (split != -1) ? placeToken.substring(0, split) : placeToken;
        String query = (split != -1) ? placeToken.substring(split + 1) : "";

        TreeSet<RouteMatch> allMatches = matchRoute(place);
        RouteMatch match = (!allMatches.isEmpty()) ? allMatches.last() : new RouteMatch(place, 0, emptyMap());

        return PlaceRequest.of(match.route).with(parseQueryString(query, match.parameters)).build();
    }

    /** Parse the given query-string and store all parameters into a map. */
    Map<String, String> parseQueryString(String queryString, Map<String, String> into) {
        Map<String, String> result = new HashMap<>(into);

        if (queryString != null && !queryString.isEmpty()) {
            for (String keyValuePair : queryString.split("&")) {
                String[] keyValue = keyValuePair.split("=", 2);
                result.put(keyValue[0], keyValue.length <= 1 ? "" : urlUtils.decodeQueryString(keyValue[1]));
            }
        }

        return result;
    }

    private boolean placeTokenIsValid(String placeToken) {
        return placeToken.startsWith("/") || placeToken.startsWith("!/");
    }

    TreeSet<RouteMatch> matchRoute(String placeToken) {
        assert placeTokenIsValid(placeToken) : "Place-token should start with a '/' or '!/'";
        assert placeToken.indexOf('?') == -1 : "No Query string expected here";
        String[] placeParts = placeToken.split("/");
        return places.stream()
                .map(p -> matchRoute(p.getNameToken(), placeParts))
                .filter(Objects::nonNull).collect(toCollection(TreeSet::new));
    }

    @Nullable RouteMatch matchRoute(String route, String[] placeParts) {
        String[] routeParts = route.split("/");

        if (placeParts.length != routeParts.length) return null;
        if (placeParts.length == 0) return new RouteMatch(route, 0, emptyMap());

        Map<String, String> ps = new HashMap<>();
        int nestLevel = 0;
        for (int i = 0; i < placeParts.length; i++) {
            String placePart = placeParts[i], routePart = routeParts[i];
            if (placePart.equals(routePart)) nestLevel++;
            else if (routePart.matches("\\{.*\\}")) {
                String paramName = routePart.substring(1, routePart.length() - 1);
                String paramValue = urlUtils.decodeQueryString(placePart);
                ps.put(paramName, paramValue);
            } else return null;
        }

        return new RouteMatch(route, nestLevel, ps);
    }
}
