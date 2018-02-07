package de.unipassau.medspace.wrapper.image_wrapper.rdf_mapping;

import de.unipassau.medspace.common.rdf.*;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.DataTypePropertyParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TODO
 */
public class Util {

  /**
   * TODO
   *
   * @param normalizer
   * @param baseURI
   * @param id
   * @return
   * @throws IllegalArgumentException
   */
  public static String createResourceId(QNameNormalizer normalizer, String baseURI, String id) {
    String subject;
    try {
      subject = baseURI + "#" + URLEncoder.encode(id, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Couldn't encode id", e);
    }

    return normalizer.normalize(subject);
  }


  /**
   * TODO
   * @param propertyParsing
   * @param subject
   * @param value
   * @return
   */
  public static Triple triplize(RDFFactory factory,
                                QNameNormalizer normalizer,
                                Property propertyParsing,
                                RDFResource subject,
                                String value) {

    // create property
    RDFResource property = createProperty(factory, normalizer, propertyParsing);

    // create object
    RDFObject object;
    if (propertyParsing instanceof DataTypePropertyParsing) {
      object = Util.createLiteral(factory,
          normalizer,
          (DataTypePropertyParsing) propertyParsing,
          value);
    } else {
      object = factory.createResource(value);
    }

    return factory.createTriple(subject, property, object);
  }

  /**
   * TODO
   * @param value
   * @param propertyParsing
   * @return
   */
  public static RDFObject createLiteral(RDFFactory factory,
                                        QNameNormalizer normalizer,
                                        DataTypePropertyParsing propertyParsing,
                                        String value) {

    String dataType = propertyParsing.getDataType();
    String lang = propertyParsing.getLang();
    RDFObject object;

    // The lang tag specifies indirectly the dataType (rdf:lang)
    // Thus the lang tag has a higher priority than the dataType tag
    if ((value != null) && (lang != null)) {
      object = factory.createLiteral(value, lang);
    } else if ((value != null) && (dataType != null)) {
      // if no lang tag is set but the dataType tag createDoc a typed literal
      dataType = normalizer.normalize(dataType);
      object = factory.createTypedLiteral(value, dataType);
    }  else {
      // no lang tag and dataType set; assume xsd:string is the data type
      object = factory.createLiteral(value);
    }
    return object;
  }

  /**
   * TODO
   * @param propertyParsing
   * @return
   */
  public static RDFResource createProperty(RDFFactory factory,
                                           QNameNormalizer normalizer,
                                           Property propertyParsing) {
    String propertyQName = propertyParsing.getPropertyType();
    String propURI = normalizer.normalize(propertyQName);
    return factory.createResource(propURI);
  }

  /**
   * TODO
   * @param date
   * @return
   */
  public static String format(Date date) {
    SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy");
    return formater.format(date);
  }
}