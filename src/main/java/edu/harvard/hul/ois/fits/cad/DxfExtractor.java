package edu.harvard.hul.ois.fits.cad;

import org.jdom.Element;

import javax.activation.DataSource;
import java.io.IOException;

/**
 * Created by Isaac Simmons on 9/13/2015.
 */
public class DxfExtractor extends CadExtractor {
    protected DxfExtractor() {
        super("dxf", "Drawing eXchange Format", "image/vnd.dxf", ".dxf");
    }

    @Override
    protected void doRun(DataSource ds, String filename, Element result) throws IOException {

    }
}
