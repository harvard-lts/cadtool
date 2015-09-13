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

    public static final String[] DWG_TEST_FILES = new String[]{
            "/civil_example-imperial.dwg",
            "/Figure_A04.dwg",
            "/Pump_cover.dwg",
            "/visualization_-_sun_and_sky_demo.dwg"
    };

    public static final String[] X3D_TEST_FILES = new String[]{
            "/5000points.x3d",
            "/extents.x3d",
            "/HelloWorld.x3d",
            "/NonplanarPolygons.x3d",
            "/test-ccwsolid.x3d",
            "/TriangleStripSet.x3d"
    };

    public TestSampleFiles() throws FitsToolException {
        cadTool = new CadTool();
    }

    @Test
    public void testPdfFiles() throws IOException, FitsToolException {
        final Element results = new Element("pdf-test-results");
        for (String filename: PDF_TEST_FILES) {
            final URL resource = getClass().getResource(filename);
            assertNotNull(resource);
            final ToolOutput output = cadTool.extractInfo(filename, new URLDataSource(resource));
            results.addContent(output.getToolOutput().detachRootElement());
        }
        out.output(results, System.out);
    }

    @Test
    public void testDwgFiles() throws IOException, FitsToolException {
        final Element results = new Element("dwg-test-results");
        for (String filename: DWG_TEST_FILES) {
            final URL resource = getClass().getResource(filename);
            assertNotNull(resource);
            final ToolOutput output = cadTool.extractInfo(filename, new URLDataSource(resource));
            results.addContent(output.getToolOutput().detachRootElement());
        }
        out.output(results, System.out);
    }

    @Test
    public void testX3dFiles() throws IOException, FitsToolException {
        final Element results = new Element("x3d-test-results");
        for (String filename: X3D_TEST_FILES) {
            final URL resource = getClass().getResource(filename);
            assertNotNull(resource);
            final ToolOutput output = cadTool.extractInfo(filename, new URLDataSource(resource));
            results.addContent(output.getToolOutput().detachRootElement());
        }
        out.output(results, System.out);
    }
}


