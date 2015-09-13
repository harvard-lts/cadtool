package edu.harvard.hul.ois.fits.cad;

import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.exceptions.FitsToolException;
import edu.harvard.hul.ois.fits.mapping.FitsXmlMapper;
import edu.harvard.hul.ois.fits.tools.ToolBase;
import edu.harvard.hul.ois.fits.tools.ToolOutput;
import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.jdom.DocumentWrapper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Main extends ToolBase {
    private final Map<String, Extractor> extractors;
    private static final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
    private boolean enabled = true;
    private static final String CADTOOL_XSLT_RESOURCE = "/cadtool_to_fits.xslt";

    public Document transform(InputStream xslt, Document input) throws FitsToolException, JDOMException, TransformerException {
        final DOMSource source = new DOMSource(new DOMOutputter().output(input));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslt));
        DOMResult result = new DOMResult();
        transformer.transform(source, result);
        return new DOMBuilder().build((org.w3c.dom.Document) result.getNode());
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
        final Extractor extractor = extractors.get(extension);
        final Element results;

        try {
            results = extractor.run(dataSource, filename);
        } catch (IOException e) {
            throw new FitsToolException("Error running cad extractor " + extractor.getName() + " on " + filename, e);
        }

        final Document toolOutput = new Document(results);
//        final Document fitsOutput = new Document(new Element("fits-placeholder"));
        //TODO: write a cadtool to fits XSLT transform and use that here
        final Document fitsOutput;
        try {
            fitsOutput = transform(getClass().getResourceAsStream(CADTOOL_XSLT_RESOURCE), toolOutput);
        } catch (JDOMException e) {
            e.printStackTrace();
            return null;
        } catch (TransformerException e) {
            e.printStackTrace();
            return null;
        }
        final ToolOutput output = new ToolOutput(this, fitsOutput, toolOutput);
//        output.addFileIdentity(new ToolIdentity("mime", "format", new ToolInfo("name", "version", "date")));
        try {
            out.output(output.getFitsXml(), System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
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

    public Main() throws FitsToolException {
        super();
        final Map<String, Extractor> temp = new HashMap<>();
        final Extractor[] allExtractors = new Extractor[] {
                new PdfExtractor()
        };
        for (Extractor extractor: allExtractors) {
            for(String extension: extractor.getExtensions()) {
                if (temp.containsKey(extension)) {
                   throw new FitsToolException("Tried to register multiple cad extractors (" + extractor.getName()
                           + ", " + temp.get(extension).getName() + ") for extension \"" + extension + "\"");
                }
                temp.put(extension, extractor);
            }
        }
        extractors = Collections.unmodifiableMap(temp);
        if (Fits.FITS_XML == null && Fits.mapper == null) {
            try {
                final String tempDir = System.getProperty("java.io.tmpdir");
                final File file = new File(tempDir, "fits_xml_map.xml");
                if (!file.isFile()) {
                    Files.copy(getClass().getResourceAsStream("/fits_xml_map.xml"), file.toPath());
                }
                Fits.FITS_XML = tempDir;
                Fits.mapper = new FitsXmlMapper();
            } catch (JDOMException | IOException e) {
                throw new FitsToolException("damn", e);
            }
        }
    }

    public static void main(String[] args) throws FitsToolException, IOException {
        //TODO: check args, print usage message
        final File file = new File(args[0]);
        final Main main = new Main();
        final ToolOutput results = main.extractInfo(file);
        out.output(results.getToolOutput(), System.out);
    }
}