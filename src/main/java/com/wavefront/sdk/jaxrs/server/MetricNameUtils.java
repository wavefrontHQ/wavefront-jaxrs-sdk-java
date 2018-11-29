package com.wavefront.sdk.jaxrs.server;

import com.wavefront.sdk.common.Pair;

import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * A utils class to generate metric name for JAX-RS based application requests/responses.
 *
 * @author Hao Song (songhao@vmware.com).
 */
abstract class MetricNameUtils {

  /**
   * Util to generate metric name from the JAX-RS container request.
   *
   * @param resourceInfo JAX-RS container request.
   * @return generated metric name from the JAX-RS container request.
   */
  static Optional<Pair<String, String>> metricNameAndPath(ContainerRequestContext request,
                                                          ResourceInfo resourceInfo) {
    return metricNameAndPath(resourceInfo, request);
  }

  /**
   * Util to generate metric name from the JAX-RS container response.
   *
   * @param request  JAX-RS container request.
   * @param response JAX-RS container response.
   * @return generated metric name from the JAX-RS container request/response.
   */
  static Optional<String> metricName(ContainerRequestContext request,
                                     ContainerResponseContext response,
                                     ResourceInfo resourceInfo) {
    Optional<Pair<String, String>> optionalMetricName = metricNameAndPath(resourceInfo, request);
    return optionalMetricName.map(metricName -> metricName._1 + "." + response.getStatus());
  }

  private static Optional<Pair<String, String>> metricNameAndPath(ResourceInfo resourceInfo,
                                                                  ContainerRequestContext request) {
    Class<?> clazz = resourceInfo.getResourceClass();
    Method method = resourceInfo.getResourceMethod();
    String path = extractPath(clazz.getAnnotation(Path.class)) + extractPath(method.
        getAnnotation(Path.class));
    Optional<String> optionalMetricName = metricName(request.getMethod(), path);
    String matchingPath = stripLeadingAndTrailingSlashes(path);
    return optionalMetricName.map(metricName -> new Pair<>(metricName, matchingPath));
  }

  /**
   * Accepts a resource method and extracts the path and turns slashes into dots to be more metric
   * friendly. Might return empty metric name if all the original characters in the string are not
   * metric friendly.
   *
   * @param httpMethod JAX-RS API HTTP request method.
   * @param path       JAX-RS API request relative path.
   * @return generated metric name from the original request.
   */
  private static Optional<String> metricName(String httpMethod, String path) {
    String metricId = stripLeadingAndTrailingSlashes(path);
    // prevents metrics from trying to create object names with weird characters
    // swagger-ui introduces a route: api-docs/{route: .+} and the colon must be removed
    metricId = metricId.replace('/', '.').replace(":", "").
        replace("{", "_").replace("}", "_");
    if (StringUtils.isBlank(metricId)) {
      return Optional.empty();
    }
    return Optional.of(metricId + "." + httpMethod);
  }

  private static String stripLeadingAndTrailingSlashes(String path) {
    return path == null ? "" : StringUtils.strip(path, "/");
  }

  private static String extractPath(Path path) {
    return path == null ? "" : path.value();
  }
}
