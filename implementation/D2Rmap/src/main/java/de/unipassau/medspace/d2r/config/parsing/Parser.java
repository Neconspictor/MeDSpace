package de.unipassau.medspace.d2r.config.parsing;

import de.unipassau.medspace.common.util.XmlUtil;
import de.unipassau.medspace.d2r.D2R;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;

/**
 * TODO
 */
public class Parser {

  /**
   * TODO
   * @param fileName
   * @return
   * @throws JAXBException
   */
  public RootParsing parse(String fileName) throws JAXBException, SAXException, IOException {
    JAXBContext context = JAXBContext.newInstance(RootParsing.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    Schema schema = XmlUtil.createSchema(new String[]{D2R.MEDSPACE_VALIDATION_SCHEMA});
    unmarshaller.setSchema(schema);
    return (RootParsing) unmarshaller.unmarshal(new File(fileName));
  }
}