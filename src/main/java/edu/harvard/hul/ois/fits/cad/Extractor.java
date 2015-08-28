package edu.harvard.hul.ois.fits.cad;

import javax.activation.DataSource;
import java.io.IOException;

public interface Extractor {
    boolean accepts(String filename);
    void run(DataSource ds, String filename) throws IOException;
}
