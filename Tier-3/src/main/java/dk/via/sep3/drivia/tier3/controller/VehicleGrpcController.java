package dk.via.sep3.drivia.tier3.controller;

import dk.via.sep3.drivia.grpc.AddVehicleRequest;
import dk.via.sep3.drivia.grpc.AvailabilityRequest;
import dk.via.sep3.drivia.grpc.GetAllVehiclesRequest;
import dk.via.sep3.drivia.grpc.RemoveVehicleResponse;
import dk.via.sep3.drivia.grpc.SearchVehiclesRequest;
import dk.via.sep3.drivia.grpc.UpdateVehicleStatusRequest;
import dk.via.sep3.drivia.grpc.Vehicle;
import dk.via.sep3.drivia.grpc.VehicleIdRequest;
import dk.via.sep3.drivia.grpc.VehicleList;
import dk.via.sep3.drivia.grpc.VehicleServiceGrpc;
import dk.via.sep3.drivia.tier3.dto.VehicleDTO;
import dk.via.sep3.drivia.tier3.entity.VehicleStatus;
import dk.via.sep3.drivia.tier3.service.VehicleService;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import java.time.LocalDate;
import java.util.List;

import static dk.via.sep3.drivia.tier3.common.GrpcErrors.toGrpcError;

@GrpcService
public class VehicleGrpcController extends VehicleServiceGrpc.VehicleServiceImplBase{

    private final VehicleService vehicleService;

    public VehicleGrpcController( VehicleService vehicleService ){
        this.vehicleService = vehicleService;
    }

    @Override
    public void getAllVehicles(GetAllVehiclesRequest request, StreamObserver<VehicleList> responseObserver) {
        try {
            responseObserver.onNext(toProtoList(vehicleService.getAllVehicles()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void getVehicle(VehicleIdRequest request, StreamObserver<Vehicle> responseObserver) {
        try {
            responseObserver.onNext(toProto(vehicleService.getVehicle(request.getId())));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void searchVehicles( SearchVehiclesRequest request, StreamObserver<VehicleList> responseObserver) {
        try {
            List<VehicleDTO> vehicles = vehicleService.searchVehicles(
                    emptyToNull(request.getModel()),
                    emptyToNull(request.getCategory()),
                    emptyToNull(request.getGearbox()),
                    request.getLocationId());
            responseObserver.onNext(toProtoList(vehicles));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void getAvailableVehicles( AvailabilityRequest request, StreamObserver<VehicleList> responseObserver) {
        try {
            List<VehicleDTO> vehicles = vehicleService.getAvailableVehicles(
                    request.getLocationId(),
                    LocalDate.parse(request.getPickupDate()),
                    LocalDate.parse(request.getReturnDate()));
            responseObserver.onNext(toProtoList(vehicles));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void addVehicle( AddVehicleRequest request, StreamObserver<Vehicle> responseObserver) {
        try {
            VehicleDTO dto = new VehicleDTO(0, request.getRegistrationNo(), request.getModel(),
                    LocalDate.parse(request.getProductionDate()), request.getHorsepower(),
                    request.getCategory(), request.getGearbox(), request.getPricePerDay(),
                    null, request.getLocationId(), null);
            responseObserver.onNext(toProto(vehicleService.addVehicle(dto)));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    // Status and location name are ignored here, use updateVehicleStatus for the status
    @Override
    public void updateVehicle( Vehicle request, StreamObserver<Vehicle> responseObserver) {
        try {
            VehicleDTO dto = new VehicleDTO(request.getId(), request.getRegistrationNo(), request.getModel(),
                    LocalDate.parse(request.getProductionDate()), request.getHorsepower(),
                    request.getCategory(), request.getGearbox(), request.getPricePerDay(),
                    null, request.getLocationId(), null);
            responseObserver.onNext(toProto(vehicleService.updateVehicle(dto)));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void updateVehicleStatus( UpdateVehicleStatusRequest request, StreamObserver<Vehicle> responseObserver) {
        try {
            VehicleStatus status = VehicleStatus.valueOf(request.getStatus());
            responseObserver.onNext(toProto(vehicleService.updateVehicleStatus(request.getId(), status)));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void removeVehicle( VehicleIdRequest request, StreamObserver<RemoveVehicleResponse> responseObserver){
        try {
            vehicleService.removeVehicle(request.getId());
            responseObserver.onNext(RemoveVehicleResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    private Vehicle toProto( VehicleDTO dto){
        return Vehicle.newBuilder()
                .setId(dto.id())
                .setRegistrationNo(dto.registrationNo())
                .setModel(dto.model())
                .setProductionDate(dto.productionDate().toString())
                .setHorsepower(dto.horsepower())
                .setCategory(dto.category())
                .setGearbox(dto.gearbox())
                .setPricePerDay(dto.pricePerDay())
                .setStatus(dto.status().name())
                .setLocationId(dto.locationId())
                .setLocationName(dto.locationName())
                .build();
    }

    private VehicleList toProtoList(List<VehicleDTO> dtos){
        VehicleList.Builder list = VehicleList.newBuilder();
        for (VehicleDTO dto : dtos) {
            list.addVehicles(toProto(dto));
        }
        return list.build();
    }

    private String emptyToNull(String value){
        return value.isBlank() ? null : value;
    }
}
