package edu.harvard.hul.ois.fits.cad;

import org.jdom.Element;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by Isaac Simmons on 9/13/2015.
 */
public class DwgExtractor extends CadExtractor {
    private final MagicNumberValidator validator = MagicNumberValidator.string("AC10", false);

    public DwgExtractor() {
        super("dwg", ".dwg");
    }

    @Override
    protected void doRun(DataSource ds, String filename, Element result) throws IOException, ValidationException {
        try (final InputStream in = ds.getInputStream()) {
            validator.validate(in);
            final byte[] versionBytes = MagicNumberValidator.readBytes(in, 2);
            final String versionString = new String(versionBytes, StandardCharsets.US_ASCII);

            final Element identity = new Element("identity");
            identity.setAttribute("mimetype", "image/vnd.dwg");
            identity.setAttribute("format", "AutoCad Drawing");
            final String version;
            switch(versionString) {
                case "01": version = "AutoCAD R2.22"; break;
                case "02": version = "AutoCAD R2.50"; break;
                case "03": version = "AutoCAD R2.60"; break;
                case "04": version = "AutoCAD R9"; break;
                case "06": version = "AutoCAD R10"; break;
                case "09": version = "AutoCAD R11/R12"; break;
                case "10": case "11": case "12": version = "AutoCAD R13"; break;
                case "13": case "14": version = "AutoCAD R14"; break;
                case "15": version = "AutoCAD R2000"; break;
                case "18": version = "AutoCAD R2004"; break;
                case "21": version = "AutoCAD R2007"; break;
                case "24": version = "AutoCAD R2010"; break;
                case "27": version = "AutoCAD R2014"; break;
                default:
                    System.out.println("Unrecognized AutoCAD version string: " + versionString);
                    version = null;
            }
            if (version != null) {
                identity.setAttribute("version", version);
            }
            result.addContent(identity);
        }
    }
}
