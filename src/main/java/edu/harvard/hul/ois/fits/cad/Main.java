package edu.harvard.hul.ois.fits.cad;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Main {
    private final Set<Extractor> extractors;

    public Main() {
        final Set<Extractor> temp = new HashSet<>();
        temp.add(new PdfExtractor());
        extractors = Collections.unmodifiableSet(temp);
    }

    public void run(DataSource ds, String filename) throws IOException {
        boolean anyMatch = false;
        for(Extractor extractor: extractors) {
            if (extractor.accepts(filename)) {
                anyMatch = true;
                extractor.run(ds, filename);
            }
        }
        if (!anyMatch) {
            System.out.println("Warning: No metadata extractors matched for file " + filename);
        }
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