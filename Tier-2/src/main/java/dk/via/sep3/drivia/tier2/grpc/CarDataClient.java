package dk.via.sep3.drivia.tier2.grpc;

import dk.via.sep3.drivia.grpc.GetAllVehiclesRequest;
import dk.via.sep3.drivia.grpc.VehicleList;
import dk.via.sep3.drivia.grpc.VehicleServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CarDataClient {

  private final ManagedChannel channel;
  private final VehicleServiceGrpc.VehicleServiceBlockingStub stub;

  public CarDataClient(
      @Value("${grpc.client.host:localhost}") String host,
      @Value("${grpc.client.port:9090}") int port) {

    // Create a connection to Tier 3
    channel = ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext()
        .build();

    // Create the gRPC client stub
    stub = VehicleServiceGrpc.newBlockingStub(channel);
  }

  public VehicleList getAllVehicles() {

    // Create an empty request
    GetAllVehiclesRequest request = GetAllVehiclesRequest.newBuilder().build();

    // Call the GetAllCars method on Tier 3
    return stub.getAllVehicles(request);
  }

  @PreDestroy
  public void shutdown() {
    channel.shutdown();
  }
}
