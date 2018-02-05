package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene;

import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.Converter;
import org.apache.lucene.document.Document;

import java.io.IOException;

/**
 * TODO
 */
public class DocConverterStream<E> implements Stream<Document> {

  private final Stream<E> source;
  private final Converter<E, Document> converter;

  public DocConverterStream(Stream<E> source, Converter<E, Document> converter) {
    this.source = source;
    this.converter = converter;
  }


  @Override
  public Document next() throws IOException {
    E elem = source.next();
    return converter.convert(elem);
  }

  @Override
  public boolean hasNext() throws IOException {
    return source.hasNext();
  }

  @Override
  public void close() throws IOException {
    source.close();
  }
}