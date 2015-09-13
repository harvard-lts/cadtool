package edu.harvard.hul.ois.fits.cad;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.jdom.Element;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Isaac Simmons on 8/27/2015.
 */
public class PdfExtractor extends CadExtractor {
    private final MagicNumberValidator validator = MagicNumberValidator.string("%PDF");

    public PdfExtractor() {
        super("pdf", "Portable Document Format", "application/pdf", ".pdf");
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

    @Override
    public void doRun(DataSource ds, String filename, Element result) throws IOException, ValidationException {
        validator.validate(ds.getInputStream());
        try (final InputStream in = ds.getInputStream()) {
            final PDDocument doc = PDDocument.load(in);
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
