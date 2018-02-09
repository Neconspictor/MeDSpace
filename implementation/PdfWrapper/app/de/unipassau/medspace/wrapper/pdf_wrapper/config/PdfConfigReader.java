package de.unipassau.medspace.wrapper.pdf_wrapper.config;

import de.unipassau.medspace.common.util.XmlUtil;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.parsing.RootParsing;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;

/**
 * TODO
 */
public class PdfConfigReader {

  private final String pdfConfigSpecificationSchema;

  public PdfConfigReader(String pdfConfigSpecificationSchema) {
    this.pdfConfigSpecificationSchema = pdfConfigSpecificationSchema;
  }


  /**
   * TODO
   * @param fileName
   * @throws JAXBException
   * @throws IOException
   * @throws SAXException
   */
  public RootParsing parse(String fileName) throws JAXBException, IOException, SAXException {
    JAXBContext context = JAXBContext.newInstance(RootParsing.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    Schema schema = XmlUtil.createSchema(new String[]{pdfConfigSpecificationSchema});
    unmarshaller.setSchema(schema);
    return (RootParsing) unmarshaller.unmarshal(new File(fileName));
  }
}
