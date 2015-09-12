package edu.harvard.hul.ois.fits.cad;

import org.jdom.Element;

import javax.activation.DataSource;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class Extractor {
    private final Set<String> extensions = new HashSet<>();
    private final String name;

    protected Extractor(String name, String... extensions) {
        this.name = name;

        //Make sure every extension is lowercase and prefixed by a period
        for(String extension: extensions) {
            extension = extension.toLowerCase();
            if (extension.charAt(0) == '.') {
                this.extensions.add(extension);
            } else {
                this.extensions.add("." + extension);
            }
        }
    }

    public boolean accepts(String filename) {
        filename = filename.toLowerCase();
        for(String extension: extensions) {
            if (filename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    protected abstract void doRun(DataSource ds, String filename, Element result) throws IOException;

    public final Element run(DataSource ds, String filename) throws IOException {
        final Element result = new Element("result");
        result.setAttribute("extractor", name);
        result.setAttribute("file", filename);
        doRun(ds, filename, result);
        return result;
    }

    public String getName() {
        return name;
    }
}
