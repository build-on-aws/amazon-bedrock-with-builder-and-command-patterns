package com.amazon.aws.developers.bedrock.examples;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import com.amazon.aws.developers.bedrock.util.BedrockRequestBody;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;

public class ClaudeChatWithStreaming {

    private static final String MODEL_ID = "anthropic.claude-v2";

    private static final String PROMPT = """
        <paragraph>
            "In 1758, the Swedish botanist and zoologist Carl Linnaeus published in his Systema Naturae,
            the two-word naming of species (binomial nomenclature). Canis is the Latin word meaning "dog",
            and under this genus, he listed the domestic dog, the wolf, and the golden jackal."
        </paragraph>

        Please rewrite the above paragraph to make it understandable to a 5th grader.
        Please output your rewrite in <rewrite></rewrite> tags.
    """;

    public static void main(String... args) throws Exception {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        try (BedrockRuntimeAsyncClient bedrockClient = BedrockRuntimeAsyncClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build()) {

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

                executor.submit(() -> {

                    String bedrockBody = BedrockRequestBody.builder()
                            .withModelId(MODEL_ID)
                            .withPrompt(PROMPT)
                            .withInferenceParameter("max_tokens_to_sample", 2048)
                            .withInferenceParameter("temperature", 0.5)
                            .withInferenceParameter("top_k", 250)
                            .withInferenceParameter("top_p", 1)
                            .build();

                    InvokeModelWithResponseStreamRequest invokeModelRequest = InvokeModelWithResponseStreamRequest
                            .builder()
                            .modelId(MODEL_ID)
                            .body(SdkBytes.fromString(bedrockBody, Charset.defaultCharset()))
                            .build();

                    InvokeModelWithResponseStreamResponseHandler responseHandler = InvokeModelWithResponseStreamResponseHandler
                            .builder()
                            .onEventStream(
                                    eventStream -> eventStream.subscribe(System.out::println)
                            )
                            .onComplete(() -> {
                                countDownLatch.countDown();
                            })
                            .build();

                    bedrockClient.invokeModelWithResponseStream(invokeModelRequest, responseHandler);

                });

                countDownLatch.await();

            }

        }

    }

}