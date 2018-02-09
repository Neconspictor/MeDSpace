package de.unipassau.medspace.wrapper.pdf_wrapper;

import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4J_RDFProvider;
import de.unipassau.medspace.common.util.RdfUtil;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.PdfConfigReader;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.parsing.NamespaceParsing;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.parsing.RootParsing;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFile;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.LuceneIndexFactory;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter.*;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
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
  public static final String PDF_CONFIG_FILE = "./medspace/medspace-pdf-wrapper-config.xml";

  /**
   * TODO
   */
  public static final String PDF_CONFIG_FILE_SPEC = "./medspace/medspace-pdf-wrapper-config-specification.xsd";

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

    PdfConfigReader configReader = new PdfConfigReader(PDF_CONFIG_FILE_SPEC);
    RootParsing rootParsing = configReader.parse(PDF_CONFIG_FILE);

    Pdf_AdapterFactory adapterFactory = new Pdf_AdapterFactory(rootParsing);
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

    TripleIndexManager<Document, PdfFile> tripleIndexManager = indexFactory.createIndexManager();


    File root = new File(rootParsing.getPdfRootDirectory());
    FileUtils.forceMkdir(root);

    PdfWrapper<Document> pdfWrapper = new PdfWrapper<>(tripleIndexManager,
        namespaces,
        root);

    pdfWrapper.reindexData();
  }
}