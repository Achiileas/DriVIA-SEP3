package dk.via.sep3.drivia.tier3.controller;

import dk.via.sep3.drivia.grpc.LoginRequest;
import dk.via.sep3.drivia.grpc.RegisterRequest;
import dk.via.sep3.drivia.grpc.User;
import dk.via.sep3.drivia.grpc.UserIdRequest;
import dk.via.sep3.drivia.grpc.UserServiceGrpc;
import dk.via.sep3.drivia.tier3.dto.UserDTO;
import dk.via.sep3.drivia.tier3.service.UserService;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import static dk.via.sep3.drivia.tier3.common.GrpcErrors.toGrpcError;

@GrpcService
public class UserGrpcController extends UserServiceGrpc.UserServiceImplBase{

    private final UserService userService;

    public UserGrpcController( UserService userService ){
        this.userService = userService;
    }

    @Override
    public void register( RegisterRequest request, StreamObserver<User> responseObserver ){
        try {
            UserDTO dto = userService.registerCustomer(request.getName(), request.getEmail(),
                    request.getPassword(), request.getPhoneNumber());
            responseObserver.onNext(toProto(dto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void login( LoginRequest request, StreamObserver<User> responseObserver ){
        try {
            UserDTO dto = userService.login(request.getEmail(), request.getPassword());
            responseObserver.onNext(toProto(dto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    @Override
    public void getUser( UserIdRequest request, StreamObserver<User> responseObserver ){
        try {
            responseObserver.onNext(toProto(userService.getUser(request.getId())));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e));
        }
    }

    private User toProto( UserDTO dto ){
        return User.newBuilder()
                .setId(dto.id())
                .setName(dto.name())
                .setEmail(dto.email())
                .setRole(dto.role().name())
                .build();
    }
}
