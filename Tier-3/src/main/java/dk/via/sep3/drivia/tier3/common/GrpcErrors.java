package dk.via.sep3.drivia.tier3.common;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.time.DateTimeException;
import java.util.NoSuchElementException;

public class GrpcErrors {

    private GrpcErrors(){
    }

    public static StatusRuntimeException toGrpcError( Exception e ){
        Status status;
        if (e instanceof NoSuchElementException) {
            status = Status.NOT_FOUND;
        } else if (e instanceof IllegalArgumentException || e instanceof DateTimeException) {
            status = Status.INVALID_ARGUMENT;
        } else if (e instanceof IllegalStateException) {
            status = Status.FAILED_PRECONDITION;
        } else {
            status = Status.INTERNAL;
        }
        return status.withDescription(e.getMessage()).asRuntimeException();
    }
}
