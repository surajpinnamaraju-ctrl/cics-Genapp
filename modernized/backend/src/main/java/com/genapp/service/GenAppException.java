package com.genapp.service;

/** Carries a legacy CA-RETURN-CODE (00/01/02/70/90/98/99) to the web layer. */
public class GenAppException extends RuntimeException {

    private final String returnCode;

    public GenAppException(String returnCode, String message) {
        super(message);
        this.returnCode = returnCode;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public static GenAppException notFound(String what) {
        return new GenAppException("01", what + " not found");
    }

    public static GenAppException invalid(String message) {
        return new GenAppException("98", message);
    }

    public static GenAppException unsupported(String message) {
        return new GenAppException("99", message);
    }

    public static GenAppException conflict(String message) {
        return new GenAppException("02", message);
    }

    public static GenAppException customerMissing() {
        return new GenAppException("70", "Customer does not exist");
    }

    public static GenAppException internal(String message) {
        return new GenAppException("90", message);
    }
}
