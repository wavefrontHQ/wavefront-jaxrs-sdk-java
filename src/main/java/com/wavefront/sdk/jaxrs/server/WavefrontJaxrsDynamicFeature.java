package com.wavefront.sdk.jaxrs.server;

import com.wavefront.config.WavefrontReportingConfig;
import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.jaxrs.reporter.WavefrontJaxrsReporter;

import org.apache.commons.lang3.BooleanUtils;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import static com.wavefront.config.ReportingUtils.constructWavefrontSender;

@Provider
public class WavefrontJaxrsDynamicFeature implements DynamicFeature {
  private final WavefrontJaxrsServerFilter.Builder wfJaxrsFilterBuilder;
  private final WavefrontJaxrsReporter wavefrontJaxrsReporter;
  private Tracer tracer;

  public WavefrontJaxrsDynamicFeature(ApplicationTags applicationTags,
                                      WavefrontReportingConfig wavefrontReportingConfig,
                                      Set<String> headerTags) {
    String source = wavefrontReportingConfig.getSource();
    WavefrontSender wavefrontSender = constructWavefrontSender(wavefrontReportingConfig);
    wavefrontJaxrsReporter = new WavefrontJaxrsReporter.Builder
        (applicationTags).withSource(source).build(wavefrontSender);
    wfJaxrsFilterBuilder = new WavefrontJaxrsServerFilter.Builder
        (wavefrontJaxrsReporter, applicationTags);
    if (BooleanUtils.isTrue(wavefrontReportingConfig.getReportTraces())) {
      WavefrontSpanReporter wfSpanReporter;
      wfSpanReporter = new WavefrontSpanReporter.Builder().withSource(source).build(wavefrontSender);
      this.tracer = new WavefrontTracer.Builder(wfSpanReporter, applicationTags).build();
      wfJaxrsFilterBuilder.withTracer(this.tracer);
      wfJaxrsFilterBuilder.headerTags(headerTags);
      GlobalTracer.register(this.tracer);
    }
  }

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
    wavefrontJaxrsReporter.start();
    featureContext.register(wfJaxrsFilterBuilder.build());
  }

  public Tracer getTracer() {
    return this.tracer;
  }
}

