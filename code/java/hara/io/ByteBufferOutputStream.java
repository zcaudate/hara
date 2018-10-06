package hara.io;

import java.nio.ByteBuffer;
import java.io.OutputStream;
import java.io.IOException;

public class ByteBufferOutputStream extends OutputStream {
    ByteBuffer buf;

    public ByteBufferOutputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public void write(int b) throws IOException {
        buf.put((byte) b);
    }

    public void write(byte[] bytes, int off, int len)
            throws IOException {
        buf.put(bytes, off, len);
    }
}