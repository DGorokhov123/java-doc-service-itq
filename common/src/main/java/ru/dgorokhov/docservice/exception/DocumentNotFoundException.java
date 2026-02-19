package ru.dgorokhov.docservice.exception;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(Long id) {
        super("Document not found: " + id);
    }

    public DocumentNotFoundException(String message) {
        super(message);
    }

}
