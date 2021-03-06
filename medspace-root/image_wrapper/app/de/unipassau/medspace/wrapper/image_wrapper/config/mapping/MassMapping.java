//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.04.02 at 12:36:13 AM CEST 
//


package de.unipassau.medspace.wrapper.image_wrapper.config.mapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import de.unipassau.medspace.common.rdf.mapping.ClassMapping;
import de.unipassau.medspace.common.rdf.mapping.DataTypePropertyMapping;


/**
 * <p>Java class for Mass complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Mass">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.medspace.com/rdf-mapping}Class">
 *       &lt;sequence>
 *         &lt;element name="shape" type="{http://www.medspace.com/rdf-mapping}DataTypeProperty"/>
 *         &lt;element name="margins" type="{http://www.medspace.com/rdf-mapping}DataTypeProperty"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Mass", propOrder = {
    "shape",
    "margins"
})
public class MassMapping
    extends ClassMapping
{

    @XmlElement(required = true)
    protected DataTypePropertyMapping shape;
    @XmlElement(required = true)
    protected DataTypePropertyMapping margins;

    /**
     * Gets the value of the shape property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public DataTypePropertyMapping getShape() {
        return shape;
    }

    /**
     * Sets the value of the shape property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public void setShape(DataTypePropertyMapping value) {
        this.shape = value;
    }

    /**
     * Gets the value of the margins property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public DataTypePropertyMapping getMargins() {
        return margins;
    }

    /**
     * Sets the value of the margins property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public void setMargins(DataTypePropertyMapping value) {
        this.margins = value;
    }

}
