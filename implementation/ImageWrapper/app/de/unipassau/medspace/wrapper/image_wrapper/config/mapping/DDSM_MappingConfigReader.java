package de.unipassau.medspace.wrapper.image_wrapper.config.mapping;

import de.unipassau.medspace.common.config.Constants;
import de.unipassau.medspace.common.util.XmlUtil;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;

/**
 * A reader for the DDSM maping configuration file.
 */
public class DDSM_MappingConfigReader {

  private final String mappingSpecificationSchema;

  /**
   * Creates a new DDSM_MappingConfigReader.
   * @param mappingSpecificationSchema The validation XSD file to use.
   */
  public DDSM_MappingConfigReader(String mappingSpecificationSchema) {
    this.mappingSpecificationSchema = mappingSpecificationSchema;
  }


  /**
   * Parses a DDSM maping configuration file.
   * @param fileName The DDSM maping configuration file.
   * @throws JAXBException If an error occurs regarding JAXB-
   * @throws IOException If an IO error occurs.
   * @throws SAXException If an XML error occurs.
   */
  public RootMapping parse(String fileName) throws JAXBException, IOException, SAXException {
    JAXBContext context = JAXBContext.newInstance(RootMapping.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();

    // RDF_MAPPING_SCHEMA is a dependency for mappingSpecificationSchema, so it has to be stated at the first place
    Schema schema = XmlUtil.createSchema(new String[]{Constants.RDF_MAPPING_SCHEMA, mappingSpecificationSchema});
    unmarshaller.setSchema(schema);
    return (RootMapping) unmarshaller.unmarshal(new File(fileName));
  }
}