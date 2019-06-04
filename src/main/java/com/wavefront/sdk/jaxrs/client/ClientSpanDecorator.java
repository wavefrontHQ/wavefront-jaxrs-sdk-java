package com.wavefront.sdk.jaxrs.client;

import java.net.MalformedURLException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

import io.opentracing.Span;
import io.opentracing.tag.Tags;

import static com.wavefront.sdk.jaxrs.Constants.JAXRS_CLIENT_COMPONENT;
import static com.wavefront.sdk.jaxrs.Constants.WF_SPAN_HEADER;

/**
 * Decorators for span tags and operation name.
 *
 * @author Hao Song (songhao@vmware.com).
 */
public interface ClientSpanDecorator {

  /**
   * Decorate get by incoming object.
   */
  void decorateRequest(ClientRequestContext requestContext, Span span);

  /**
   * Decorate spans by outgoing object.
   */
  void decorateResponse(ClientResponseContext responseContext, Span span);

  /**
   * Adds standard tags:
   * {@link io.opentracing.tag.Tags#SPAN_KIND}, {@link io.opentracing.tag.Tags#COMPONENT},
   * {@link io.opentracing.tag.Tags#HTTP_METHOD}, {@link io.opentracing.tag.Tags#HTTP_URL},
   * {@link io.opentracing.tag.Tags#HTTP_STATUS}, {@link io.opentracing.tag.Tags#ERROR}
   */
  ClientSpanDecorator STANDARD_TAGS = new ClientSpanDecorator() {
    @Override
    public void decorateRequest(ClientRequestContext requestContext, Span span) {
      Tags.COMPONENT.set(span, JAXRS_CLIENT_COMPONENT);
      Tags.HTTP_METHOD.set(span, requestContext.getMethod());
      try {
        Tags.HTTP_URL.set(span, requestContext.getUri().toURL().toString());
      } catch (MalformedURLException e) {
        // ignoring returning null
      }
    }

    @Override
    public void decorateResponse(ClientResponseContext responseContext, Span span) {
      Tags.HTTP_STATUS.set(span, responseContext.getStatus());
      int statusCode = responseContext.getStatus();
      if (statusCode >= 400 && statusCode <= 599) {
        Tags.ERROR.set(span, true);
      }
    }
  };

  /**
   * Set operation of the span using the server-side path injected by Wavefront Filter.
   * For non-instrumented server side, the operation name will be just HTTP method name.
   */
  ClientSpanDecorator WF_PATH_OPERATION_NAME = new ClientSpanDecorator() {
    ThreadLocal<String> methodName = new ThreadLocal<>();

    @Override
    public void decorateRequest(ClientRequestContext clientRequestContext, Span span) {
      this.methodName.set(clientRequestContext.getMethod());
    }

    @Override
    public void decorateResponse(ClientResponseContext response, Span span) {
      String operationName = this.methodName.get();
      if (response.getHeaders().containsKey(WF_SPAN_HEADER)) {
        operationName += "-" + String.valueOf(response.getHeaders().getFirst(WF_SPAN_HEADER));
      }
      span.setOperationName(operationName);
    }
  };

}
