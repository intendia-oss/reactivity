package com.intendia.reactivity.client;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public class PlaceRequest {
    private final String nameToken;
    private final Map<String, String> params;

    private PlaceRequest(String nameToken, Map<String, String> params) {
        this.nameToken = requireNonNull(nameToken, "nameToken required");
        this.params = params == null ? Collections.emptyMap() : params;
    }

    public String getNameToken() { return nameToken; }

    public String getParameter(String key, String defaultValue) { return params.getOrDefault(key, defaultValue); }

    public Set<String> getParameterNames() { return params.keySet(); }

    public boolean matchesNameToken(PlaceRequest other) { return matchesNameToken(other.nameToken); }

    public boolean matchesNameToken(String nameToken) { return this.nameToken.equals(nameToken); }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof PlaceRequest)) return false;
        PlaceRequest req = (PlaceRequest) obj;
        if (nameToken == null || req.nameToken == null) return false;
        if (!nameToken.equals(req.nameToken)) return false;
        if (params == null) return req.params == null;
        return params.equals(req.params);
    }

    @Override public int hashCode() { return 11 * (nameToken.hashCode() + (params == null ? 0 : params.hashCode())); }

    @Override public String toString() { return "PlaceRequest(nameToken=" + nameToken + ", params=" + params + ")"; }

    public static Builder of(String nameToken) { return new Builder().nameToken(nameToken); }

    public static final class Builder {
        private String nameToken = "";
        private final Map<String, String> params = new LinkedHashMap<>();

        public Builder() {}
        public Builder(PlaceRequest request) {
            nameToken = request.nameToken;
            with(request.params);
        }

        public Builder nameToken(String nameToken) { this.nameToken = requireNonNull(nameToken); return this; }

        public Builder with(String name, @Nullable String value) { this.params.put(name, value); return this; }

        public Builder with(Map<String, String> params) { this.params.putAll(params); return this; }

        public Builder without(String name) { params.remove(name); return this; }

        public PlaceRequest build() { return new PlaceRequest(nameToken, params); }
    }
}
