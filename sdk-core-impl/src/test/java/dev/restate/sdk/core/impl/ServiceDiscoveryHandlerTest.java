package dev.restate.sdk.core.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.DescriptorProtos;
import dev.restate.generated.service.discovery.Discovery;
import dev.restate.sdk.core.impl.testservices.CounterGrpc;
import dev.restate.sdk.core.impl.testservices.GreeterGrpc;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ServiceDiscoveryHandlerTest {

  @Test
  void handleWithMultipleServices() {
    ServiceDiscoveryHandler handler =
        new ServiceDiscoveryHandler(
            Map.of(
                GreeterGrpc.SERVICE_NAME, new GreeterGrpc.GreeterImplBase() {}.bindService(),
                CounterGrpc.SERVICE_NAME, new CounterGrpc.CounterImplBase() {}.bindService()));

    Discovery.ServiceDiscoveryResponse response =
        handler.handle(Discovery.ServiceDiscoveryRequest.getDefaultInstance());

    assertThat(response.getServicesList())
        .containsExactlyInAnyOrder(GreeterGrpc.SERVICE_NAME, CounterGrpc.SERVICE_NAME);
    assertThat(response.getFilesList())
        .map(DescriptorProtos.FileDescriptorProto::getName)
        .containsExactlyInAnyOrder(
            "dev/restate/ext.proto",
            "google/protobuf/descriptor.proto",
            "google/protobuf/empty.proto",
            "counter.proto",
            "common.proto",
            "greeter.proto");
  }
}