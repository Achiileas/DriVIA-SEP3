package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.grpc.*;
import dk.via.sep3.drivia.tier2.dto.response.VehicleResponse;
import dk.via.sep3.drivia.tier2.dto.request.VehicleCreateRequest;
import dk.via.sep3.drivia.tier2.dto.request.VehicleUpdateRequest;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleServiceGrpc.VehicleServiceBlockingStub vehicleStub;

    public VehicleServiceImpl(VehicleServiceGrpc.VehicleServiceBlockingStub vehicleStub) {
        this.vehicleStub = vehicleStub;
    }

    @Override
    public List<VehicleResponse> getAllVehicles() {
        return vehicleStub.getAllVehicles(GetAllVehiclesRequest.newBuilder().build())
            .getVehiclesList()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public VehicleResponse getVehicleById(int id) {
        Vehicle vehicle = vehicleStub.getVehicle(
            VehicleIdRequest.newBuilder()
                .setId(id)
                .build()
        );

        return toResponse(vehicle);
    }

    @Override
    public List<VehicleResponse> searchVehicles(
        String model,
        String category,
        String gearbox,
        int locationId) {

        return vehicleStub.searchVehicles(
                SearchVehiclesRequest.newBuilder()
                    .setModel(model == null ? "" : model)
                    .setCategory(category == null ? "" : category)
                    .setGearbox(gearbox == null ? "" : gearbox)
                    .setLocationId(locationId)
                    .build()
            )
            .getVehiclesList()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public List<VehicleResponse> getAvailableVehicles(
        int locationId,
        LocalDateTime pickupDate,
        LocalDateTime returnDate) {

        return vehicleStub.getAvailableVehicles(
                AvailabilityRequest.newBuilder()
                    .setLocationId(locationId)
                    .setPickupDate(pickupDate.toString())
                    .setReturnDate(returnDate.toString())
                    .build()
            )
            .getVehiclesList()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
            vehicle.getId(),
            vehicle.getRegistrationNo(),
            vehicle.getModel(),
            LocalDate.parse(vehicle.getProductionDate()),
            vehicle.getHorsepower(),
            vehicle.getCategory(),
            vehicle.getGearbox(),
            new BigDecimal(vehicle.getPricePerDay()),
            vehicle.getStatus(),
            vehicle.getLocationId(),
            vehicle.getLocationName()
        );
    }

    @Override
    public VehicleResponse addVehicle(VehicleCreateRequest request) {

        Vehicle vehicle = vehicleStub.addVehicle(
            AddVehicleRequest.newBuilder()
                .setRegistrationNo(request.registrationNo())
                .setModel(request.model())
                .setProductionDate(request.productionDate().toString())
                .setHorsepower(request.horsepower())
                .setCategory(request.category())
                .setGearbox(request.gearbox())
                .setPricePerDay(request.pricePerDay().toPlainString())
                .setLocationId(request.locationId())
                .build()
        );

        return toResponse(vehicle);
    }

    @Override
    public VehicleResponse updateVehicle(int id, VehicleUpdateRequest request) {

        Vehicle vehicle = vehicleStub.updateVehicle(
            Vehicle.newBuilder()
                .setId(id)
                .setRegistrationNo(request.registrationNo())
                .setModel(request.model())
                .setProductionDate(request.productionDate().toString())
                .setHorsepower(request.horsepower())
                .setCategory(request.category())
                .setGearbox(request.gearbox())
                .setPricePerDay(request.pricePerDay().toPlainString())
                .setLocationId(request.locationId())
                .build()
        );

        return toResponse(vehicle);
    }

    @Override
    public VehicleResponse updateVehicleStatus(int id, String status) {

        Vehicle vehicle = vehicleStub.updateVehicleStatus(
            UpdateVehicleStatusRequest.newBuilder()
                .setId(id)
                .setStatus(status)
                .build()
        );

        return toResponse(vehicle);
    }

    @Override
    public void removeVehicle(int id) {

        vehicleStub.removeVehicle(
            VehicleIdRequest.newBuilder()
                .setId(id)
                .build()
        );
    }
}