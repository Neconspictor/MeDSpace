//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.04.02 at 12:36:13 AM CEST 
//


package de.unipassau.medspace.wrapper.image_wrapper.config.mapping;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.unipassau.medspace.wrapper.image_wrapper.config.mapping package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.unipassau.medspace.wrapper.image_wrapper.config.mapping
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RootMapping }
     * 
     */
    public RootMapping createRootMapping() {
        return new RootMapping();
    }

    /**
     * Create an instance of {@link MassMapping }
     * 
     */
    public MassMapping createMassMapping() {
        return new MassMapping();
    }

    /**
     * Create an instance of {@link CalcificationMapping }
     * 
     */
    public CalcificationMapping createCalcificationMapping() {
        return new CalcificationMapping();
    }

    /**
     * Create an instance of {@link IcsFileMapping }
     * 
     */
    public IcsFileMapping createIcsFileMapping() {
        return new IcsFileMapping();
    }

    /**
     * Create an instance of {@link ImageMapping }
     * 
     */
    public ImageMapping createImageMapping() {
        return new ImageMapping();
    }

    /**
     * Create an instance of {@link OverlayMapping }
     * 
     */
    public OverlayMapping createOverlayMapping() {
        return new OverlayMapping();
    }

    /**
     * Create an instance of {@link AbnormalityMapping }
     * 
     */
    public AbnormalityMapping createAbnormalityMapping() {
        return new AbnormalityMapping();
    }

}
