package com.wavefront.sdk.jaxrs.client;

import io.opentracing.Scope;
import io.opentracing.Span;

/**
 * Wrapper class used for exchanging span between filters.
 *
 * @author Amit Wadhwani (amitw@vmware.com).
 */
public class SpanWrapper {
    private Scope scope;
    private Span span;

    public SpanWrapper(Span span, Scope scope) {
        this.span = span;
        this.scope = scope;
    }

    public Span getSpan() {
        return span;
    }

    public Scope getScope() {
        return scope;
    }
}
