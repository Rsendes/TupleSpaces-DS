package pt.ulisboa.tecnico.tuplespaces.server.service;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateRequest;
import java.util.ArrayList;
import java.util.List;

import static io.grpc.Status.INVALID_ARGUMENT;

public class ServiceImplCentralized extends TupleSpacesGrpc.TupleSpacesImplBase  {

    private final ServerState serverState;

    public ServiceImplCentralized(ServerState serverState) {
        this.serverState = serverState;
    }

    @Override
    public synchronized void getTupleSpacesState(getTupleSpacesStateRequest request, StreamObserver<getTupleSpacesStateResponse> responseObserver) {
        List <String> tuples = serverState.getTupleSpacesState();

        getTupleSpacesStateResponse response = getTupleSpacesStateResponse.newBuilder().addAllTuple(tuples).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

