package org.bfreuden.docxgen;

import java.io.IOException;
import java.io.OutputStream;

public class CloseShieldOutputStream extends OutputStream {

    private final OutputStream delegate;

    public CloseShieldOutputStream(OutputStream os) {
        this.delegate = os;
    }

    public static OutputStream nullOutputStream() {
        return OutputStream.nullOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        delegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
    }
}
