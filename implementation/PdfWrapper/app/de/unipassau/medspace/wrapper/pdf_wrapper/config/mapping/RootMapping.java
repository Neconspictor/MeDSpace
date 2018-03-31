//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.03.31 at 08:47:32 PM CEST 
//


package de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import de.unipassau.medspace.common.rdf.mapping.NamespaceMapping;


/**
 * <p>Java class for Root element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="Root">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="Namespace" type="{http://www.medspace.com/rdf-mapping}Namespace" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="PdfRootDirectory" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="PdfFile" type="{http://www.medspace.com/images/ddsm}PdfFile"/>
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
    "namespace",
    "pdfRootDirectory",
    "pdfFile"
})
@XmlRootElement(name = "Root")
public class RootMapping {

    @XmlElement(name = "Namespace")
    protected List<NamespaceMapping> namespace;
    @XmlElement(name = "PdfRootDirectory", required = true)
    protected String pdfRootDirectory;
    @XmlElement(name = "PdfFile", required = true)
    protected PdfFileMapping pdfFile;

    /**
     * Gets the value of the namespace property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the namespace property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNamespace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NamespaceMapping }
     * 
     * 
     */
    public List<NamespaceMapping> getNamespace() {
        if (namespace == null) {
            namespace = new ArrayList<NamespaceMapping>();
        }
        return this.namespace;
    }

    /**
     * Gets the value of the pdfRootDirectory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPdfRootDirectory() {
        return pdfRootDirectory;
    }

    /**
     * Sets the value of the pdfRootDirectory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPdfRootDirectory(String value) {
        this.pdfRootDirectory = value;
    }

    /**
     * Gets the value of the pdfFile property.
     * 
     * @return
     *     possible object is
     *     {@link PdfFileMapping }
     *     
     */
    public PdfFileMapping getPdfFile() {
        return pdfFile;
    }

    /**
     * Sets the value of the pdfFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link PdfFileMapping }
     *     
     */
    public void setPdfFile(PdfFileMapping value) {
        this.pdfFile = value;
    }

}
