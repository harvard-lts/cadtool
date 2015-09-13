package edu.harvard.hul.ois.fits.cad;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.activation.DataSource;
import java.io.IOException;

/**
 * Created by Isaac Simmons on 9/13/2015.
 */
public class X3dExtractor extends CadExtractor {
    final SAXBuilder parser = new SAXBuilder();

    protected X3dExtractor() {
        super("x3d", "X3D (Extensible 3D) model xml text", "model/x3d", ".x3d");
    }

    @Override
    protected void doRun(DataSource ds, String filename, Element result) throws IOException, ValidationException {
        final Document doc;
        try {
            doc = parser.build(ds.getInputStream());
        } catch (JDOMException e) {
            throw new ValidationException("Unable to parse X3D XML content", e);
        }
        System.out.println("X3D blah" + doc.getBaseURI());
        //TODO: think more about what my tools should output if given invalid files. Maybe it shouldn't propogate an exception all the way up, and instead just not return anything
    }
}
