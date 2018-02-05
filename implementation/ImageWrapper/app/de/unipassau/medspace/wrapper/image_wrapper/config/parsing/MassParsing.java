//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.02.04 at 04:12:07 PM CET 
//


package de.unipassau.medspace.wrapper.image_wrapper.config.parsing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Mass complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Mass">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="shape" type="{http://www.medspace.com/images/ddsm}DataTypeProperty"/>
 *         &lt;element name="margins" type="{http://www.medspace.com/images/ddsm}DataTypeProperty"/>
 *       &lt;/sequence>
 *       &lt;attribute name="objectType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
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
public class MassParsing {

    @XmlElement(required = true)
    protected DataTypePropertyParsing shape;
    @XmlElement(required = true)
    protected DataTypePropertyParsing margins;
    @XmlAttribute(name = "objectType", required = true)
    protected String objectType;

    /**
     * Gets the value of the shape property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public DataTypePropertyParsing getShape() {
        return shape;
    }

    /**
     * Sets the value of the shape property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public void setShape(DataTypePropertyParsing value) {
        this.shape = value;
    }

    /**
     * Gets the value of the margins property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public DataTypePropertyParsing getMargins() {
        return margins;
    }

    /**
     * Sets the value of the margins property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public void setMargins(DataTypePropertyParsing value) {
        this.margins = value;
    }

    /**
     * Gets the value of the objectType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Sets the value of the objectType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectType(String value) {
        this.objectType = value;
    }

}