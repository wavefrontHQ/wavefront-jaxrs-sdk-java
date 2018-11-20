# wavefront-jaxrs-sdk-java

The Wavefront by VMware JAX-RS SDK for Java provides out-of-the-box trace data from JAX-RS based clients in your Java application. You can analyze the tracing data in [Wavefront](https://www.wavefront.com/) to better understand how your application is performing in production. If you have multiple services running, you can instrument your server-side service with our [Wavefront Jersey SDK](https://github.com/wavefrontHQ/wavefront-jersey-sdk-java) to form a complete trace.

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

Follow the steps below to set up a `WavefrontJaxrsClientFilter`. For each client that uses a JAX-RS-compliant framework:

1. Create an `ApplicationTags` instance, which specifies metadata about your application.
2. Create a `WavefrontSender` for sending data to Wavefront.
3. Create a `WavefrontSpanReporter` for reporting trace data to Wavefront.
4. Create a `WavefrontTracer` as the tracer for generating traces and spans.
5. Create a `WavefrontJaxrsClientFilter`.
6. Register the `WavefrontJaxrsClientFilter`.

For the details of each step, see the sections below.

### 1. Set Up Application Tags

Application tags determine the metadata (point tags and span tags) that are included with every metric/histogram/span reported to Wavefront. These tags enable you to filter and query the reported data in Wavefront.

You encapsulate application tags in an `ApplicationTags` object. See [Instantiating ApplicationTags](https://github.com/wavefrontHQ/wavefront-sdk-java/blob/master/docs/apptags.md) for details.

### 2. Set Up a WavefrontSender 

A `WavefrontSender` object implements the low-level interface for sending data to Wavefront. You can choose to send data to Wavefront using either the [Wavefront proxy](https://docs.wavefront.com/proxies.html) or [direct ingestion](https://docs.wavefront.com/direct_ingestion.html).

* If you have already set up a `WavefrontSender` for another SDK that will run in the same JVM, use that one.  (For details about sharing a `WavefrontSender` instance, see [Share a WavefrontSender](https://github.com/wavefrontHQ/wavefront-sdk-java/blob/master/docs/sender.md#share-a-wavefrontsender).)

* Otherwise, follow the steps in [Set Up a WavefrontSender](https://github.com/wavefrontHQ/wavefront-sdk-java/blob/master/docs/sender.md#set-up-a-wavefrontsender).


### 3. Set Up a WavefrontSpanReporter

A `WavefrontSpanReporter` reports trace data to Wavefront. 
To build a `WavefrontSpanReporter`, you specify:
* The `WavefrontSender` you created above.
* The source of the reported trace data, typically the name of the host that the code is running on. 

```java
sourceName = "mySource"; // Example - Replace value!
Reporter wfSpanReporter = new WavefrontSpanReporter.Builder().
    withSource(sourceName).build(wavefrontSender);
```

### 4. Set Up a WavefrontTracer

A `WavefrontTracer` creates trace data from your JAX-RS client, and sends the data to Wavefront through a `WavefrontSpanReporter`.

To build a `WavefrontTracer`, you specify the `WavefrontSpanReporter` and the `ApplicationTags` you created above.

```Java
WavefrontTracer wfTracer = new WavefrontTracer.Builder(wfSpanReporter,   
    applicationTags).build();
```

### 5. Set Up a WavefrontJaxrsClientFilter

To build the `WavefrontJaxrsClientFilter`, you specify: 
* The `WavefrontSender`, `ApplicationTags` and `WavefrontTracer` you created above.
* The same source that you specified when creating the `WavefrontSpanReporter`.

```Java
sourceName = "mySource"; // Example - Replace value!
WavefrontJaxrsClientFilter filter = new WavefrontJaxrsClientFilter(wfSender,  
    applicationTags, sourceName, wfTracer);
```

### 6. Register the WavefrontJaxrsClientFilter

For any JAX-RS-compliant `ClientBuilder`, register your filter as follows:

```Java
clientBuilder.register(filter);
```

