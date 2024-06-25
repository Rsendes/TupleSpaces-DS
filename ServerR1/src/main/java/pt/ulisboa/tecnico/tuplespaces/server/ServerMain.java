package pt.ulisboa.tecnico.tuplespaces.server;

import java.io.IOException;

import io.grpc.*;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import pt.ulisboa.tecnico.tuplespaces.server.service.ServiceImplReplica;
import pt.ulisboa.tecnico.tuplespaces.server.service.ServiceImplCentralized;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServer;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServer.RegisterRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServer.DeleteRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServer.RegisterResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NamingServerServiceGrpc;
import io.grpc.StatusRuntimeException;

public class ServerMain {
    public static void main(String[] args) throws  IOException, InterruptedException {

    // Receive and print arguments
    // System.out.printf("Received %d arguments%n", args.length);
    for (int i = 0; i < args.length; i++) {
        System.out.printf("arg[%d] = %s%n", i, args[i]);
    }

    // Check number of arguments
    if (args.length != 2) {
        System.err.println("Wrong number of arguments!");
        System.err.println("Usage: mvn exec:java -Dexec.args=\"<port> <server qualifier>\"");
        return;
    }

    final String port = args[0];
    final String qualifier = args[1];
    ServerState serverState = new ServerState();

    // set target
    final String target = "localhost:5001";
	final String serviceName = "TupleSpace";

    final BindableService serviceImplCentralized = new ServiceImplCentralized(serverState);
    final BindableService serviceImplReplica = new ServiceImplReplica(serverState);

    ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    NamingServerServiceGrpc.NamingServerServiceBlockingStub stub = NamingServerServiceGrpc.newBlockingStub(channel);
    try {
        RegisterRequest request = RegisterRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).setServerAddress(port).build();
        stub.register(request);
    } catch (StatusRuntimeException e) {
        System.err.println("Not possible to register the server");
        System.exit(1);
    }

    channel.shutdownNow();

    // Create a new server to listen on port
	Server serverCentralized = ServerBuilder.forPort(Integer.parseInt(port)).addService(serviceImplCentralized).build();
    Server serverReplica = ServerBuilder.forPort(Integer.parseInt(port)+100).addService(serviceImplReplica).build();

	// Start the server
	serverCentralized.start();
    serverReplica.start();

	// Server threads are running in the background.
	System.out.printf("Server started on ports %d and %d\n", Integer.parseInt(port), Integer.parseInt(port) + 100);

     // print termination message
    System.out.println("Press CTRL+C to terminate");

    // Shutdown servers in a safe way
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        ManagedChannel temporaryChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        NamingServerServiceGrpc.NamingServerServiceBlockingStub temporaryStub = NamingServerServiceGrpc.newBlockingStub(temporaryChannel);
        DeleteRequest temporaryRequest = DeleteRequest.newBuilder().setTarget(port).setServiceName(serviceName).build();
        temporaryStub.delete(temporaryRequest);
        temporaryChannel.shutdownNow();
        serverCentralized.shutdown();
        serverReplica.shutdown();
    }));

	// Do not exit the main thread. Wait until server is terminated.
	serverCentralized.awaitTermination();
    serverReplica.awaitTermination();
  }
}

