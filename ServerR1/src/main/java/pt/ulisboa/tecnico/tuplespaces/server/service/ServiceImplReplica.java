package pt.ulisboa.tecnico.tuplespaces.server.service;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Response;

import java.util.ArrayList;
import java.util.List;
import io.grpc.stub.StreamObserver;
import static io.grpc.Status.INVALID_ARGUMENT;
import java.util.Random;


public class ServiceImplReplica extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase  {

    private final ServerState serverState;

    public ServiceImplReplica(ServerState serverState) {
        this.serverState = serverState;
    }


    @Override
    public synchronized void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        String newTuple = request.getNewTuple();

        serverState.put(newTuple);
        notifyAll();

        PutResponse response = PutResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        String tuple = request.getSearchPattern();

        String matchingTuple = serverState.read(tuple);

        // Add server delay to test read operation
        Random random = new Random();
        try {
            Thread.sleep(random.nextInt(5000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        while(matchingTuple == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            matchingTuple = serverState.read(tuple);
        }

        ReadResponse response = ReadResponse.newBuilder().setResult(matchingTuple).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void takePhase1(TakePhase1Request request, StreamObserver<TakePhase1Response> responseObserver) {
        String tuple = request.getSearchPattern();
        Integer clientId = request.getClientId();

        List<String> matchingTuples = serverState.takePhase1(tuple, clientId);

        while(matchingTuples == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            matchingTuples = serverState.takePhase1(tuple, clientId);
        }
        TakePhase1Response response = TakePhase1Response.newBuilder().addAllReservedTuples(matchingTuples).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void takePhase1Release(TakePhase1ReleaseRequest request, StreamObserver<TakePhase1ReleaseResponse> responseObserver) {
        Integer clientId = request.getClientId();

        serverState.takePhase1Release(clientId);

        TakePhase1ReleaseResponse response = TakePhase1ReleaseResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void takePhase2(TakePhase2Request request, StreamObserver<TakePhase2Response> responseObserver) {
        String tuple = request.getTuple();
        Integer clientId = request.getClientId();

        serverState.takePhase2(tuple, clientId);

        TakePhase2Response response = TakePhase2Response.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}