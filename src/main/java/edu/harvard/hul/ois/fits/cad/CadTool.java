package edu.harvard.hul.ois.fits.cad;

import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.exceptions.FitsToolException;
import edu.harvard.hul.ois.fits.mapping.FitsXmlMapper;
import edu.harvard.hul.ois.fits.tools.ToolBase;
import edu.harvard.hul.ois.fits.tools.ToolOutput;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformException;
import org.jdom.transform.XSLTransformer;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class CadTool extends ToolBase {
    private static final String CADTOOL_XSLT_RESOURCE = "/cadtool_to_fits.xslt";

    private final Map<String, CadExtractor> extractors;
    private final XSLTransformer transformer;
    private boolean enabled = true;

    public CadTool() throws FitsToolException {
        super();
        final Map<String, CadExtractor> temp = new HashMap<>();
        final CadExtractor[] allExtractors = new CadExtractor[] {
                new PdfExtractor()
        };
        for (CadExtractor extractor: allExtractors) {
            for(String extension: extractor.getExtensions()) {
                if (temp.containsKey(extension)) {
                    throw new FitsToolException("Tried to register multiple cad extractors (" + extractor.getName()
                            + ", " + temp.get(extension).getName() + ") for extension \"" + extension + "\"");
                }
                temp.put(extension, extractor);
            }
        }
        extractors = Collections.unmodifiableMap(temp);

        try {
            this.transformer = new XSLTransformer(getClass().getResourceAsStream(CADTOOL_XSLT_RESOURCE));
        } catch (XSLTransformException e) {
            throw new FitsToolException("Error initializing JDOM XSL Transformer");
        }

        ///////////////UGLY HACK/////////////////
        //If we are running outside of FITS, we need to fake a few static values that normally get initialized
        //in its constructor
        if (Fits.FITS_XML == null && Fits.mapper == null) {
            try {
                //FitsXmlMapper constructor expects to find the fits_xml_map.xml file on the filesystem
                //Pointed at by the Fits.FITS_XML variable
                final String tempDir = System.getProperty("java.io.tmpdir");
                final File file = new File(tempDir, "fits_xml_map.xml");
                if (!file.isFile()) {
                    Files.copy(getClass().getResourceAsStream("/fits_xml_map.xml"), file.toPath());
                }
                Fits.FITS_XML = tempDir;
                Fits.mapper = new FitsXmlMapper();
            } catch (JDOMException | IOException e) {
                throw new FitsToolException("Error initializing static FITS values for standalone use", e);
            }
        }
    }

    public ToolOutput extractInfo(String filename, DataSource dataSource) throws FitsToolException {
        final int lastPeriod = filename.lastIndexOf('.');
        if (lastPeriod == -1) {
            throw new FitsToolException("cadtool invoked on file with no extension: " + filename);
        }
        final String extension = filename.substring(lastPeriod);
        if (! extractors.containsKey(extension)) {
            throw new FitsToolException("cadtool invoked on file with unsupported extension: " + filename);
        }
        final CadExtractor extractor = extractors.get(extension);
        final Element results;

        try {
            results = extractor.run(dataSource, filename);
        } catch (IOException e) {
            throw new FitsToolException("Error running cad extractor " + extractor.getName() + " on " + filename, e);
        }

        final Document toolOutput = new Document(results);
        final Document fitsOutput;
        try {
            fitsOutput = transformer.transform(toolOutput);
        } catch (JDOMException e) {
            throw new FitsToolException("Error transforming tool output to fits output", e);
        }
        return new ToolOutput(this, fitsOutput, toolOutput);
    }

    @Override
    public ToolOutput extractInfo(File file) throws FitsToolException {
        return extractInfo(file.getName(), new FileDataSource(file));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static void main(String[] args) throws FitsToolException, IOException {
        //TODO: check args, print usage message
        final File file = new File(args[0]);
        final CadTool cadTool = new CadTool();
        final ToolOutput results = cadTool.extractInfo(file);
        new XMLOutputter(Format.getPrettyFormat()).output(results.getToolOutput(), System.out);
    }
}