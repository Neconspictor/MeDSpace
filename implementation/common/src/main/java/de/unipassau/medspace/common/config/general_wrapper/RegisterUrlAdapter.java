package de.unipassau.medspace.common.config.general_wrapper;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * An XML adapter for the register url property. Is used to convert between a string which represents an URL
 * and the actual Java URL class.
 */
public class RegisterUrlAdapter
    extends XmlAdapter<String, URL>
{

    @Override
    public URL unmarshal(String value) throws MalformedURLException {
        return new URL(value);
    }

    @Override
    public String marshal(URL value) throws Exception {
        return value.toString();
    }
}