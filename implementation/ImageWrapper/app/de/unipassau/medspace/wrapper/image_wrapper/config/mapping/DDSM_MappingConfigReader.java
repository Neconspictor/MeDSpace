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
 * TODO
 */
public class DDSM_MappingConfigReader {

  private final String mappingSpecificationSchema;

  /**
   * TODO
   * @param mappingSpecificationSchema
   */
  public DDSM_MappingConfigReader(String mappingSpecificationSchema) {
    this.mappingSpecificationSchema = mappingSpecificationSchema;
  }


  /**
   * TODO
   * @param fileName
   * @throws JAXBException
   * @throws IOException
   * @throws SAXException
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