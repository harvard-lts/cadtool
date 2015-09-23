package edu.harvard.hul.ois.fits.cad;

import org.jdom.Element;

import javax.activation.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Isaac Simmons on 9/13/2015.
 */
public class DxfExtractor extends CadExtractor {
    private static final int GROUPCODE_READAHEAD_LIMIT = 64;

    public DxfExtractor() {
        super("dxf", "Drawing eXchange Format", "image/vnd.dxf", ".dxf");
    }

    private static void seekToHeaderStart(BufferedReader reader) throws ValidationException, IOException {
        //Looking for lines that read, in sequence, {"0", "SECTION", "2", "HEADER"}
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if ("0".equals(line)) {
                line = reader.readLine();
                if (line == null) {
                    throw new ValidationException("Unexpected end of DXF file");
                }
                if (!"SECTION".equals(line.trim())) {
                    throw new ValidationException("Expected \"SECTION\" in DXF file but found: " +
                            line.substring(0, Math.min(line.length(), 40)));
                }
                line = reader.readLine();
                if (line == null) {
                    throw new ValidationException("Unexpected end of DXF file");
                }
                if (!"2".equals(line.trim())) {
                    throw new ValidationException("Expected groupcode \"2\" after SECTION entry but found: " +
                            line.substring(0, Math.min(line.length(), 40)));
                }
                line = reader.readLine();
                if (line == null) {
                    throw new ValidationException("Unexpected end of DXF file");
                }
                if ("HEADER".equals(line.trim())) {
                    return;
                }
            }
        }
        throw new ValidationException("No header encountered before end of DXF file");
    }

    private static String readHeaderVarName(BufferedReader reader) throws ValidationException, IOException {
        final String groupCode = reader.readLine();
        if (groupCode == null) {
            throw new ValidationException("Unexpected end of DXF file");
        }
        if ("0".equals(groupCode.trim())) {
            final String endsec = reader.readLine();
            if (endsec == null) {
                throw new ValidationException("Unexpected end of DXF file");
            }
            if (! "ENDSEC".equals(endsec.trim())) {
                throw new IOException("Expected \"ENDSEC\" at end of DXF header but got: " +
                        endsec.substring(0, Math.min(endsec.length(), 40)));
            }
            return null;
        }

        if (! "9".equals(groupCode.trim())) {
            throw new ValidationException("Unexpected group code in DXF header: " +
                    groupCode.substring(0, Math.min(groupCode.length(), 40)));
        }

        String varName = reader.readLine();
        if (varName == null) {
            throw new ValidationException("Unexpected end of DXF file");
        }
        varName = varName.trim();
        if (! varName.startsWith("$")) {
            throw new ValidationException("DXF header variable should begin with $: " +
                    varName.substring(0, Math.min(varName.length(), 40)));
        }
        return varName.substring(1);
    }

    private static List<String> readHeaderVarValues(BufferedReader reader) throws ValidationException, IOException {
        final List<String> values = new ArrayList<>();
        reader.mark(GROUPCODE_READAHEAD_LIMIT);
        String groupCode = reader.readLine();
        if (groupCode == null) {
            throw new ValidationException("Unexpected end of DXF file");
        }
        groupCode = groupCode.trim();

        while(!"9".equals(groupCode) && !"0".equals(groupCode)) {
            final String value = reader.readLine();
            if (value == null) {
                throw new ValidationException("Unexpected end of DXF file");
            }
            values.add(value.trim());
            reader.mark(GROUPCODE_READAHEAD_LIMIT);
            groupCode = reader.readLine();
            if (groupCode == null) {
                throw new ValidationException("Unexpected end of DXF file");
            }
            groupCode = groupCode.trim();
        }

        reader.reset(); //Put the stream back to before I read the last groupCode (0 or 9)
        if (values.isEmpty()) {
            throw new ValidationException("Got DXF header with no values");
        }
        return values;
    }

    private Map<String, List<String>> readHeader(InputStream in) throws IOException, ValidationException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final Map<String, List<String>> headerValues = new HashMap<>();

        seekToHeaderStart(reader);

        String varName;
        while((varName = readHeaderVarName(reader)) != null) {
            headerValues.put(varName, readHeaderVarValues(reader));
        }

        return headerValues;
    }

    @Override
    protected void doRun(DataSource ds, String filename, Element result) throws IOException, ValidationException {
        final Map<String, List<String>> entries = readHeader(ds.getInputStream());
        for (Map.Entry<String, List<String>> entry: entries.entrySet()) {
            final Element headerElement = new Element("header");
            headerElement.setAttribute("name", entry.getKey());
            for (String value: entry.getValue()) {
                final Element valueElement = new Element("value");
                valueElement.setText(value);
                headerElement.addContent(valueElement);
            }
            result.addContent(headerElement);
        }
    }
}
