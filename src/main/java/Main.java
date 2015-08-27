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

import javax.activation.FileDataSource;
import java.io.IOException;
import java.util.List;

public class Main {
    public static final String FILENAME = "C:\\Users\\Isaac Simmons\\Google Drive\\2D3DFormats\\Sample Files\\3D PDF Harvard\\DOC00175.pdf";
//    public static final String FILENAME = "C:\\Users\\Isaac Simmons\\Google Drive\\2D3DFormats\\Sample Files\\3D PDF pdf3d.com\\DominicNotman_HydraulicChuck.pdf";

    public static void pdfbox_validate() throws IOException {
        ValidationResult result;
        final FileDataSource fd = new FileDataSource(FILENAME);
        final PreflightParser parser = new PreflightParser(fd);
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
            System.out.println("The file " + FILENAME + " is a valid PDF/A-1b file");
        } else {
            System.out.println("The file" + FILENAME + " is not valid, error(s) :");
            for (ValidationResult.ValidationError error : result.getErrorsList()) {
                System.out.println(error.getErrorCode() + " : " + error.getDetails());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final PDDocument doc = PDDocument.load( FILENAME );

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
}