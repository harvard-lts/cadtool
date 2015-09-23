package edu.harvard.hul.ois.fits.cad;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.jdom.Element;
import org.jdom.IllegalDataException;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Isaac Simmons on 8/27/2015.
 */
public class PdfExtractor extends CadExtractor {
    private final MagicNumberValidator validator = MagicNumberValidator.string("%PDF");

    public PdfExtractor() {
        super("pdf", ".pdf");
    }

    public static void pdfbox_validate(DataSource ds, Element result) throws IOException {
        ValidationResult validationResult;
        final PreflightParser parser = new PreflightParser(ds);
        PreflightDocument document = null;

        try {
            parser.parse();
            document = parser.getPreflightDocument();
            document.validate();
            validationResult = document.getResult();
            document.close();
        } catch (SyntaxValidationException e) {
            validationResult = e.getResult();
        } finally {
            if (document != null) {
                document.close();
            }
        }

        final Map<String, Map<String, Integer>> validationErrors = new HashMap<>();
        for (ValidationResult.ValidationError error : validationResult.getErrorsList()) {
            final String errorCode = error.getErrorCode();
            final String details = error.getDetails();

            Map<String, Integer> match = validationErrors.get(errorCode);
            if (match == null) {
                match = new HashMap<>();
                validationErrors.put(errorCode, match);
            }

            if (match.containsKey(details)) {
                match.put(details, match.get(details) + 1);
            } else {
                match.put(details, 1);
            }
        }
        for(Map.Entry<String, Map<String, Integer>> codeEntry: validationErrors.entrySet()) {
            final String errorCode = codeEntry.getKey();
            for(Map.Entry<String, Integer> detailEntry: codeEntry.getValue().entrySet()) {
                final Element element = new Element("pdf-a-validation-error");
                element.setAttribute("code", errorCode);
                element.setAttribute("details", detailEntry.getKey());
                element.setAttribute("count", Integer.toString(detailEntry.getValue()));
                result.addContent(element);
            }
        }
    }

    private static void appendElement(String elementName, String content, Element base) {
        if (content != null && !content.isEmpty()) {
            try {
                final Element element = new Element(elementName);
                element.setText(content);
                base.addContent(element);
            } catch (IllegalDataException ignored) {
                System.out.println("Invalid XML data for pdf header: " + elementName);
            }
        }
    }

    private static void appendElement(String elementName, Calendar content, Element base) {
        if (content != null) {
            final Element element = new Element(elementName);
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            element.setText(df.format(content.getTime()));
            base.addContent(element);
        }
    }

    @Override
    public void doRun(DataSource ds, String filename, Element result) throws IOException, ValidationException {
        validator.validate(ds.getInputStream());

        final Element identity = new Element("identity");
        identity.setAttribute("mimetype", "application/pdf");
        identity.setAttribute("format", "Portable Document Format");
//        identity.setAttribute("version", ??);  //TODO: doesn't look like PDF revision is available to me from pdfbox
        result.addContent(identity);

        try (final InputStream in = ds.getInputStream()) {
            final PDDocument doc = PDDocument.load(in);
            final PDDocumentInformation info = doc.getDocumentInformation();
            try {
                appendElement("title", info.getTitle(), result);
                appendElement("author", info.getAuthor(), result);
                appendElement("subject", info.getSubject(), result);
                appendElement("keywords", info.getKeywords(), result);
                appendElement("creator", info.getCreator(), result);
                appendElement("producer", info.getProducer(), result);
                appendElement("created", info.getCreationDate(), result);
                appendElement("modified", info.getModificationDate(), result);
            } catch (IOException ex) {
                System.out.println("Trouble parsing pdf metadata: " + ex.getMessage());
            }
            final PDDocumentCatalog cat = doc.getDocumentCatalog();

            for(COSObject o: doc.getDocument().getObjects()) {
                final COSBase item = o.getObject();
                if (item instanceof COSStream) {
                    final COSStream stream = (COSStream) item;
                    if (stream.containsKey(COSName.TYPE) && "3D".equals(stream.getNameAsString(COSName.TYPE))) {
                        final Element streamElement = new Element("embedded-3d-content");
                        if (stream.containsKey(COSName.SUBTYPE)) {
                            streamElement.setAttribute("type", stream.getNameAsString(COSName.SUBTYPE));
                        }
                        streamElement.setAttribute("bytes", Long.toString(stream.getFilteredLength()));
                        result.addContent(streamElement);
                        //TODO: actually pull the stream itself and decode it?
                    }
                }
            }

            //TODO: File attachments?

            final Element annotationElement = new Element("annotation-3d");
            annotationElement.setAttribute("present", "false");
            pageloop: for (Object o: cat.getAllPages()) {
                if (o instanceof PDPage) {
                    final PDPage page = (PDPage) o;
                    for (PDAnnotation annotation: page.getAnnotations()) {
                        if ("3D".equals(annotation.getSubtype())) {
                            annotationElement.setAttribute("present", "true");
                            break pageloop;
                        }
                    }
                }
            }
            result.addContent(annotationElement);
            doc.close();
        }
        pdfbox_validate(ds, result);
    }
}
