# Amazon Bedrock with Builder and Command Patterns

[Amazon Bedrock](https://aws.amazon.com/bedrock) is a fully managed service that offers a choice of high-performing foundation models (FMs) from leading AI companies like AI21 Labs, Anthropic, Cohere, Stability AI, and Amazon with a single API. From the Ops perspective, having a single unified API provides development and deployment velocity, as aspects like security, scalability, and monitoring are standardized.

However, when it comes to write code to interact with the FMs, developers must handle manually the API peculiarities of each FM. This happens because the schema of each FM is unique, and to perform an API request, the correct body payload must be provided.

➡️ For example, consider the following prompt: `When Christmas is celebrated?`

Asking this question to Amazon Bedrock using the `AI21Labs Jurassic-2 Mid` FM requires this API request:

```json
{
   "modelId": "ai21.j2-mid-v1",
   "contentType": "application/json",
   "accept": "*/*",
   "body": "{\"prompt\":\"When Christmas is celebrated?\",\"maxTokens\":200,\"temperature\":0.7,\"topP\":1,\"stopSequences\":[],\"countPenalty\":{\"scale\":0},\"presencePenalty\":{\"scale\":0},\"frequencyPenalty\":{\"scale\":0}}"
}
```

Asking the exact same question to Amazon Bedrock using the `Anthropic Claude V2` FM requires a different API request:
```json
{
   "modelId": "anthropic.claude-v2",
   "contentType": "application/json",
   "accept": "*/*",
   "body": "{\"prompt\":\"Human: \\n\\nHuman: When Christmas is celebrated?\\n\\nAssistant:\",\"max_tokens_to_sample\":300,\"temperature\":1,\"top_k\":250,\"top_p\":0.999,\"stop_sequences\":[\"\\n\\nHuman:\"],\"anthropic_version\":\"bedrock-2023-05-31\"}"
}
```

To reduce the cacophony, this project introduces a simple yet powerful implementation to abstract away each the schemas from each FM. This is possible thanks to the usage of the design pattern Builder that separates the construction of a complex object from its representation. In this case, the complex object is the request body that must be sent to Amazon Bedrock. To reduce the cacophony, this project introduces a simple yet powerful implementation to abstract away each the schemas from each FM. This is possible thanks to the usage of the [Builder design pattern](https://en.wikipedia.org/wiki/Builder_pattern) that separates the construction of a complex object from its representation. In this case, the complex object is the request body that must be sent to Amazon Bedrock. To allow the usage of different FMs, the [Command design pattern](https://en.wikipedia.org/wiki/Command_pattern) was also used. Each FM has their implementation of the command. This way, each FM can handle their unique parameters their own way.

## 🧑🏻‍💻 Using the BedrockRequetBody implementation

To use the BedrockRequestBody implementation, just invoke the `builder()` static method from the class and start providing the required parameters, which are the `modeId` and the `prompt` respectively. Considering the prompt `When Christmas is celebrated?` this is how you would work with `AI21Labs Jurassic-2 Mid`:

```java
String bedrockBody = BedrockRequestBody.builder()
   .withModelId("ai21.j2-mid-v1")
   .withPrompt("When Christmas is celebrated?")
   .build();
```

Similarly, if you want to use the `Anthropic Claude V2` FM, this is how you would work:
```java
String bedrockBody = BedrockRequestBody.builder()
   .withModelId("anthropic.claude-v2")
   .withPrompt("When Christmas is celebrated?")
   .build();
```

Much simpler, right? 😎

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the MIT-0 License. See the [LICENSE](./LICENSE) file.
