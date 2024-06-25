package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutResponse;

public class PutObserver implements StreamObserver<PutResponse> {
    private static boolean DEBUG_MODE = false;

    ResponseCollector collector;

    public static void enableDebugMode(boolean debugFlag) {
        DEBUG_MODE = debugFlag;
    }

    public PutObserver(ResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(PutResponse r) {
        if (DEBUG_MODE) {
            System.out.println("Received put response");
        }
        collector.addString("put response");
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error on put: " + throwable);
    }

    @Override
    public void onCompleted() {
        if (DEBUG_MODE) {
            System.out.println("Put Request completed");
        }
    }
}