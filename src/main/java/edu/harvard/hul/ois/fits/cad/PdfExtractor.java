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

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Isaac Simmons on 8/27/2015.
 */
public class PdfExtractor implements Extractor {
    private final FilenameChecker filenameChecker = new FilenameChecker("pdf");

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
    public void run(DataSource ds, String filename) throws IOException {
        try (final InputStream in = ds.getInputStream()) {
            final PDDocument doc = PDDocument.load(in);
            final PDDocumentCatalog cat = doc.getDocumentCatalog();

            for(COSObject o: doc.getDocument().getObjects()) {
                final COSBase item = o.getObject();
                if (item instanceof COSStream) {
//                System.out.println("Found a stream");
                    final COSStream stream = (COSStream) item;
                    if (stream.containsKey(COSName.TYPE) && "3D".equals(stream.getNameAsString(COSName.TYPE))) {
                        if (stream.containsKey(COSName.SUBTYPE)) {
                            System.out.println("Embedded 3D content found: " + stream.getNameAsString(COSName.SUBTYPE));
                        } else {
                            System.out.println("Embedded 3D content found: Unknown subtype");
                        }
                    }
                    //TODO: other keys contain some viewport stuff? Wonder if there's anything interesting there for 2d stuff?
//                for(Entry<COSName, COSBase> s: stream.entrySet()) {
//                    System.out.println(s.getKey().getName() + " " + s.getValue().toString());
//                }

                    //TODO: actually pull the stream itself and decode it?

                }
            }

            //TODO: File attachments?

            for (PDPage page: ((List<PDPage>) cat.getAllPages())) {
                for (PDAnnotation annotation: page.getAnnotations()) {
                    if ("3D".equals(annotation.getSubtype())) {
                        System.out.println("3D annotation present");
                    }
                }
            }
        }
//        pdfbox_validate(ds);
    }

    @Override
    public boolean accepts(String filename) {
        return filenameChecker.accepts(filename);
    }
}
