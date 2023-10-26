package dev.restate.sdk.core.impl;

import static dev.restate.sdk.core.impl.ProtoUtils.*;
import static dev.restate.sdk.core.impl.TestDefinitions.testInvocation;

import com.google.protobuf.ByteString;
import dev.restate.generated.service.protocol.Protocol;
import dev.restate.sdk.core.impl.TestDefinitions.TestDefinition;
import dev.restate.sdk.core.impl.TestDefinitions.TestSuite;
import dev.restate.sdk.core.impl.testservices.GreeterGrpc;
import dev.restate.sdk.core.impl.testservices.GreetingRequest;
import io.grpc.BindableService;
import java.util.stream.Stream;

public abstract class InvocationIdTestSuite implements TestSuite {

  protected abstract BindableService returnInvocationId();

  @Override
  public Stream<TestDefinition> definitions() {
    String debugId = "my-debug-id";
    ByteString id = ByteString.copyFromUtf8(debugId);

    return Stream.of(
        testInvocation(this::returnInvocationId, GreeterGrpc.getGreetMethod())
            .withInput(
                Protocol.StartMessage.newBuilder().setDebugId(debugId).setId(id).setKnownEntries(1),
                inputMessage(GreetingRequest.getDefaultInstance()))
            .onlyUnbuffered()
            .expectingOutput(outputMessage(greetingResponse(debugId))));
  }
}