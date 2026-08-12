/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.urlparser;

import com.hitorro.util.core.string.StringUtilCore;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * URL utility methods for parsing, cleanup, and extraction.
 */
public class URLUtil {

    public static final String getSiteFromURL(String url) {
        UrlCursor curs = new UrlCursor();
        return getSiteFromURL(url, curs);
    }

    public static final String getSiteFromURL(String url, UrlCursor curs) {
        curs.setUrl(url);
        curs.nextToken();
        return curs.getAllToCurrentPos();
    }

    public static final String cleanupUrl(String url) {
        try {
            url = URLDecoder.decode(url, StandardCharsets.UTF_8);
            return new URL(url).toString();
        } catch (MalformedURLException mue) {
            return url;
        } catch (IllegalArgumentException e) {
            return url;
        }
    }

    public static final String cleanupUrlAndCutToLength(String url, int length) {
        return StringUtilCore.truncateToLength(cleanupUrl(url), length);
    }

    /**
     * Validates whether a string looks like a valid URL with protocol and host.
     */
    public static boolean isValidUrl(String url) {
        if (StringUtilCore.nullOrEmptyString(url)) {
            return false;
        }
        try {
            URL u = new URL(url);
            return u.getHost() != null && !u.getHost().isEmpty();
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Extract query parameters from a URL as a map.
     * Duplicate keys are overwritten (last wins).
     */
    public static Map<String, String> getQueryParameters(String url) {
        Map<String, String> params = new LinkedHashMap<>();
        if (StringUtilCore.nullOrEmptyString(url)) {
            return params;
        }
        int qIndex = url.indexOf('?');
        if (qIndex == -1 || qIndex == url.length() - 1) {
            return params;
        }
        String query = url.substring(qIndex + 1);
        // strip fragment
        int hashIndex = query.indexOf('#');
        if (hashIndex != -1) {
            query = query.substring(0, hashIndex);
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eqIndex = pair.indexOf('=');
            if (eqIndex > 0) {
                String key = URLDecoder.decode(pair.substring(0, eqIndex), StandardCharsets.UTF_8);
                String value = eqIndex < pair.length() - 1
                        ? URLDecoder.decode(pair.substring(eqIndex + 1), StandardCharsets.UTF_8)
                        : "";
                params.put(key, value);
            } else if (!pair.isEmpty()) {
                params.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
            }
        }
        return params;
    }

    /**
     * Extract the host portion of a URL.
     */
    public static final String getHost(String url) {
        if (StringUtilCore.nullOrEmptyString(url)) {
            return null;
        }
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Extract the path portion of a URL.
     */
    public static final String getPath(String url) {
        if (StringUtilCore.nullOrEmptyString(url)) {
            return null;
        }
        try {
            return new URL(url).getPath();
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
