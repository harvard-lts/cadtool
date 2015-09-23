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

    public X3dExtractor() {
        super("x3d", ".x3d");
    }

    @Override
    protected void doRun(DataSource ds, String filename, Element result) throws IOException, ValidationException {
        final Document doc;
        try {
            doc = new SAXBuilder().build(ds.getInputStream());
        } catch (JDOMException e) {
            throw new ValidationException("Unable to parse X3D XML content", e);
        }
        if (doc.getDocType() == null || doc.getDocType().getSystemID() == null) {
            throw new ValidationException("No doctype system identifier defined for X3D XML content");
        }
        if (! doc.getDocType().getSystemID().startsWith("http://www.web3d.org/specifications/x3d-")) {
            throw new ValidationException("XML doctype system identifier doesn't look like X3D: " + doc.getDocType().getSystemID());
        }
        //TODO: think more about what my tools should output if given invalid files. Maybe it shouldn't propagate an exception all the way up, and instead just not return anything

        if (! "X3D".equals(doc.getRootElement().getName())) {
            throw new ValidationException("x3d document has incorrect root element: " + doc.getRootElement().getName());
        }

        final Element identity = new Element("identity");
        identity.setAttribute("mimetype", "model/x3d");
        identity.setAttribute("format", "X3D (Extensible 3D) model xml text");
        final String version = doc.getDocType().getSystemID().substring("http://www.web3d.org/specifications/x3d-".length());
        if (version.endsWith(".dtd")) {
            identity.setAttribute("version", version.substring(0, version.length() - 4));
        }
        result.addContent(identity);

        //Grab all headers from the X3D doc and shove them into our results
        final List headerNodes;
        try {
            headerNodes = XPath.newInstance("/X3D/head/meta").selectNodes(doc);
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
