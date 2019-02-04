package com.wavefront.sdk.jaxrs;

import com.wavefront.sdk.jaxrs.client.WavefrontJaxrsClientFilter;

import io.opentracing.References;

/**
 * JAX-RS Client SDK constants.
 *
 * @author Hao Song (songhao@vmware.com).
 */
public class Constants {

  /**
   * Name of the header for span operation name from server side.
   */
  public static final String WF_SPAN_HEADER = "X-WF-SPAN-NAME";

  /**
   * Property name of the active span
   */
  public static final String PROPERTY_NAME = WavefrontJaxrsClientFilter.class.getName() + ".activeSpan";

  /**
   * Property name of the child span
   */
  public static final String CHILD_OF = WavefrontJaxrsClientFilter.class.getName() + "." +
      References.CHILD_OF;

  /**
   * Component name of the JAX-RS client
   */
  public static final String JAXRS_CLIENT_COMPONENT = "jaxrs-client";

  /**
   * Component name of the JAX-RS server
   */
  public static final String JAXRS_SERVER_COMPONENT = "jaxrs-server";

  /**
   * Prefix for request
   */
  public static final String REQUEST_PREFIX = "request.";

  /**
   * Prefix for response
   */
  public static final String RESPONSE_PREFIX = "response.";
}
