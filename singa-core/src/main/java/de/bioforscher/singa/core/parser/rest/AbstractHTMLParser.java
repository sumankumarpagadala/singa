package de.bioforscher.singa.core.parser.rest;

import de.bioforscher.singa.core.parser.AbstractParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * This class allows handling and parameter definition for simple HTML requests.
 *
 * @author cl
 */
public abstract class AbstractHTMLParser<ResultType> extends AbstractParser<ResultType> {

    public void fetchResource(String resource) {
        // encode string to utf and connect to resource
        String urlString;
        try {
            urlString = getResource() + URLEncoder.encode(resource, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException("Could not encode query \""+ resource+"\"", e);
        }
        // open url and fetch result
        try {
            URL url = new URL(urlString);
            setFetchResult(url.openStream());
        } catch (MalformedURLException e) {
            throw new UncheckedIOException("The url \""+ urlString +"\" seems to be malformed", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not connect to \""+ urlString +"\", the server seems to be unavailable.", e);
        }
    }

    /**
     * Builds a {@code Client} and {@code WebTarget} with the specified parameters in the query map and sets the
     * input stream as a fetch result.
     */
    public void fetchWithQuery(Map<String, String> queryMap) {
        // build query
        StringBuilder query = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = queryMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            query.append(entry.getKey())
                    .append('=')
                    .append(entry.getValue());
            // connect request parameters
            if (iterator.hasNext()) {
                query.append('&');
            }
        }
        fetchResource(query.toString());
    }

}
