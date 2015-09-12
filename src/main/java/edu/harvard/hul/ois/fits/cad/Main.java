package edu.harvard.hul.ois.fits.cad;

import edu.harvard.hul.ois.fits.exceptions.FitsToolException;
import edu.harvard.hul.ois.fits.tools.ToolBase;
import edu.harvard.hul.ois.fits.tools.ToolOutput;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Main extends ToolBase {
    private final Set<Extractor> extractors;
    private static final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
    private boolean enabled = true;

    @Override
    public ToolOutput extractInfo(File file) throws FitsToolException {
        final Element results = new Element("cad-tool-output");

        for(Extractor extractor: extractors) {
            if (extractor.accepts(file.getName())) {
                final DataSource in = new FileDataSource(file);
                try {
                    results.addContent(extractor.run(in, file.getName()));
                } catch (IOException e) {
                    throw new FitsToolException("Error running cadtool extractor " + extractor.getName(), e);
                }
            }
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
        final Set<Extractor> temp = new HashSet<>();
        temp.add(new PdfExtractor());
        extractors = Collections.unmodifiableSet(temp);
    }

    public static void main(String[] args) throws FitsToolException, IOException {
        //TODO: check args, print usage message
        final File file = new File(args[0]);
        final Main main = new Main();
        final ToolOutput results = main.extractInfo(file);
        out.output(results.getToolOutput(), System.out);
    }
}