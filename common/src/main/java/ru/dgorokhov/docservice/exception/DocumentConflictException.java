package ru.dgorokhov.docservice.exception;

public class DocumentConflictException extends RuntimeException {

    public DocumentConflictException(Long id, String message) {
        super("Conflict for document " + id + ": " + message);
    }

    public DocumentConflictException(String message) {
        super(message);
    }

}
