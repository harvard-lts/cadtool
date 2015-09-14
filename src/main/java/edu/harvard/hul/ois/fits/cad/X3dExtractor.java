package edu.harvard.hul.ois.fits.cad;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import javax.activation.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Isaac Simmons on 9/13/2015.
 */
public class X3dExtractor extends CadExtractor {
    //TODO: only works with XML encoding for now. Do I care about .x3dv or .wrl?
    final SAXBuilder parser = new SAXBuilder();
    final XPath headerPath;


    public X3dExtractor() throws JDOMException {
        super("x3d", "X3D (Extensible 3D) model xml text", "model/x3d", ".x3d");
        headerPath = XPath.newInstance("/X3D/head/meta");
    }

    @Override
    protected void doRun(DataSource ds, String filename, Element result) throws IOException, ValidationException {
        final Document doc;
        try {
            doc = parser.build(ds.getInputStream());
        } catch (JDOMException e) {
            throw new ValidationException("Unable to parse X3D XML content", e);
        }
        if (doc.getDocType() == null || doc.getDocType().getSystemID() == null) {
            throw new ValidationException("No doctype system identifier defined for X3D XML content");
        }
        if (! doc.getDocType().getSystemID().startsWith("http://www.web3d.org/specifications/x3d-")) {
            throw new ValidationException("XML doctype system identifier doesn't look like X3D: " + doc.getDocType().getSystemID());
        }
        //TODO: think more about what my tools should output if given invalid files. Maybe it shouldn't propogate an exception all the way up, and instead just not return anything

        if (! "X3D".equals(doc.getRootElement().getName())) {
            throw new ValidationException("x3d document has incorrect root element: " + doc.getRootElement().getName());
        }

        //Grab all headers from the X3D doc and shove them into our results
        final List headerNodes;
        try {
            headerNodes = headerPath.selectNodes(doc);
        } catch (JDOMException e) {
            throw new RuntimeException(e); //TODO: something better than just upgrading this to a runtime
        }
        for(Object headerNode: headerNodes) {
            //TODO: special handling of some headers in order to make the xslt into fits xml simpler?
            if (headerNode instanceof Element) {
                result.addContent((Element)((Element)headerNode).clone());
            }
        }
    }

    //TODO: I should be able to pull quite a few structural features out of this as well
}