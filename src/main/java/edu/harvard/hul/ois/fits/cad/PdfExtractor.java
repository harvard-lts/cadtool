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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by Isaac Simmons on 8/27/2015.
 */
public class PdfExtractor extends Extractor {
    protected PdfExtractor() {
        super("pdf", "pdf");
    }

    public static void pdfbox_validate(DataSource ds, String filename) throws IOException {
        ValidationResult result;
        final PreflightParser parser = new PreflightParser(ds);
        try {
            parser.parse();
            PreflightDocument document = parser.getPreflightDocument();
            document.validate();
            result = document.getResult();
            document.close();
        } catch (SyntaxValidationException e) {
            result = e.getResult();
        }

        if (result.isValid()) {
            System.out.println("The file " + filename + " is a valid PDF/A-1b file");
        } else {
            System.out.println("The file " + filename + " is not valid, error(s) :");
            for (ValidationResult.ValidationError error : result.getErrorsList()) {
                System.out.println(error.getErrorCode() + " : " + error.getDetails());
            }
        }
    }

    @Override
    public void doRun(DataSource ds, String filename, Element result) throws IOException {
        final Document outputDoc = result.getOwnerDocument();

        try (final InputStream in = ds.getInputStream()) {
            final PDDocument doc = PDDocument.load(in);
            final PDDocumentCatalog cat = doc.getDocumentCatalog();

            for(COSObject o: doc.getDocument().getObjects()) {
                final COSBase item = o.getObject();
                if (item instanceof COSStream) {
                    final COSStream stream = (COSStream) item;
                    if (stream.containsKey(COSName.TYPE) && "3D".equals(stream.getNameAsString(COSName.TYPE))) {
                        final Element streamElement = outputDoc.createElement("embedded-3d-content");
                        if (stream.containsKey(COSName.SUBTYPE)) {
                            streamElement.setAttribute("type", stream.getNameAsString(COSName.SUBTYPE));
                        }
                        streamElement.setAttribute("bytes", Long.toString(stream.getFilteredLength()));
                        result.appendChild(streamElement);
                        //TODO: actually pull the stream itself and decode it?
                    }
                }
            }

            //TODO: File attachments?

            final Element annotationElement = outputDoc.createElement("annotation-3d");
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
            result.appendChild(annotationElement);
        }
//        pdfbox_validate(ds);
    }
}
