//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.03.18 at 05:59:32 PM CET 
//


package de.unipassau.medspace.common.rdf.mapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for File complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="File">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.medspace.com/rdf-mapping}Class">
 *       &lt;sequence>
 *         &lt;element name="source" type="{http://www.medspace.com/rdf-mapping}DataTypeProperty"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "File", propOrder = {
    "source"
})
public class FileMapping
    extends ClassMapping
{

    @XmlElement(required = true)
    protected DataTypePropertyMapping source;

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public DataTypePropertyMapping getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public void setSource(DataTypePropertyMapping value) {
        this.source = value;
    }

}
