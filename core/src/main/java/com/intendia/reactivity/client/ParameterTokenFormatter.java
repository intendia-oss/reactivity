package com.intendia.reactivity.client;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ParameterTokenFormatter implements TokenFormatter {

    protected static final char PARAM_SEPARATOR = ';';
    protected static final char VALUE_SEPARATOR = '=';

    // Escaped versions of the above.
    protected static final char ESCAPE_CHARACTER = '\\';
    protected static final String ESCAPED_PARAM_SEPARATOR = "\\1";
    protected static final String ESCAPED_VALUE_SEPARATOR = "\\2";
    protected static final String ESCAPED_ESCAPE_CHAR = "\\3";

    private final UrlUtils urlUtils;

    @Inject public ParameterTokenFormatter(UrlUtils urlUtils) {
        this.urlUtils = urlUtils;
    }

    @Override
    public PlaceRequest toPlaceRequest(String placeToken) throws TokenFormatException {
        String unescapedPlaceToken = urlUtils.decodeQueryString(placeToken);
        int split = unescapedPlaceToken.indexOf(PARAM_SEPARATOR);
        if (split == -1) return PlaceRequest.of(customUnescape(unescapedPlaceToken)).build(); // no parameters
        if (split == 0) throw new TokenFormatException("Place history token is missing."); // error

        PlaceRequest.Builder reqBuilder = PlaceRequest.of(customUnescape(unescapedPlaceToken.substring(0, split)));
        String paramsChunk = unescapedPlaceToken.substring(split + 1);
        String[] paramTokens = paramsChunk.split(String.valueOf(PARAM_SEPARATOR));
        for (String paramToken : paramTokens) {
            if (paramToken.isEmpty()) {
                throw new TokenFormatException("Bad parameter: Successive parameters require a single '" +
                        PARAM_SEPARATOR + "' between them.");
            }
            String[] param = paramToken.split(String.valueOf(VALUE_SEPARATOR));
            if (param.length == 1) {
                // If there is only one parameter, then we need an '=' at the last position.
                if (paramToken.charAt(paramToken.length() - 1) != VALUE_SEPARATOR) {
                    throw new TokenFormatException("Bad parameter: Need exactly one key and one value.");
                }
            } else if (param.length == 2) {
                // If there are two parameters, then there must not be a '=' at the last position.
                if (paramToken.charAt(paramToken.length() - 1) == VALUE_SEPARATOR) {
                    throw new TokenFormatException("Bad parameter: Need exactly one key and one value.");
                }
            } else {
                throw new TokenFormatException("Bad parameter: Need exactly one key and one value.");
            }
            String key = customUnescape(param[0]);
            String value = param.length == 2 ? customUnescape(param[1]) : "";
            reqBuilder.with(key, value);
        }
        return reqBuilder.build();
    }

    /**
     * Use our custom escaping mechanism to unescape the provided string. This should be used on the
     * name token, and the parameter keys and values, after they have been split using the various
     * separators. The input string is expected to already be sent through
     * {@link UrlUtils#decodeQueryString}.
     */
    private String customUnescape(String string) throws TokenFormatException {
        StringBuilder builder = new StringBuilder();
        int len = string.length();

        char paramNum = ESCAPED_PARAM_SEPARATOR.charAt(1);
        char valueNum = ESCAPED_VALUE_SEPARATOR.charAt(1);
        char escapeNum = ESCAPED_ESCAPE_CHAR.charAt(1);

        int i = 0;
        while (i < len - 1) {
            char ch = string.charAt(i);
            if (ch == ESCAPE_CHARACTER) {
                i++;
                char ch2 = string.charAt(i);
                if (ch2 == paramNum) builder.append(PARAM_SEPARATOR);
                else if (ch2 == valueNum) builder.append(VALUE_SEPARATOR);
                else if (ch2 == escapeNum) builder.append(ESCAPE_CHARACTER);
            } else {
                builder.append(ch);
            }
            i++;
        }
        if (i == len - 1) {
            char ch = string.charAt(i);
            if (ch == ESCAPE_CHARACTER) {
                throw new TokenFormatException("Last character of string being unescaped cannot be '" +
                        ESCAPE_CHARACTER + "'.");
            }
            builder.append(ch);
        }
        return builder.toString();
    }

    @Override
    public String toPlaceToken(PlaceRequest placeRequest) throws TokenFormatException {
        StringBuilder out = new StringBuilder();
        out.append(customEscape(placeRequest.getNameToken()));
        for (String name : placeRequest.getParameterNames()) {
            out.append(PARAM_SEPARATOR).append(customEscape(name))
                    .append(VALUE_SEPARATOR).append(customEscape(placeRequest.getParameter(name, null)));
        }

        return out.toString();
    }

    /**
     * Use our custom escaping mechanism to escape the provided string. This should be used on the
     * name token, and the parameter keys and values, before they are attached with the various
     * separators. The string will also be passed through {@link UrlUtils#encodeQueryString}.
     * Visible for testing.
     */
    String customEscape(String string) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, len = string.length(); i < len; i++) {
            char ch = string.charAt(i);
            if (ch == ESCAPE_CHARACTER) builder.append(ESCAPED_ESCAPE_CHAR);
            else if (ch == PARAM_SEPARATOR) builder.append(ESCAPED_PARAM_SEPARATOR);
            else if (ch == VALUE_SEPARATOR) builder.append(ESCAPED_VALUE_SEPARATOR);
            else builder.append(ch);
        }
        return urlUtils.encodeQueryString(builder.toString());
    }
}
