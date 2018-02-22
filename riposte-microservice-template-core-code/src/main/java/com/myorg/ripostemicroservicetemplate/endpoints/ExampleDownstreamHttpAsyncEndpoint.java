package com.myorg.ripostemicroservicetemplate.endpoints;

import com.nike.riposte.client.asynchttp.ning.AsyncHttpClientHelper;
import com.nike.riposte.client.asynchttp.ning.RequestBuilderWrapper;
import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.ResponseInfo;
import com.nike.riposte.server.http.StandardEndpoint;
import com.nike.riposte.util.Matcher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import io.netty.channel.ChannelHandlerContext;

/**
 * Contains an example of how to do asynchronous downstream HTTP calls so that the thread count on the server stays
 * constant even when it's being hammered by hundreds or thousands of concurrent requests, each one needing to make
 * downstream calls. Since we want the examples to be fully self-contained we simply make a downstream call to our own
 * {@link ExampleEndpoint.Get} endpoint. To show how you can do additional
 * work on the response from the downstream call we insert a {@code "viaAsyncHttpClient": "true"} JSON field into
 * the downstream response before returning.
 *
 * <p>TODO: EXAMPLE CLEANUP - Delete this class.
 *
 * @author Nic Munroe
 */
@SuppressWarnings("WeakerAccess")
public class ExampleDownstreamHttpAsyncEndpoint extends StandardEndpoint<Void, Map<String, Object>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String MATCHING_ENDPOINT_PATH = "/exampleDownstreamHttpAsync";
    private static final Matcher MATCHER = Matcher.match(MATCHING_ENDPOINT_PATH);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AsyncHttpClientHelper asyncHttpClientHelper;
    private final String downstreamUrl;

    @Inject
    public ExampleDownstreamHttpAsyncEndpoint(AsyncHttpClientHelper asyncHttpClientHelper,
                                              @Named("endpoints.port") int httpPort,
                                              @Named("endpoints.sslPort") int httpsPort,
                                              @Named("endpoints.useSsl") boolean useSecure) {
        this.asyncHttpClientHelper = asyncHttpClientHelper;
        String localServerHostAndPort = (useSecure)
                                        ? "https://localhost:" + httpsPort
                                        : "http://localhost:" + httpPort;
        this.downstreamUrl = localServerHostAndPort + ExampleEndpoint.MATCHING_PATH;
    }

    @Override
    public CompletableFuture<ResponseInfo<Map<String, Object>>> execute(RequestInfo<Void> request,
                                                                        Executor longRunningTaskExecutor,
                                                                        ChannelHandlerContext ctx) {

        RequestBuilderWrapper reqWrapper = asyncHttpClientHelper.getRequestBuilder(downstreamUrl, request.getMethod());
        reqWrapper.requestBuilder
            .addQueryParam("some_query_param", "foo")
            .addHeader("Accept", "application/json");

        if (request.getRawContentLengthInBytes() > 0)
            reqWrapper.requestBuilder.setBody(request.getRawContent());

        logger.info("About to make async library call");
        ObjectHolder<Long> startTime = new ObjectHolder<>();
        startTime.heldObject = System.currentTimeMillis();

        return asyncHttpClientHelper.executeAsyncHttpRequest(reqWrapper, (asyncResponse) -> {
            logger.info("In async response handler. Total time spent millis: {}",
                        (System.currentTimeMillis() - startTime.heldObject));
            return ResponseInfo.newBuilder(modifyResponseBody(asyncResponse.getResponseBody())).build();
        }, ctx);
    }

    private Map<String, Object> modifyResponseBody(String rawDownstreamResponse) throws IOException {
        Map<String, Object> deserializedResponse = objectMapper.readValue(rawDownstreamResponse,
                                                                          new TypeReference<Map<String, Object>>() {});
        deserializedResponse.put("viaAsyncHttpClient", "true");
        return deserializedResponse;
    }

    @Override
    public Matcher requestMatcher() {
        return MATCHER;
    }

    private static class ObjectHolder<T> {

        public T heldObject;
    }

}
