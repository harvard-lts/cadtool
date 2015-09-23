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
    public CadToolResult run(DataSource ds, String filename) throws IOException, ValidationException {
        final CadToolResult result = new CadToolResult(name, filename);
        try (final InputStream in = ds.getInputStream()) {
            validator.validate(in);
            final byte[] versionBytes = MagicNumberValidator.readBytes(in, 2);
            final String versionString = new String(versionBytes, StandardCharsets.US_ASCII);

            result.mimetype = "image/vnd.dwg";
            result.formatName = "AutoCad Drawing";
            switch(versionString) {
                case "01": result.formatVersion = "AutoCAD R2.22"; break;
                case "02": result.formatVersion = "AutoCAD R2.50"; break;
                case "03": result.formatVersion = "AutoCAD R2.60"; break;
                case "04": result.formatVersion = "AutoCAD R9"; break;
                case "06": result.formatVersion = "AutoCAD R10"; break;
                case "09": result.formatVersion = "AutoCAD R11/R12"; break;
                case "10": case "11": case "12": result.formatVersion = "AutoCAD R13"; break;
                case "13": case "14": result.formatVersion = "AutoCAD R14"; break;
                case "15": result.formatVersion = "AutoCAD R2000"; break;
                case "18": result.formatVersion = "AutoCAD R2004"; break;
                case "21": result.formatVersion = "AutoCAD R2007"; break;
                case "24": result.formatVersion = "AutoCAD R2010"; break;
                case "27": result.formatVersion = "AutoCAD R2014"; break;
                default: System.out.println("Unrecognized AutoCAD version string: " + versionString);
            }
            return result;
        }
    }
}
