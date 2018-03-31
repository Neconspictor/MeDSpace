//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.03.31 at 09:20:20 PM CEST 
//


package de.unipassau.medspace.wrapper.image_wrapper.config.mapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import de.unipassau.medspace.common.rdf.mapping.DataTypePropertyMapping;
import de.unipassau.medspace.common.rdf.mapping.FileMapping;
import de.unipassau.medspace.common.rdf.mapping.ObjectPropertyMapping;


/**
 * <p>Java class for Overlay complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Overlay">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.medspace.com/rdf-mapping}File">
 *       &lt;sequence>
 *         &lt;element name="totalAbnormalities" type="{http://www.medspace.com/rdf-mapping}DataTypeProperty"/>
 *         &lt;element name="abnormality" type="{http://www.medspace.com/rdf-mapping}ObjectProperty"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Overlay", propOrder = {
    "totalAbnormalities",
    "abnormality"
})
public class OverlayMapping
    extends FileMapping
{

    @XmlElement(required = true)
    protected DataTypePropertyMapping totalAbnormalities;
    @XmlElement(required = true)
    protected ObjectPropertyMapping abnormality;

    /**
     * Gets the value of the totalAbnormalities property.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public DataTypePropertyMapping getTotalAbnormalities() {
        return totalAbnormalities;
    }

    /**
     * Sets the value of the totalAbnormalities property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePropertyMapping }
     *     
     */
    public void setTotalAbnormalities(DataTypePropertyMapping value) {
        this.totalAbnormalities = value;
    }

    /**
     * Gets the value of the abnormality property.
     * 
     * @return
     *     possible object is
     *     {@link ObjectPropertyMapping }
     *     
     */
    public ObjectPropertyMapping getAbnormality() {
        return abnormality;
    }

    /**
     * Sets the value of the abnormality property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectPropertyMapping }
     *     
     */
    public void setAbnormality(ObjectPropertyMapping value) {
        this.abnormality = value;
    }

}
