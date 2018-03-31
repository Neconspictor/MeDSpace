//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.04.01 at 12:15:20 AM CEST 
//


package de.unipassau.medspace.global.config.mapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Config element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="Config">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="DataCollector" type="{http://www.medspace.com/global/global-server-config-specification}DataCollector"/>
 *           &lt;element name="QueryExecutor" type="{http://www.medspace.com/global/global-server-config-specification}QueryExecutor"/>
 *           &lt;element name="Register" type="{http://www.medspace.com/global/global-server-config-specification}Register"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dataCollector",
    "queryExecutor",
    "register"
})
@XmlRootElement(name = "Config")
public class ConfigMapping {

    @XmlElement(name = "DataCollector", required = true)
    protected DataCollectorMapping dataCollector;
    @XmlElement(name = "QueryExecutor", required = true)
    protected QueryExecutorMapping queryExecutor;
    @XmlElement(name = "Register", required = true)
    protected RegisterMapping register;

    /**
     * Gets the value of the dataCollector property.
     * 
     * @return
     *     possible object is
     *     {@link DataCollectorMapping }
     *     
     */
    public DataCollectorMapping getDataCollector() {
        return dataCollector;
    }

    /**
     * Sets the value of the dataCollector property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataCollectorMapping }
     *     
     */
    public void setDataCollector(DataCollectorMapping value) {
        this.dataCollector = value;
    }

    /**
     * Gets the value of the queryExecutor property.
     * 
     * @return
     *     possible object is
     *     {@link QueryExecutorMapping }
     *     
     */
    public QueryExecutorMapping getQueryExecutor() {
        return queryExecutor;
    }

    /**
     * Sets the value of the queryExecutor property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryExecutorMapping }
     *     
     */
    public void setQueryExecutor(QueryExecutorMapping value) {
        this.queryExecutor = value;
    }

    /**
     * Gets the value of the register property.
     * 
     * @return
     *     possible object is
     *     {@link RegisterMapping }
     *     
     */
    public RegisterMapping getRegister() {
        return register;
    }

    /**
     * Sets the value of the register property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegisterMapping }
     *     
     */
    public void setRegister(RegisterMapping value) {
        this.register = value;
    }

}
