import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;

import javax.activation.FileDataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

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

//        //attachments are stored as part of the "names" dictionary in the document catalog
//        PDDocumentNameDictionary names = new PDDocumentNameDictionary( fd.getDocumentCatalog() );
//        names.setEmbeddedFiles( efTree );
//        doc.getDocumentCatalog().setNames( names );
//    }

    public static void main(String[] args) throws Exception {
        final PDDocument doc = PDDocument.load( FILENAME );

        final PDDocumentCatalog cat = doc.getDocumentCatalog();

//        cat.getNames().

        for(COSObject o: doc.getDocument().getObjects()) {
            final COSBase item = o.getObject();
            if (item instanceof COSStream) {
                System.out.println("Found a stream");
                final COSStream stream = (COSStream) item;
                for(Entry<COSName, COSBase> s: stream.entrySet()) {
                    System.out.println(s.getKey().getName() + " " + s.getValue().toString());
                }
            }
        }

        for (PDPage page: ((List<PDPage>) cat.getAllPages())) {
            for (PDAnnotation annotation: page.getAnnotations()) {
                System.out.println("annotation here: " + annotation.getSubtype());
            }
        }
    }
}