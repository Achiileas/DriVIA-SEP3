package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.grpc.GetAllVehiclesRequest;
import dk.via.sep3.drivia.grpc.Vehicle;
import dk.via.sep3.drivia.grpc.VehicleServiceGrpc;
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

    private CarResponse toResponse( Vehicle vehicle ){
        return new CarResponse( vehicle.getId(), vehicle.getRegistrationNo(), vehicle.getModel(),
                LocalDate.parse(vehicle.getProductionDate()), vehicle.getHorsepower(), vehicle.getCategory(),
                vehicle.getGearbox(), vehicle.getPricePerDay(), vehicle.getStatus(),
                vehicle.getLocationId(), vehicle.getLocationName());
    }
}
