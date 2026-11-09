package dk.via.sep3.drivia.tier3.reservation.controller;

import dk.via.sep3.drivia.grpc.CreateReservationRequest;
import dk.via.sep3.drivia.grpc.CustomerIdRequest;
import dk.via.sep3.drivia.grpc.GetAllReservationsRequest;
import dk.via.sep3.drivia.grpc.ModifyReservationRequest;
import dk.via.sep3.drivia.grpc.Reservation;
import dk.via.sep3.drivia.grpc.ReservationIdRequest;
import dk.via.sep3.drivia.grpc.ReservationList;
import dk.via.sep3.drivia.grpc.ReservationServiceGrpc;
import dk.via.sep3.drivia.tier3.reservation.dto.ReservationDTO;
import dk.via.sep3.drivia.tier3.reservation.service.ReservationService;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.util.List;

import static dk.via.sep3.drivia.tier3.common.GrpcErrors.toGrpcError;

@GrpcService
public class ReservationGrpcController extends ReservationServiceGrpc.ReservationServiceImplBase {

    private final ReservationService reservationService;

    public ReservationGrpcController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Override
    public void createReservation(CreateReservationRequest request, StreamObserver<Reservation> responseObserver) {
        try {
            ReservationDTO dto = reservationService.createReservation(
                    request.getCustomerId(),
                    request.getVehicleId(),
                    LocalDate.parse(request.getPickupDate()),
                    LocalDate.parse(request.getReturnDate()));
            responseObserver.onNext(toProto(dto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void getReservation(ReservationIdRequest request, StreamObserver<Reservation> responseObserver) {
        try {
            responseObserver.onNext(toProto(reservationService.getReservation(request.getId())));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void getAllReservations(GetAllReservationsRequest request, StreamObserver<ReservationList> responseObserver) {
        try {
            responseObserver.onNext(toProtoList(reservationService.getAllReservations()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void getCustomerReservations(CustomerIdRequest request, StreamObserver<ReservationList> responseObserver) {
        try {
            responseObserver.onNext(toProtoList(reservationService.getCustomerReservations(request.getCustomerId())));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void modifyReservation(ModifyReservationRequest request, StreamObserver<Reservation> responseObserver) {
        try {
            ReservationDTO dto = reservationService.modifyReservation(
                    request.getId(),
                    request.getVehicleId(),
                    LocalDate.parse(request.getPickupDate()),
                    LocalDate.parse(request.getReturnDate()));
            responseObserver.onNext(toProto(dto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void cancelReservation(ReservationIdRequest request, StreamObserver<Reservation> responseObserver) {
        try {
            responseObserver.onNext(toProto(reservationService.cancelReservation(request.getId())));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void approveReservation(ReservationIdRequest request, StreamObserver<Reservation> responseObserver) {
        try {
            responseObserver.onNext(toProto(reservationService.approveReservation(request.getId())));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void pickupReservation(ReservationIdRequest request, StreamObserver<Reservation> responseObserver) {
        try {
            responseObserver.onNext(toProto(reservationService.pickupReservation(request.getId())));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void returnReservation(ReservationIdRequest request, StreamObserver<Reservation> responseObserver) {
        try {
            responseObserver.onNext(toProto(reservationService.returnReservation(request.getId())));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    private Reservation toProto(ReservationDTO dto) {
        return Reservation.newBuilder()
                .setId(dto.id())
                .setCustomerId(dto.customerId())
                .setVehicleId(dto.vehicleId())
                .setVehicleModel(dto.vehicleModel())
                .setLocationId(dto.locationId())
                .setLocationName(dto.locationName())
                .setPickupDate(dto.pickupDate().toString())
                .setReturnDate(dto.returnDate().toString())
                .setStatus(dto.status().name())
                .setTotalPrice(dto.totalPrice())
                .build();
    }

    private ReservationList toProtoList(List<ReservationDTO> dtos) {
        ReservationList.Builder list = ReservationList.newBuilder();
        for (ReservationDTO dto : dtos) {
            list.addReservations(toProto(dto));
        }
        return list.build();
    }
}

