package com.osi.conquest;


import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * @author Paul Folbrecht
 */
public class ConquestRuntimeException extends RuntimeException {
    public Exception _cause;

    public ConquestRuntimeException(String msg) {
        super(msg);
    }

    public ConquestRuntimeException(Exception e) {
        super("Cause: " + e.getClass() + ": " + e.getMessage());
        _cause = e;
    }

    public ConquestRuntimeException(String msg, Exception e) {
        super(msg + ": Cause: " + e.getClass() + ": " + e.getMessage());
        _cause = e;
    }

    public Throwable getCause() {
        return _cause;
    }

    public String toString() {
        return getMessage();
    }

    public String getMessage() {
        StringBuffer buffer = new StringBuffer(200);

        buffer.append(super.getMessage());
        if (_cause != null) {
            buffer.append(": Cause: ");
            buffer.append(_cause.getMessage());
        }

        return buffer.toString();
    }

    /**
     * Overridden to append the stack trace of the cause exception.
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Overridden to append the stack trace of the cause exception.
     */
    public void printStackTrace(PrintStream stream) {
        stream.println("Stack trace of this exception: ");
        super.printStackTrace(stream);

        if (_cause != null) {
            stream.println("\nStack trace of cause: ");
            _cause.printStackTrace(stream);
        }
    }

    /**
     * Overridden to append the stack trace of the cause exception.
     */
    public void printStackTrace(PrintWriter writer) {
        writer.println("Stack trace of this exception: ");
        super.printStackTrace(writer);

        if (_cause != null) {
            writer.println("\nStack trace of cause: ");
            _cause.printStackTrace(writer);
        }
    }
}
