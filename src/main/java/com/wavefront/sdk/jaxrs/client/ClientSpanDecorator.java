package com.wavefront.sdk.jaxrs.client;

import io.opentracing.Span;

import com.wavefront.sdk.jaxrs.internal.URIUtils;

import io.opentracing.tag.Tags;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

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
   * Adds standard tags: {@link io.opentracing.tag.Tags#SPAN_KIND}, {@link
   * io.opentracing.tag.Tags#PEER_HOSTNAME}, {@link io.opentracing.tag.Tags#PEER_PORT}, {@link
   * io.opentracing.tag.Tags#HTTP_METHOD}, {@link io.opentracing.tag.Tags#HTTP_URL} and {@link
   * io.opentracing.tag.Tags#HTTP_STATUS}
   */
  ClientSpanDecorator STANDARD_TAGS = new ClientSpanDecorator() {
    @Override
    public void decorateRequest(ClientRequestContext requestContext, Span span) {
      Tags.COMPONENT.set(span, "jaxrs");
      Tags.PEER_HOSTNAME.set(span, requestContext.getUri().getHost());
      Tags.PEER_PORT.set(span, requestContext.getUri().getPort());

      Tags.HTTP_METHOD.set(span, requestContext.getMethod());

      String url = URIUtils.url(requestContext.getUri());
      if (url != null) {
        Tags.HTTP_URL.set(span, url);
      }
    }

    @Override
    public void decorateResponse(ClientResponseContext responseContext, Span span) {
      Tags.HTTP_STATUS.set(span, responseContext.getStatus());
    }
  };

  /**
   * As operation name provides a path injected by Wavefront at server side.
   */
  ClientSpanDecorator WF_PATH_OPERATION_NAME = new ClientSpanDecorator() {
    String methodName = "";
    @Override
    public void decorateRequest(ClientRequestContext clientRequestContext, Span span) {
      this.methodName = clientRequestContext.getMethod();
    }

    @Override
    public void decorateResponse(ClientResponseContext response, Span span) {
      span.setOperationName(this.methodName + "-" + String.valueOf(response.getHeaders().
          getFirst("WF_SPAN_OPERATION_NAME")));
    }
  };

}
