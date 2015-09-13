package edu.harvard.hul.ois.fits.cad;

import edu.harvard.hul.ois.fits.exceptions.FitsToolException;
import edu.harvard.hul.ois.fits.tools.ToolBase;
import edu.harvard.hul.ois.fits.tools.ToolOutput;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.activation.FileDataSource;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends ToolBase {
    private final Map<String, Extractor> extractors;
    private static final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
    private boolean enabled = true;

    @Override
    public ToolOutput extractInfo(File file) throws FitsToolException {
        final String filename = file.getName();
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
            results = extractor.run(new FileDataSource(file), filename);
        } catch (IOException e) {
            throw new FitsToolException("Error running cad extractor " + extractor.getName() + " on " + filename, e);
        }

        final Document toolOutput = new Document(results);
        final Document fitsOutput = new Document(new Element("fits-placeholder"));
        //TODO: write a cadtool to fits XSLT transform and use that here
//        final Document fitsOutput = transform(xslt, toolOutput);

        return new ToolOutput(this, fitsOutput, toolOutput);
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
    }

    public static void main(String[] args) throws FitsToolException, IOException {
        //TODO: check args, print usage message
        final File file = new File(args[0]);
        final Main main = new Main();
        final ToolOutput results = main.extractInfo(file);
        out.output(results.getToolOutput(), System.out);
    }
}