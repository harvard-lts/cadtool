package edu.harvard.hul.ois.fits.cad;

import edu.harvard.hul.ois.fits.exceptions.FitsToolException;
import edu.harvard.hul.ois.fits.tools.ToolOutput;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

import javax.activation.URLDataSource;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

public class TestSampleFiles {
    private static final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
    private final CadTool cadTool;

    public static final String[] PDF_TEST_FILES = new String[]{
            "/1344464123.pdf",
            "/1344465784.pdf",
            "/DominicNotman_HydraulicChuck.pdf",
            "/Kompas-Stanchion_eng.pdf",
            "/mayavi_conic_spiral.pdf",
            "/PDF3D_COMSOL_EigenvalueAnalysisOfACrankshaft.pdf"
    };

    public TestSampleFiles() throws FitsToolException {
        cadTool = new CadTool();
    }

    @Test
    public void testPdfFiles() throws IOException, FitsToolException {
        final Element results = new Element("test-results");
        for (String filename: PDF_TEST_FILES) {
            final URL resource = getClass().getResource(filename);
            assertNotNull(resource);
            final ToolOutput output = cadTool.extractInfo(filename, new URLDataSource(resource));
            results.addContent(output.getToolOutput().detachRootElement());
        }
        out.output(results, System.out);
    }
}
