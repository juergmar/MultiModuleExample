package com.example.email.core.model;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Represents an email attachment.
 */
public class Attachment {
    private final String name;
    private final String contentType;
    private final AttachmentSource source;

    private Attachment(String name, String contentType, AttachmentSource source) {
        this.name = name;
        this.contentType = contentType;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public AttachmentSource getSource() {
        return source;
    }

    /**
     * Create an attachment from a file path
     */
    public static Attachment fromPath(Path path, String name, String contentType) {
        return new Attachment(name, contentType, new PathAttachmentSource(path));
    }

    /**
     * Create an attachment from an input stream
     */
    public static Attachment fromInputStream(InputStream inputStream, String name, String contentType) {
        return new Attachment(name, contentType, new InputStreamAttachmentSource(inputStream));
    }

    /**
     * Create an attachment from a byte array
     */
    public static Attachment fromBytes(byte[] bytes, String name, String contentType) {
        return new Attachment(name, contentType, new ByteArrayAttachmentSource(bytes));
    }

    /**
     * Interface for different attachment sources
     */
    public interface AttachmentSource {
        // Marker interface for different attachment sources
    }

    /**
     * Attachment source from a file path
     */
    public static class PathAttachmentSource implements AttachmentSource {
        private final Path path;

        public PathAttachmentSource(Path path) {
            this.path = path;
        }

        public Path getPath() {
            return path;
        }
    }

    /**
     * Attachment source from an input stream
     */
    public static class InputStreamAttachmentSource implements AttachmentSource {
        private final InputStream inputStream;

        public InputStreamAttachmentSource(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public InputStream getInputStream() {
            return inputStream;
        }
    }

    /**
     * Attachment source from a byte array
     */
    public static class ByteArrayAttachmentSource implements AttachmentSource {
        private final byte[] bytes;

        public ByteArrayAttachmentSource(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }
}
