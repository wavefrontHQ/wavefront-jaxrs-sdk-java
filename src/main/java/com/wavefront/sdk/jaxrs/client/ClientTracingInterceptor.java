package com.wavefront.sdk.jaxrs.client;

import static com.wavefront.sdk.jaxrs.internal.SpanWrapper.PROPERTY_NAME;

import io.opentracing.Tracer;

import com.wavefront.sdk.jaxrs.internal.CastUtils;
import com.wavefront.sdk.jaxrs.internal.SpanWrapper;
import com.wavefront.sdk.jaxrs.serialization.InterceptorSpanDecorator;
import com.wavefront.sdk.jaxrs.serialization.TracingInterceptor;

import java.util.List;

import javax.ws.rs.ext.InterceptorContext;

public class ClientTracingInterceptor extends TracingInterceptor {

  public ClientTracingInterceptor(Tracer tracer, List<InterceptorSpanDecorator> spanDecorators) {
    super(tracer, spanDecorators);
  }

  @Override
  protected SpanWrapper findSpan(InterceptorContext context) {
    return CastUtils.cast(context.getProperty(PROPERTY_NAME), SpanWrapper.class);
  }
}
