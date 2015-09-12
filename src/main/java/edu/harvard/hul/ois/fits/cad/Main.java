package edu.harvard.hul.ois.fits.cad;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Main {
    private final Set<Extractor> extractors;
    private static final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

    public Main() {
        final Set<Extractor> temp = new HashSet<>();
        temp.add(new PdfExtractor());
        extractors = Collections.unmodifiableSet(temp);
    }

    public void run(DataSource ds, String filename) throws IOException {
        final Element results = new Element("results");

        for(Extractor extractor: extractors) {
            if (extractor.accepts(filename)) {
                results.addContent(extractor.run(ds, filename));
            }
        }

        out.output(results, System.out);
    }

    public static void main(String[] args) {
        //TODO: check args, print usage message
        final String filename = args[0];
        final DataSource in = new FileDataSource(filename);
        final Main main = new Main();

        try {
            main.run(in, filename);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}