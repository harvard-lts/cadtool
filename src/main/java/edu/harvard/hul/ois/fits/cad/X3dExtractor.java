package edu.harvard.hul.ois.fits.cad;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import javax.activation.DataSource;
import java.io.IOException;
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
    public CadToolResult run(DataSource ds, String filename) throws IOException, ValidationException {
        final CadToolResult result = new CadToolResult(name, filename);
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

        result.mimetype = "model/x3d";
        result.formatName = "X3D (Extensible 3D) model xml text";
        final String version = doc.getDocType().getSystemID().substring("http://www.web3d.org/specifications/x3d-".length());
        if (version.endsWith(".dtd")) {
            result.formatVersion = version.substring(0, version.length() - 4);
        }

        //Grab all headers from the X3D doc and shove them into our results
        final List headerNodes;
        try {
            headerNodes = XPath.newInstance("/X3D/head/meta").selectNodes(doc);
        } catch (JDOMException e) {
            throw new RuntimeException(e); //TODO: something better than just upgrading this to a runtime
        }
        for(Object headerNode: headerNodes) {
            if (headerNode instanceof Element) {
                final String name = ((Element) headerNode).getAttributeValue("name");
                final String value = ((Element) headerNode).getAttributeValue("content");
                result.addKeyValue(name, value);
            }
        }
        return result;
    }

    //TODO: I should be able to pull quite a few structural features out of this as well
}
