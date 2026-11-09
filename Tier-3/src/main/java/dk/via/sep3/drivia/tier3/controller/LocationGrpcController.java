package dk.via.sep3.drivia.tier3.controller;

import dk.via.sep3.drivia.grpc.AddLocationRequest;
import dk.via.sep3.drivia.grpc.DeleteLocationResponse;
import dk.via.sep3.drivia.grpc.GetAllLocationsRequest;
import dk.via.sep3.drivia.grpc.Location;
import dk.via.sep3.drivia.grpc.LocationIdRequest;
import dk.via.sep3.drivia.grpc.LocationList;
import dk.via.sep3.drivia.grpc.LocationServiceGrpc;
import dk.via.sep3.drivia.tier3.dto.LocationDTO;
import dk.via.sep3.drivia.tier3.service.LocationService;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import static dk.via.sep3.drivia.tier3.common.GrpcErrors.toGrpcError;

@GrpcService
public class LocationGrpcController extends LocationServiceGrpc.LocationServiceImplBase {

    private final LocationService locationService;

    public LocationGrpcController(LocationService locationService){
        this.locationService = locationService;
    }

    @Override
    public void getAllLocations( GetAllLocationsRequest request, StreamObserver<LocationList> responseObserver) {
        try {
            LocationList.Builder list = LocationList.newBuilder();
            for (LocationDTO dto : locationService.getAllLocations()) {
                list.addLocations(toProto(dto));
            }
            responseObserver.onNext(list.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void getLocation( LocationIdRequest request, StreamObserver<Location> responseObserver) {
        try {
            LocationDTO dto = locationService.getLocation(request.getId());
            responseObserver.onNext(toProto(dto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void addLocation(AddLocationRequest request, StreamObserver<Location> responseObserver) {
        try {
            LocationDTO dto = locationService.addLocation(
                    new LocationDTO(null, request.getName(), request.getAddress()));
            responseObserver.onNext(toProto(dto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void updateLocation(Location request, StreamObserver<Location> responseObserver) {
        try {
            LocationDTO dto = locationService.updateLocation(
                    new LocationDTO(request.getId(), request.getName(), request.getAddress()));
            responseObserver.onNext(toProto(dto));
            responseObserver.onCompleted();
        } catch (Exception e){
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void deleteLocation( LocationIdRequest request, StreamObserver<DeleteLocationResponse> responseObserver) {
        try {
            locationService.deleteLocation(request.getId());
            responseObserver.onNext(DeleteLocationResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    private Location toProto(LocationDTO dto){
        return Location.newBuilder()
                .setId(dto.id())
                .setName(dto.name())
                .setAddress(dto.address())
                .build();
    }
}

