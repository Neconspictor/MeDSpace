package de.unipassau.medspace.common.play;

import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.wrapper.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * TODO
 */
public class WrapperService {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(WrapperService.class);

  /**
   * The general wrapper configuration.
   */
  protected final GeneralWrapperConfig generalConfig;

  /**
   * The  wrapper.
   */
  protected final Wrapper wrapper;

  public WrapperService(GeneralWrapperConfig generalConfig,
                        Wrapper wrapper) {

    this.generalConfig = generalConfig;
    this.wrapper = wrapper;
  }

  /**
   * Provides the wrapper.
   * @return The wrapper.
   */
  public Wrapper getWrapper() {
    return wrapper;
  }


  /**
   * Performs a keyword search on the underlying datasource or on the index if one is used.
   * @param keywords The keywords to keywordSearch for.
   * @param operator TODO
   * @return A stream of rdf triples representing the success of the keyword keywordSearch.
   * @throws IOException If an IO-Error occurs.
   * @throws NoValidArgumentException If 'keywords' is null.
   */
  public Stream<Triple> keywordSearch(String keywords, KeywordSearcher.Operator operator) throws IOException, NoValidArgumentException {

    if (keywords == null) {
      throw new NoValidArgumentException("keywords mustn't be null");
    }

    StringTokenizer tokenizer = new StringTokenizer(keywords, ", ", false);
    List<String> keywordList = new ArrayList<>();

    while(tokenizer.hasMoreTokens()) {
      keywordList.add(tokenizer.nextToken());
    }

    KeywordSearcher<Triple> searcher = wrapper.createKeywordSearcher(operator);
    return searcher.searchForKeywords(keywordList);
  }
}