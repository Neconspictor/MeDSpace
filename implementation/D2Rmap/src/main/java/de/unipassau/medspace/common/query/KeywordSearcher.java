package de.unipassau.medspace.common.query;

import de.unipassau.medspace.common.stream.DataSourceStream;

import java.io.IOException;
import java.util.List;

/**
 * Created by David Goeth on 07.07.2017.
 */
public interface KeywordSearcher<E> {

  DataSourceStream<E> searchForKeywords(List<String> keywords) throws IOException;
}
