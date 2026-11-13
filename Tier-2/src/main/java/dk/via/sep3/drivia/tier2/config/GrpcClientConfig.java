package dk.via.sep3.drivia.tier2.config;

import dk.via.sep3.drivia.grpc.LocationServiceGrpc;
import dk.via.sep3.drivia.grpc.VehicleServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig{

    @Value("${grpc.client.host}")
    private String host;

    @Value("${grpc.client.port}")
    private int port;

    // One connection to Tier 3, closed when the application stops
    @Bean( destroyMethod = "shutdown" )
    public ManagedChannel tier3Channel(){
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public VehicleServiceGrpc.VehicleServiceBlockingStub vehicleStub( ManagedChannel tier3Channel ){
        return VehicleServiceGrpc.newBlockingStub(tier3Channel);
    }

    @Bean
    public LocationServiceGrpc.LocationServiceBlockingStub locationStub(
            ManagedChannel tier3Channel) {
        return LocationServiceGrpc.newBlockingStub(tier3Channel);
    }
}
