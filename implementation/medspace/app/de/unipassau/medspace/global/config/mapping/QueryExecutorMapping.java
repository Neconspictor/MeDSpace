//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.04.02 at 01:04:52 AM CEST 
//


package de.unipassau.medspace.global.config.mapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for QueryExecutor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryExecutor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="QueryCacheSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryExecutor", propOrder = {
    "queryCacheSize"
})
public class QueryExecutorMapping {

    @XmlElement(name = "QueryCacheSize")
    protected int queryCacheSize;

    /**
     * Gets the value of the queryCacheSize property.
     * 
     */
    public int getQueryCacheSize() {
        return queryCacheSize;
    }

    /**
     * Sets the value of the queryCacheSize property.
     * 
     */
    public void setQueryCacheSize(int value) {
        this.queryCacheSize = value;
    }

}
