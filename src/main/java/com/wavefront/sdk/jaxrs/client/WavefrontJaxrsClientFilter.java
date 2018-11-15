package com.wavefront.sdk.jaxrs.client;

import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.common.application.HeartbeaterService;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;

import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import static com.wavefront.sdk.jaxrs.Constants.JAXRS_CLIENT_COMPONENT;
import static com.wavefront.sdk.jaxrs.Constants.PROPERTY_NAME;
import static com.wavefront.sdk.jaxrs.Constants.CHILD_OF;

/**
 * A filter to generate Wavefront client side span for JAX-RS based API requests/responses.
 *
 * @author Hao Song (songhao@vmware.com).
 */
public class WavefrontJaxrsClientFilter implements ClientRequestFilter, ClientResponseFilter {

  @Nullable
  private final Tracer tracer;
  private final List<ClientSpanDecorator> spanDecorators;

  public WavefrontJaxrsClientFilter(WavefrontSender wfSender, ApplicationTags applicationTags,
                                    @Nonnull String source, @Nullable Tracer tracer) {
    this.tracer = tracer;
    this.spanDecorators = Arrays.asList(ClientSpanDecorator.STANDARD_TAGS,
        ClientSpanDecorator.WF_PATH_OPERATION_NAME);
    HeartbeaterService heartbeaterService = new HeartbeaterService(wfSender, applicationTags,
        JAXRS_CLIENT_COMPONENT, source);
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    if (requestContext.getProperty(PROPERTY_NAME) != null) {
      return;
    }
    if (tracer != null) {
      Tracer.SpanBuilder spanBuilder = tracer.buildSpan(requestContext.getMethod())
          .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
      SpanContext parentSpanContext = (SpanContext) requestContext.getProperty(CHILD_OF);
      if (parentSpanContext != null) {
        spanBuilder.ignoreActiveSpan().asChildOf(parentSpanContext);
      }
      final Span span = spanBuilder.start();
      for (ClientSpanDecorator decorator : spanDecorators) {
        decorator.decorateRequest(requestContext, span);
      }
      tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new ClientHeadersInjectTextMap(
          requestContext.getHeaders()));
      requestContext.setProperty(PROPERTY_NAME, span);
    }
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
      throws IOException {
    if (tracer != null) {
      Span span = (Span) requestContext.getProperty(PROPERTY_NAME);
      if (span != null) {
        for (ClientSpanDecorator decorator : spanDecorators) {
          decorator.decorateResponse(responseContext, span);
        }
        span.finish();
      }
    }
  }
}
