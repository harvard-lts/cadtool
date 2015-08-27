package edu.harvard.hul.ois.fits.cad;

import org.junit.Test;

import javax.activation.URLDataSource;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

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
    public void testPdfFiles() {
        System.out.println("RAN A TEST");
        final Main main = new Main();
        for (String filename: PDF_TEST_FILES) {
            final URL resource = getClass().getResource(filename);
            assertNotNull(resource);
            try {
                main.runPdf(new URLDataSource(resource));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
