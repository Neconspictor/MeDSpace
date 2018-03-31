package de.unipassau.medspace.global.config;

import de.unipassau.medspace.common.util.XmlUtil;
import de.unipassau.medspace.global.config.mapping.ConfigMapping;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;

/**
 * Created by David Goeth on 3/31/2018.
 */
public class GlobalMeDSpaceConfigReader {

  private final String globalServerConfigSpecificationFile;

  /**
   * Creates a new PdfConfigReader object.
   * @param globalServerConfigSpecificationFile The XSD validation schema for the global MeDSpace configuration.
   */
  public GlobalMeDSpaceConfigReader(String globalServerConfigSpecificationFile) {
    this.globalServerConfigSpecificationFile = globalServerConfigSpecificationFile;
  }


  /**
   * Parses the PDF wrapper RDF mappings from a given file.
   *
   * @param fileName The file to parse.
   * @throws JAXBException If an error related to JAXB occurs.
   * @throws IOException If an IO error occurs.
   * @throws SAXException If an error related to XML parsing occurs.
   */
  public ConfigMapping parse(String fileName) throws JAXBException, IOException, SAXException {
    JAXBContext context = JAXBContext.newInstance(ConfigMapping.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    Schema schema = XmlUtil.createSchema(new String[]{globalServerConfigSpecificationFile});
    unmarshaller.setSchema(schema);
    return (ConfigMapping) unmarshaller.unmarshal(new File(fileName));
  }
}