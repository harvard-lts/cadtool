package edu.harvard.hul.ois.fits.cad;

import java.util.HashSet;
import java.util.Set;

public class FilenameChecker {
    private final Set<String> extensions = new HashSet<>();

    public FilenameChecker(String... extensions) {
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

    /**
     * Whether or not this extractor will accept a file with the given name.
     * Note: These checks are case insensitive.
     *
     * @param filename File name or path to check
     * @return <code>true</code> if the extractor can run on this file, <code>false</code> otherwise
     */
    public final boolean accepts(String filename) {
        filename = filename.toLowerCase();
        for(String extension: extensions) {
            if (filename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    //TODO: a magic number validator?

}
