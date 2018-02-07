package de.unipassau.medspace.wrapper.image_wrapper;

import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4J_RDFProvider;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.RdfUtil;
import de.unipassau.medspace.wrapper.image_wrapper.config.DDSMConfigReader;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.NamespaceParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.RootParsing;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFileCollectorStream;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.Util;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.FlatMapStream;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.IcsFileStreamToDocStream;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.LuceneIndexFactory;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter.*;
import org.apache.lucene.document.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 */
public class Test {

  /**
   * TODO
   */
  public static final String DDSM_CONFIG_FILE = "./medspace/medspace-ddsm-mapping.xml";

  /**
   * TODO
   */
  public static String IMAGE_FILE_ENDING = "png";

  /**
   * TODO
   */
  public static File root = new File("F:\\bachelorThesis\\DDSM\\WinSCP-Test\\cases\\cases");

  /**
   * TODO
   */
  private static String INDEX_DIRECTORY = "./_work/index/lucene/";

  /**
   * TODO
   * @throws JAXBException
   * @throws IOException
   * @throws SAXException
   */
  public static void main(String[] args) throws JAXBException, IOException, SAXException {

    DDSMConfigReader configReader = new DDSMConfigReader();
    RootParsing rootParsing = configReader.parse(DDSM_CONFIG_FILE);

    DDSM_AdapterFactory adapterFactory = new DDSM_AdapterFactory(rootParsing);
    List<LuceneDocAdapter<?>> adapters = adapterFactory.createAdapters();

    Map<String, Namespace> namespaces = new HashMap<>();

    for (NamespaceParsing parsedNamespace :  rootParsing.getNamespace()) {
      String prefix = parsedNamespace.getPrefix().trim();
      String fullURI = parsedNamespace.getNamespace().trim();
      Namespace namespace = new Namespace(prefix, fullURI);
      namespaces.put(prefix, namespace);
    }

    QNameNormalizer normalizer = qName -> RdfUtil.getNormalizedURI(namespaces, qName);;
    RDFFactory factory = new RDF4J_RDFProvider().getFactory();

    LuceneIndexFactory indexFactory = new LuceneIndexFactory(INDEX_DIRECTORY,
        adapters,
        factory,
        normalizer);

    TripleIndexManager<Document, IcsFile> tripleIndexManager = indexFactory.createIndexManager();
    /*Stream<Document> documentStream = tripleIndexManager.convert(stream);



    Document document = null;
    while (documentStream.hasNext()) {
      document = documentStream.next();
      if (document.getField("TOTAL_ABNORMALITIES") != null) {
        String value = document.getField("TOTAL_ABNORMALITIES").stringValue();
      }
    }*/


    DDSM_ImageWrapper<Document> imageWrapper = new DDSM_ImageWrapper<>(tripleIndexManager,
        IMAGE_FILE_ENDING,
        namespaces,
        root);

    imageWrapper.reindexData();

    /*DocumentClassTriplizer triplizer = new DocumentClassTriplizer(adapters, normalizer, factory);

    while(documentStream.hasNext()) {
      Document document = documentStream.next();
      List<Triple> triples = triplizer.convert(document);
    }*/
  }
}