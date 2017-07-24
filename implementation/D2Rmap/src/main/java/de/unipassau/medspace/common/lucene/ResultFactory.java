package de.unipassau.medspace.common.lucene;

import org.apache.lucene.document.Document;

/**
 * Created by David Goeth on 24.07.2017.
 */
public interface ResultFactory<E> {

  Document create(E elem);

  E create(Document doc);
}