package ru.dgorokhov.docservice.exception;

public class ApprovalRegistryException extends RuntimeException {

    public ApprovalRegistryException(String message) {
        super("Approval registry error: " + message);
    }

    public ApprovalRegistryException(Long documentId, String message) {
        super("Failed to register approval for document " + documentId + ": " + message);
    }

}
