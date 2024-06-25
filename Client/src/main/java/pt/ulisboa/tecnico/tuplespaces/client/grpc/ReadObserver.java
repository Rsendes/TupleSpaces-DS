package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;

public class ReadObserver implements StreamObserver<ReadResponse> {
    private static boolean DEBUG_MODE = false;
    ResponseCollector collector;

    public static void enableDebugMode(boolean debugFlag) {
        DEBUG_MODE = debugFlag;
    }

    public boolean getDebugMode() {
        return this.DEBUG_MODE;
    }

    public ReadObserver(ResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(ReadResponse r) {
        if (DEBUG_MODE) {
            System.out.println("Received read response: " + r);
        }
        collector.addString(r.getResult());
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error on read: " + throwable);
    }

    @Override
    public void onCompleted() {
        if (DEBUG_MODE) {
            System.out.println("Read Request completed");
        }
    }
}