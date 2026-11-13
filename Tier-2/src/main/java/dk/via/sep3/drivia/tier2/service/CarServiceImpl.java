package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.grpc.*;
import dk.via.sep3.drivia.tier2.dto.CarResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CarServiceImpl implements CarService{

    private final VehicleServiceGrpc.VehicleServiceBlockingStub vehicleStub;

    public CarServiceImpl( VehicleServiceGrpc.VehicleServiceBlockingStub vehicleStub ){
        this.vehicleStub = vehicleStub;
    }

    @Override
    public List<CarResponse> getAllCars(){
        return vehicleStub.getAllVehicles(GetAllVehiclesRequest.newBuilder().build())
                .getVehiclesList()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CarResponse getCarById(int id) {
        Vehicle vehicle = vehicleStub.getVehicle(
                VehicleIdRequest.newBuilder()
                        .setId(id)
                        .build()
        );
        return toResponse(vehicle);
    }

    @Override
    public List<CarResponse> searchCars(
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
                ).getVehiclesList()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CarResponse> getAvailableCars(
            int locationId,
            LocalDate pickupDate,
            LocalDate returnDate) {

        return vehicleStub.getAvailableVehicles(
                        AvailabilityRequest.newBuilder()
                                .setLocationId(locationId)
                                .setPickupDate(pickupDate.toString())
                                .setReturnDate(returnDate.toString())
                                .build()
                ).getVehiclesList()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CarResponse toResponse( Vehicle vehicle ){
        return new CarResponse( vehicle.getId(), vehicle.getRegistrationNo(), vehicle.getModel(),
                LocalDate.parse(vehicle.getProductionDate()), vehicle.getHorsepower(), vehicle.getCategory(),
                vehicle.getGearbox(), vehicle.getPricePerDay(), vehicle.getStatus(),
                vehicle.getLocationId(), vehicle.getLocationName());
    }
}
