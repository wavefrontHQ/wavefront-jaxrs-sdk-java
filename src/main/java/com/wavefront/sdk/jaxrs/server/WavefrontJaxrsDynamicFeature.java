package com.wavefront.sdk.jaxrs.server;

import com.wavefront.config.WavefrontReportingConfig;
import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.jaxrs.reporter.WavefrontJaxrsReporter;

import org.apache.commons.lang3.BooleanUtils;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import io.opentracing.Tracer;

import static com.wavefront.config.ReportingUtils.constructWavefrontSender;

@Provider
public class WavefrontJaxrsDynamicFeature implements DynamicFeature {
  private final ApplicationTags applicationTags;
  private final WavefrontReportingConfig wavefrontReportingConfig;

  public WavefrontJaxrsDynamicFeature(ApplicationTags applicationTags,
                                      WavefrontReportingConfig wavefrontReportingConfig) {
    this.applicationTags = applicationTags;
    this.wavefrontReportingConfig = wavefrontReportingConfig;
  }

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
    WavefrontReportingConfig wfReportingConfig = this.wavefrontReportingConfig;
    String source = wfReportingConfig.getSource();
    WavefrontSender wavefrontSender = constructWavefrontSender(wfReportingConfig);
    WavefrontJaxrsReporter wfJaxrsReporter = new WavefrontJaxrsReporter.Builder
        (applicationTags).withSource(source).build(wavefrontSender);
    WavefrontJaxrsServerFilter.Builder wfJaxrsFilterBuilder = new WavefrontJaxrsServerFilter.Builder
        (wfJaxrsReporter, applicationTags);
    if (BooleanUtils.isTrue(wfReportingConfig.getReportTraces())) {
      WavefrontSpanReporter wfSpanReporter;
      wfSpanReporter = new WavefrontSpanReporter.Builder().withSource(source).build(wavefrontSender);
      Tracer tracer = new WavefrontTracer.Builder(wfSpanReporter, applicationTags).build();
      wfJaxrsFilterBuilder.withTracer(tracer);
    }
    wfJaxrsReporter.start();
    WavefrontJaxrsServerFilter wfJaxrsServerFilter = wfJaxrsFilterBuilder.build();
    featureContext.register(wfJaxrsServerFilter);
  }

}

