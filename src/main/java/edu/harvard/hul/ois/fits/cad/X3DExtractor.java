package edu.harvard.hul.ois.fits.cad;

import org.jdom.Element;

import javax.activation.DataSource;
import java.io.IOException;

/**
 * Created by Isaac Simmons on 9/13/2015.
 */
public class X3DExtractor extends CadExtractor {
    protected X3DExtractor() {
        super("x3d", "X3D (Extensible 3D) model xml text", "model/x3d", ".x3d");
    }

    @Override
    protected void doRun(DataSource ds, String filename, Element result) throws IOException {

    }
}
