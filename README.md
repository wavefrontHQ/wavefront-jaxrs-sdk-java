# wavefront-jaxrs-sdk-java

This SDK provides out of the box tracing data from JAX-RS based client in your Java application. The data can be sent to Wavefront using either the [proxy](https://docs.wavefront.com/proxies.html) or [direct ingestion](https://docs.wavefront.com/direct_ingestion.html). You can analyze the tracing data in [Wavefront](https://www.wavefront.com/) to better understand how your application is performing in production. If you have multiple services running, try to instrument your server-side service with our [Wavefront Jersey SDK](https://github.com/wavefrontHQ/wavefront-jersey-sdk) to form a complete trace.

## Maven

If you are using Maven, add the following maven dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.wavefront</groupId>
    <artifactId>wavefront-jaxrs-sdk-java</artifactId>
    <version>0.9.0</version>
</dependency>
```

## WavefrontJaxrsClientFilter

Follow the steps below to quickly set up a `WavefrontJaxrsClientFilter`. For each client that uses a JAX-RS-compliant framework:

1. Create an `ApplicationTags` instance, which specifies metadata about your application.
2. Create a `WavefrontSender` for sending data to Wavefront.
3. Create a `WavefrontSpanReporter` for reporting tracing span data to Wavefront.
4. Create a `WavefrontTracer` as the tracer for generating spans.
5. Create a `WavefrontJaxrsClientFilter`.
6. Register the `WavefrontJaxrsClientFilter`.

For the details of each step, see the sections below.

### 1. Configure Applicatation Tags

Application tags determine the metadata (point tags and span tags) that are included with every metric/histogram/span reported to Wavefront. These tags enable you to filter and query the reported data in Wavefront.

You encapsulate application tags in an `ApplicationTags` object. See [Instantiating ApplicationTags](https://github.com/wavefrontHQ/wavefront-sdk-java/blob/master/docs/apptags.md) for details.

### 2. Set Up a WavefrontSender 

A `WavefrontSender` object implements the low-level interface for sending data to Wavefront. You can choose to send data to Wavefront using either the [Wavefront proxy](https://docs.wavefront.com/proxies.html) or [direct ingestion](https://docs.wavefront.com/direct_ingestion.html). The `WavefrontSender` is used by the `WavefrontTracer`.

- See [Set Up a WavefrontSender](https://github.com/wavefrontHQ/wavefront-sdk-java/blob/master/README.md#set-up-a-wavefrontsender) for details on instantiating a proxy or direct ingestion client.

**Note:** If you are using multiple Wavefront Java SDKs, see [Sharing a WavefrontSender](https://github.com/wavefrontHQ/wavefront-sdk-java/blob/master/docs/sender.md) for information about sharing a single `WavefrontSender` instance across SDKs.

### 3. Set Up WavefrontSpanReporter

Use the `WavefrontSender` created as described above to initiaite `WavefrontSpanReporter`:

```java
Reporter wfSpanReporter = new WavefrontSpanReporter.Builder().
    withSource("{SOURCE}").build(wavefrontSender);
```

### 4. Set Up WavefrontTracer

Use the `ApplicationTags` and `WavefrontSpanReporter` created as described above to initiaite `WavefrontTracer`:

```Java
WavefrontTracer wfTracer = new WavefrontTracer.Builder(wfSpanReporter,   
    applicationTags).build();
```

### 5. Set Up WavefrontJaxrsClientFilter

Use the `WavefrontSender`, `ApplicationTags` and `WavefrontTracer` created as described above to initiaite `WavefrontJaxrsClientFilter`:

```Java
WavefrontJaxrsClientFilter filter = new WavefrontJaxrsClientFilter(wfSender,  
    applicationTags, source, wfTracer);
```

### 6. Register WavefrontJaxrsClientFilter

For any JAX-RS-complient `ClientBuilder`, register your filter as follows:

```Java
clientBuilder.register(filter);
```

