package edu.harvard.hul.ois.fits.cad;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by Isaac Simmons on 8/27/2015.
 */
public class MagicNumberValidator {
    private final byte[] magic;

    private MagicNumberValidator(byte[] magic) {
        this.magic = magic;
    }

    public static MagicNumberValidator hex(String hex) {
        if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }

        return new MagicNumberValidator(DatatypeConverter.parseHexBinary(hex));
    }

    public static MagicNumberValidator string(String s) {
        try {
            return MagicNumberValidator.string(s, Charset.defaultCharset().name());
        } catch(UnsupportedEncodingException ex) {
            throw new RuntimeException("System default charset not found", ex);
        }
    }

    public static MagicNumberValidator string(String s, String charset) throws UnsupportedEncodingException {
        return new MagicNumberValidator(s.getBytes(charset));
    }

    public static MagicNumberValidator bytes(byte[] bytes) {
        return new MagicNumberValidator(bytes);
    }

    public boolean validate(InputStream in) throws IOException {
        //TODO: might I get something with a UTF-8 (or other) BOM?
        try {
            final byte[] buf = new byte[magic.length];

            int offset = 0;

            while(offset < magic.length) {
                final int read = in.read(buf, offset, magic.length - offset);
                if (read == -1) {
                    return false;
                }
                offset += read;
            }

            return Arrays.equals(buf, magic);
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {}
        }
    }
}
