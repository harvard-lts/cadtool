package edu.harvard.hul.ois.fits.cad;

import edu.harvard.hul.ois.fits.cad.util.XmlUtil;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.activation.URLDataSource;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestSampleFiles {
    public static final String[] PDF_TEST_FILES = new String[]{
            "/1344464123.pdf",
            "/1344465784.pdf",
            "/DominicNotman_HydraulicChuck.pdf",
            "/Kompas-Stanchion_eng.pdf",
            "/mayavi_conic_spiral.pdf",
            "/PDF3D_COMSOL_EigenvalueAnalysisOfACrankshaft.pdf"
    };

    @Test
    public void testPdfFiles() throws IOException, TransformerException {
        System.out.println("RAN A TEST");
        final Extractor extractor = new PdfExtractor();
        final Element results = XmlUtil.newResults();
        for (String filename: PDF_TEST_FILES) {
            assertTrue(extractor.accepts(filename));
            final URL resource = getClass().getResource(filename);
            assertNotNull(resource);
            extractor.run(new URLDataSource(resource), filename, results);
        }
        XmlUtil.printXml(results);
    }
}
