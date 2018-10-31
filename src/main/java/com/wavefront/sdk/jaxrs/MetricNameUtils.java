package com.wavefront.sdk.jaxrs;

import com.wavefront.sdk.common.Pair;

import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A utils class to generate metric name for JAX-RS based application requests/responses.
 *
 * @author Hao Song (songhao@vmware.com).
 */
abstract class MetricNameUtils {

  private static final String REQUEST_PREFIX = "request.";
  private static final String RESPONSE_PREFIX = "response.";

  /**
   * Util to generate metric name from the JAX-RS container request.
   *
   * @param request JAX-RS container request.
   * @return generated metric name from the JAX-RS container request.
   */
  static Optional<Pair<String, String>> metricNameAndPath(ContainerRequestContext request) {
    return metricNameAndPath(request, REQUEST_PREFIX);
  }

  /**
   * Util to generate metric name from the JAX-RS container response.
   *
   * @param request  JAX-RS container request.
   * @param response JAX-RS container response.
   * @return generated metric name from the JAX-RS container request/response.
   */
  static Optional<String> metricName(ContainerRequestContext request,
                                     ContainerResponseContext response) {
    Optional<Pair<String, String>> optionalMetricName = metricNameAndPath(request, RESPONSE_PREFIX);
    return optionalMetricName.map(metricName -> metricName._1 + "." + response.getStatus());
  }

  private static Optional<Pair<String, String>> metricNameAndPath(ContainerRequestContext request,
                                                                  String prefix) {
    MultivaluedMap<String, String> pathParameters = request.getUriInfo().getPathParameters();
    String path = request.getUriInfo().getPath();
    if (path.isEmpty() || path.charAt(0) != '/') {
      path = "/" + path;
    }
    for (Map.Entry<String, List<String>> entry : pathParameters.entrySet()) {
      final String originalPathFragment = String.format("{%s}", entry.getKey());
      for (String currentPathFragment : entry.getValue()) {
        path = path.replace(currentPathFragment, originalPathFragment);
      }
    }
    Optional<String> optionalMetricName = metricName(request.getMethod(), path);
    String matchingPath = stripLeadingAndTrailingSlashes(path);
    return optionalMetricName.map(metricName -> new Pair<>(prefix + metricName, matchingPath));
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
}
