package edu.harvard.hul.ois.fits.cad;

import edu.harvard.hul.ois.fits.exceptions.FitsToolException;
import edu.harvard.hul.ois.fits.tools.ToolOutput;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Ignore;
import org.junit.Test;

import javax.activation.URLDataSource;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class TestSampleFiles {
    private static final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
    private final CadTool cadTool;

    public static final List<String> PDF_TEST_FILES = Arrays.asList(
            "/1344464123.pdf",
            "/1344465784.pdf",
            "/DominicNotman_HydraulicChuck.pdf",
            "/Kompas-Stanchion_eng.pdf",
            "/mayavi_conic_spiral.pdf",
            "/PDF3D_COMSOL_EigenvalueAnalysisOfACrankshaft.pdf"
    );

    public static final List<String> DWG_TEST_FILES = Arrays.asList(
            "/civil_example-imperial.dwg",
            "/Figure_A04.dwg",
            "/Pump_cover.dwg",
            "/visualization_-_sun_and_sky_demo.dwg",
            "/5217plan.dwg"
    );

    public static final List<String> X3D_TEST_FILES = Arrays.asList(
            "/5000points.x3d",
            "/extents.x3d",
            "/HelloWorld.x3d",
            "/NonplanarPolygons.x3d",
            "/test-ccwsolid.x3d",
            "/TriangleStripSet.x3d"
    );

    public static final List<String> DXF_TEST_FILES = Arrays.asList(
            "/5217plan.dxf",
            "/Bottom_plate.dxf",
            "/Pump_cover.dxf",
            "/R-126_strat_plan01.dxf"
    );

    public static final List<String> ALL_TEST_FILES = new ArrayList<>();
    static {
        ALL_TEST_FILES.addAll(PDF_TEST_FILES);
        ALL_TEST_FILES.addAll(DWG_TEST_FILES);
        ALL_TEST_FILES.addAll(X3D_TEST_FILES);
        ALL_TEST_FILES.addAll(DXF_TEST_FILES);
    }

    public TestSampleFiles() throws FitsToolException {
        cadTool = new CadTool();
    }

    private Element testFiles(String elementName, Collection<String> files) throws FitsToolException {
        final Element results = new Element(elementName);
        for (String filename: files) {
            final URL resource = getClass().getResource(filename);
            assertNotNull(resource);
            final ToolOutput output = cadTool.extractInfo(filename, new URLDataSource(resource));
            results.addContent(output.getToolOutput().detachRootElement());
        }
        return results;
    }

    @Test
    public void testPdfFiles() throws IOException, FitsToolException {
        final Element results = testFiles("pdf-test-results", PDF_TEST_FILES);
        out.output(results, System.out);
        System.out.flush();
    }

    @Test
    public void testDwgFiles() throws IOException, FitsToolException {
        final Element results = testFiles("dwg-test-results", DWG_TEST_FILES);
        out.output(results, System.out);
        System.out.flush();
    }

    @Test
    public void testX3dFiles() throws IOException, FitsToolException {
        final Element results = testFiles("x3d-test-results", X3D_TEST_FILES);
        out.output(results, System.out);
        System.out.flush();
    }

    @Test
    public void testDxfFiles() throws IOException, FitsToolException {
        final Element results = testFiles("dxf-test-results", DXF_TEST_FILES);
        out.output(results, System.out);
        System.out.flush();
    }

    @Test
    @Ignore
    public void testAllFiles() throws IOException, FitsToolException {
        final Element results = testFiles("test-results", ALL_TEST_FILES);
        out.output(results, System.out);
        System.out.flush();
    }
}


