//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.03.31 at 07:14:42 PM CEST 
//


package de.unipassau.medspace.global.config.mapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Register complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Register">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IO-Error-Limit" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Register", propOrder = {
    "ioErrorLimit"
})
public class RegisterMapping {

    @XmlElement(name = "IO-Error-Limit")
    protected int ioErrorLimit;

    /**
     * Gets the value of the ioErrorLimit property.
     * 
     */
    public int getIOErrorLimit() {
        return ioErrorLimit;
    }

    /**
     * Sets the value of the ioErrorLimit property.
     * 
     */
    public void setIOErrorLimit(int value) {
        this.ioErrorLimit = value;
    }

}
