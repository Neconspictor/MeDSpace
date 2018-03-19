//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.03.19 at 09:38:02 PM CET 
//


package de.unipassau.medspace.d2r.config.parsing;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DBConnection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DBConnection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DBAuthentification" type="{http://www.medspace.com/D2Rmap}DBAuthentification" minOccurs="0"/>
 *         &lt;element name="DataSourceProperty" type="{http://www.medspace.com/D2Rmap}DataSourceProperty" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="jdbcDriver" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="jdbcDSN" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="poolSize" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DBConnection", propOrder = {
    "dbAuthentification",
    "dataSourceProperty"
})
public class DBConnectionParsing {

    @XmlElement(name = "DBAuthentification")
    protected DBAuthentificationParsing dbAuthentification;
    @XmlElement(name = "DataSourceProperty")
    protected List<DataSourcePropertyParsing> dataSourceProperty;
    @XmlAttribute(name = "jdbcDriver", required = true)
    protected String jdbcDriver;
    @XmlAttribute(name = "jdbcDSN", required = true)
    protected String jdbcDSN;
    @XmlAttribute(name = "poolSize")
    protected Integer poolSize;

    /**
     * Gets the value of the dbAuthentification property.
     * 
     * @return
     *     possible object is
     *     {@link DBAuthentificationParsing }
     *     
     */
    public DBAuthentificationParsing getDBAuthentification() {
        return dbAuthentification;
    }

    /**
     * Sets the value of the dbAuthentification property.
     * 
     * @param value
     *     allowed object is
     *     {@link DBAuthentificationParsing }
     *     
     */
    public void setDBAuthentification(DBAuthentificationParsing value) {
        this.dbAuthentification = value;
    }

    /**
     * Gets the value of the dataSourceProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataSourceProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataSourceProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSourcePropertyParsing }
     * 
     * 
     */
    public List<DataSourcePropertyParsing> getDataSourceProperty() {
        if (dataSourceProperty == null) {
            dataSourceProperty = new ArrayList<DataSourcePropertyParsing>();
        }
        return this.dataSourceProperty;
    }

    /**
     * Gets the value of the jdbcDriver property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJdbcDriver() {
        return jdbcDriver;
    }

    /**
     * Sets the value of the jdbcDriver property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJdbcDriver(String value) {
        this.jdbcDriver = value;
    }

    /**
     * Gets the value of the jdbcDSN property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJdbcDSN() {
        return jdbcDSN;
    }

    /**
     * Sets the value of the jdbcDSN property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJdbcDSN(String value) {
        this.jdbcDSN = value;
    }

    /**
     * Gets the value of the poolSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPoolSize() {
        return poolSize;
    }

    /**
     * Sets the value of the poolSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPoolSize(Integer value) {
        this.poolSize = value;
    }

}
