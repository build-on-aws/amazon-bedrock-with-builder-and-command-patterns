package com.amazon.aws.developers.bedrock.examples;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import com.amazon.aws.developers.bedrock.util.BedrockRequestBody;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;
import software.amazon.awssdk.services.bedrockruntime.model.PayloadPart;

public class ClaudeChatWithStreaming {

    private static final String MODEL_ID = "anthropic.claude-v2";

    private static final String PROMPT = """
        Generative AI refers to artificial intelligence systems that are capable of generating
        novel content such as text, images, audio, video, and more, as opposed to just analyzing
        existing data. The key aspect of generative AI is that it creates brand new outputs that
        are unique and original, not just reproductions or remixes of existing content. Generative
        AI leverages machine learning techniques like neural networks that are trained on large
        datasets to build an understanding of patterns and structures within the data. It then
        uses that knowledge to generate new artifacts that conform to those patterns but are not
        identical copies, allowing for creativity and imagination. Prominent examples of generative
        AI include systems like DALL-E that creates images from text descriptions, GPT-3 that
        generates human-like text, and WaveNet that produces realistic synthetic voices. Generative
        models hold great promise for assisting and augmenting human creativity across many domains
        but also raise concerns about potential misuse if not thoughtfully implemented. Overall,
        generative AI aims to mimic human creative abilities at scale to autonomously produce
        high-quality, diverse, and novel content.        

        Please create a single short paragraph so that a fiver years old child can understand.
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

                    bedrockClient.invokeModelWithResponseStream(invokeModelRequest,
                            InvokeModelWithResponseStreamResponseHandler.builder()
                                    .onResponse(response -> {
                                        System.out.println("🤖 Response: ");
                                    })
                                    .subscriber(eventConsumer -> {
                                        eventConsumer.accept(new InvokeModelWithResponseStreamResponseHandler.Visitor() {
                                            public void visitChunk(PayloadPart payloadPart) {
                                                String payloadAsString = payloadPart.bytes().asUtf8String();
                                                JSONObject payloadAsJson = new JSONObject(payloadAsString);
                                                System.out.print(payloadAsJson.getString("completion"));
                                            }
                                        });
                                    })
                                    .onComplete(() -> {
                                        System.out.println();
                                        countDownLatch.countDown();
                                    })
                                    .build());

                });

                countDownLatch.await();

            }

        }

    }

}