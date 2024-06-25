package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
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
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServer;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServer.Server;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServer.LookupRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServer.LookupResponse;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ResponseCollector;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.nio.file.attribute.UserPrincipalLookupService;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;

public class ClientService {

    private List<ManagedChannel> channelsCentralized = new ArrayList<>();
    private List<ManagedChannel> channelsReplica = new ArrayList<>();
    private List<ManagedChannel> channelsReplicaBlocking = new ArrayList<>();
    private List<TupleSpacesGrpc.TupleSpacesBlockingStub> stubsCentralized = new ArrayList<>();
    private List<TupleSpacesReplicaGrpc.TupleSpacesReplicaStub> stubsReplica = new ArrayList<>();
    private List<TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub> stubsReplicaBlocking = new ArrayList<>();
    private List<Integer> serversAvailable = new ArrayList<>();

    OrderedDelayer delayer;
    Integer clientId;

    public ClientService(int numServers) {
        /* The delayer can be used to inject delays to the sending of requests to the
            different servers, according to the per-server delays that have been set  */
        delayer = new OrderedDelayer(numServers);

        Random random = new Random();
        clientId = random.nextInt(9999);
    }

    /* This method allows the command processor to set the request delay assigned to a given server */
    public void setDelay(int id, int delay) {
        delayer.setDelay(id, delay);
    }

    public void createChannelsAndStubs() {
        for (Integer port : serversAvailable) {

            // centralized, blocking stub
            ManagedChannel channelCentralized = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build();
            this.channelsCentralized.add(channelCentralized);
            this.stubsCentralized.add(TupleSpacesGrpc.newBlockingStub(channelCentralized));

            // xu liskov, non-blocking stub
            ManagedChannel channelReplica = ManagedChannelBuilder.forAddress("localhost", port + 100).usePlaintext().build();
            this.channelsReplica.add(channelReplica);
            this.stubsReplica.add(TupleSpacesReplicaGrpc.newStub(channelReplica));

            // xu liskov, blocking stub
            ManagedChannel channelReplicaBlocking = ManagedChannelBuilder.forAddress("localhost", port + 100).usePlaintext().build();
            this.channelsReplicaBlocking.add(channelReplicaBlocking);
            this.stubsReplicaBlocking.add(TupleSpacesReplicaGrpc.newBlockingStub(channelReplicaBlocking));
        }
    }

    public void shutdownChannels() {
        for (ManagedChannel channel : channelsCentralized) {
            channel.shutdown();
        } for (ManagedChannel channel : channelsReplica) {
            channel.shutdown();
        } for (ManagedChannel channel : channelsReplicaBlocking) {
            channel.shutdown();
        }
    }

    public void put(String tuple) {
        try {
            ResponseCollector putCollector = new ResponseCollector();
            for (Integer id : delayer) {
                stubsReplica.get(id).put(PutRequest.newBuilder().setNewTuple(tuple).build(), new PutObserver(putCollector));
            }
            putCollector.waitUntilAllReceived(3); // Assuming that there is always three active servers
            System.out.println("OK");
            System.out.println();
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
            System.out.println();
        } catch (InterruptedException ie) {
            System.out.println("Caught InterruptedException");
            System.out.println();
        }
    }

    public void read(String tuple) {
        try {
            ResponseCollector readCollector = new ResponseCollector();
            for (TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stub : stubsReplica) {
                stub.read(ReadRequest.newBuilder().setSearchPattern(tuple).build(), new ReadObserver(readCollector));
            }
            readCollector.waitUntilAllReceived(1);
            String matchingTuple = readCollector.getString(0);
            System.out.println("OK");
            System.out.println(matchingTuple);
            System.out.println();
        } catch (StatusRuntimeException e ) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
            System.out.println();
        } catch (InterruptedException ie) {
            System.out.println("Caught InterruptedException");
            System.out.println();
        }
    }

    public void take(String tuple) {
        try {
            List<List<String>> responses = new ArrayList<>();
            for (Integer id : delayer) {
                TakePhase1Response response = stubsReplicaBlocking.get(id).takePhase1(TakePhase1Request.newBuilder().setSearchPattern(tuple).setClientId(clientId).build());
                List<String> matchingTuples = response.getReservedTuplesList();
                responses.add(matchingTuples);
            }

            List<String> intersection = new ArrayList<>();
            for (String i : responses.get(0)) {
                if (responses.get(1).contains(i) && responses.get(2).contains(i)) {
                    intersection.add(i);
                }
            }
            if (intersection.isEmpty()) {
                for (Integer id : delayer) {
                    stubsReplicaBlocking.get(id).takePhase1Release(TakePhase1ReleaseRequest.newBuilder().setClientId(clientId).build());
                }
                take(tuple);
            } else {
                Random random = new Random();
                String chosen = intersection.get(random.nextInt(intersection.size()));
                for (Integer id : delayer) {
                    stubsReplicaBlocking.get(id).takePhase2(TakePhase2Request.newBuilder().setTuple(chosen).setClientId(clientId).build());
                }
                System.out.println("OK");
                System.out.println(chosen);
                System.out.println();
            }
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
            System.out.println();
        }
    }

    public void getTupleSpacesState() {
        try {
            getTupleSpacesStateResponse response = stubsCentralized.get(0).getTupleSpacesState(getTupleSpacesStateRequest.newBuilder().build());
            List<String> tuples = response.getTupleList();
            System.out.println("OK");
            if (tuples.isEmpty()) {
                System.out.println(tuples);
                System.out.println();
            } else {
                System.out.println(tuples);
                System.out.println();
            }
        } catch (StatusRuntimeException e ) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
            System.out.println();
        }
    }

    public void lookup(String serviceName, String qualifier) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5001).usePlaintext().build();
        NamingServerServiceGrpc.NamingServerServiceBlockingStub stub = NamingServerServiceGrpc.newBlockingStub(channel);
        LookupResponse response = stub.lookup(LookupRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build());
        for(int i = 0; i < response.getServerCount(); i++) {
            int target = Integer.parseInt(response.getServer(i).getServerTarget());
            if (!this.serversAvailable.contains(target)) {
                this.serversAvailable.add(target);
            }
        }
        channel.shutdownNow();
    }

}
