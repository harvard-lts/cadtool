package edu.harvard.hul.ois.fits.cad;

import org.jdom.Element;

import javax.activation.DataSource;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class CadExtractor {
    protected final Set<String> extensions;
    protected final String name;
    protected final String defaultFormat;
    protected final String defaultMime;

    protected CadExtractor(String name, String defaultFormat, String defaultMime, String... extensions) {
        this.name = name;
        this.defaultFormat = defaultFormat;
        this.defaultMime = defaultMime;

        //Make sure every extension is lowercase and prefixed by a period
        final Set<String> temp = new HashSet<>(extensions.length);
        for(String extension: extensions) {
            extension = extension.toLowerCase();
            if (extension.charAt(0) == '.') {
                temp.add(extension);
            } else {
                temp.add("." + extension);
            }
        }
        this.extensions = Collections.unmodifiableSet(temp);
    }

    public String getDefaultFormat() {
        return defaultFormat;
    }

    public String getDefaultMime() {
        return defaultMime;
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
        final Element result = new Element("cad-tool-result");
        result.setAttribute("extractor", name);
        result.setAttribute("file", filename);
        doRun(ds, filename, result);
        return result;
    }

    public String getName() {
        return name;
    }

    public Set<String> getExtensions() {
        return extensions;
    }
}
