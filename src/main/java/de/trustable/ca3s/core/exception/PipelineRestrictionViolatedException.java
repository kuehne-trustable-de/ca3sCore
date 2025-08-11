package de.trustable.ca3s.core.exception;

public class PipelineRestrictionViolatedException extends RuntimeException{

    public PipelineRestrictionViolatedException() {}

    public PipelineRestrictionViolatedException(String msg) {
        super(msg);
    }
}
