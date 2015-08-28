package edu.harvard.hul.ois.fits.cad.util;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;

/**
 * Created by Isaac Simmons on 8/27/2015.
 */
public class XmlUtil {
    public static final String XSD_FILENAME = "/cadtool.xsd";

    private static final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    private XmlUtil() {}

    public static Document newDocument() throws ParserConfigurationException {
        return dbf.newDocumentBuilder().newDocument();
    }

    public static synchronized void printXml(Document doc) throws TransformerException, IOException {
        final Transformer transformer = transformerFactory.newTransformer();
        final DOMSource source = new DOMSource(doc);
//        try {
//            final Schema schema = schemaFactory.newSchema(XmlUtil.class.getResource(XSD_FILENAME));
//            final Validator validator = schema.newValidator();
//            validator.validate(source);
//        } catch (SAXException e) {
//            e.printStackTrace();
//            //TODO: re-throw this? return false?
//        }

        final StreamResult result = new StreamResult(System.out);  //TODO: take the stream result (or the output stream) as an argument?
        transformer.transform(source, result);
    }
}
