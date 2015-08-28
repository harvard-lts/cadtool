package edu.harvard.hul.ois.fits.cad;

import edu.harvard.hul.ois.fits.cad.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.activation.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
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

    public final void run(DataSource ds, String filename, Element root) throws IOException {
        final Element result = root.getOwnerDocument().createElement("result");
        result.setAttribute("extractor", name);
        result.setAttribute("file", filename);
        root.appendChild(result);
        doRun(ds, filename, result);
    }

    public final void run(DataSource ds, String filename) throws IOException {
        //TODO: remove this, put it somewhere else
        final Document doc;
        try {
            doc = XmlUtil.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        final Element results = doc.createElement("results");
        doc.appendChild(results);
        run(ds, filename, results);
        try {
            XmlUtil.printXml(doc);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
