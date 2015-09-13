package edu.harvard.hul.ois.fits.cad;

import org.jdom.Element;

import javax.activation.DataSource;
import java.io.IOException;

/**
 * Created by Isaac Simmons on 9/13/2015.
 */
public class DwgExtractor extends CadExtractor {
    public DwgExtractor() {
        super("dwg", "AutoCad Drawing", "image/vnd.dwg", ".dwg");
    }

    @Override
    protected void doRun(DataSource ds, String filename, Element result) throws IOException {

    }
}
