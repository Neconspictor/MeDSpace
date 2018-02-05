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
 * <p>Java class for Abnormality complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Abnormality">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="abnormality" type="{http://www.medspace.com/images/ddsm}DataTypeProperty"/>
 *         &lt;element name="assessment" type="{http://www.medspace.com/images/ddsm}DataTypeProperty"/>
 *         &lt;element name="subtlety" type="{http://www.medspace.com/images/ddsm}DataTypeProperty"/>
 *         &lt;element name="pathology" type="{http://www.medspace.com/images/ddsm}DataTypeProperty"/>
 *         &lt;element name="totalOutlines" type="{http://www.medspace.com/images/ddsm}DataTypeProperty"/>
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
@XmlType(name = "Abnormality", propOrder = {
    "abnormality",
    "assessment",
    "subtlety",
    "pathology",
    "totalOutlines"
})
public class AbnormalityParsing {

    @XmlElement(required = true)
    protected DataTypePropertyParsing abnormality;
    @XmlElement(required = true)
    protected DataTypePropertyParsing assessment;
    @XmlElement(required = true)
    protected DataTypePropertyParsing subtlety;
    @XmlElement(required = true)
    protected DataTypePropertyParsing pathology;
    @XmlElement(required = true)
    protected DataTypePropertyParsing totalOutlines;
    @XmlAttribute(name = "objectType", required = true)
    protected String objectType;

    /**
     * Gets the value of the abnormality property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public DataTypePropertyParsing getAbnormality() {
        return abnormality;
    }

    /**
     * Sets the value of the abnormality property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public void setAbnormality(DataTypePropertyParsing value) {
        this.abnormality = value;
    }

    /**
     * Gets the value of the assessment property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public DataTypePropertyParsing getAssessment() {
        return assessment;
    }

    /**
     * Sets the value of the assessment property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public void setAssessment(DataTypePropertyParsing value) {
        this.assessment = value;
    }

    /**
     * Gets the value of the subtlety property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public DataTypePropertyParsing getSubtlety() {
        return subtlety;
    }

    /**
     * Sets the value of the subtlety property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public void setSubtlety(DataTypePropertyParsing value) {
        this.subtlety = value;
    }

    /**
     * Gets the value of the pathology property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public DataTypePropertyParsing getPathology() {
        return pathology;
    }

    /**
     * Sets the value of the pathology property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public void setPathology(DataTypePropertyParsing value) {
        this.pathology = value;
    }

    /**
     * Gets the value of the totalOutlines property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public DataTypePropertyParsing getTotalOutlines() {
        return totalOutlines;
    }

    /**
     * Sets the value of the totalOutlines property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyParsing }
     *     
     */
    public void setTotalOutlines(DataTypePropertyParsing value) {
        this.totalOutlines = value;
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