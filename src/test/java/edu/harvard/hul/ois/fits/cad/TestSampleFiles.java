package edu.harvard.hul.ois.fits.cad;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

import javax.activation.URLDataSource;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestSampleFiles {
    private static final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

    public static final String[] PDF_TEST_FILES = new String[]{
            "/1344464123.pdf",
            "/1344465784.pdf",
            "/DominicNotman_HydraulicChuck.pdf",
            "/Kompas-Stanchion_eng.pdf",
            "/mayavi_conic_spiral.pdf",
            "/PDF3D_COMSOL_EigenvalueAnalysisOfACrankshaft.pdf"
    };

    @Test
    public void testPdfFiles() throws IOException {
        System.out.println("RAN A TEST");
        final Extractor extractor = new PdfExtractor();
        final Element results = new Element("results");
        for (String filename: PDF_TEST_FILES) {
            assertTrue(extractor.accepts(filename));
            final URL resource = getClass().getResource(filename);
            assertNotNull(resource);
            results.addContent(extractor.run(new URLDataSource(resource), filename));
        }
        out.output(results, System.out);
    }
}
