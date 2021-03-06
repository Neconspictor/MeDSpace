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
 * <p>Java class for Calcification complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Calcification">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.medspace.com/rdf-mapping}Class">
 *       &lt;sequence>
 *         &lt;element name="type" type="{http://www.medspace.com/rdf-mapping}DataTypeProperty"/>
 *         &lt;element name="distribution" type="{http://www.medspace.com/rdf-mapping}DataTypeProperty"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Calcification", propOrder = {
    "type",
    "distribution"
})
public class CalcificationMapping
    extends ClassMapping
{

    @XmlElement(required = true)
    protected DataTypePropertyMapping type;
    @XmlElement(required = true)
    protected DataTypePropertyMapping distribution;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public DataTypePropertyMapping getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public void setType(DataTypePropertyMapping value) {
        this.type = value;
    }

    /**
     * Gets the value of the distribution property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public DataTypePropertyMapping getDistribution() {
        return distribution;
    }

    /**
     * Sets the value of the distribution property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public void setDistribution(DataTypePropertyMapping value) {
        this.distribution = value;
    }

}
