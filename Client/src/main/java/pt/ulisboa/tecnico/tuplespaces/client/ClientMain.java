package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.PutObserver;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ReadObserver;

import java.util.Objects;

public class ClientMain {

    static boolean DEBUG_FLAG = false;
    static final int numServers = 3;

    /** Helper method to print debug messages. */
    public static void debug(String debugMessage) {
        if (DEBUG_FLAG)
            System.err.println(debugMessage);
    }
    public static void main(String[] args) {

        /* Set flag to true to print debug messages.
         * The flag can be set using the -Ddebug command line option. */
        if (args.length == 1) {
            DEBUG_FLAG = (Objects.equals(args[0], "-Ddebug"));
        }

        // receive and print arguments
        debug(String.format("Received %d arguments%n", args.length));
        for (int i = 0; i < args.length; i++) {
            debug(String.format("arg[%d] = %s%n", i, args[i]));
        }

        ReadObserver.enableDebugMode(DEBUG_FLAG);
        PutObserver.enableDebugMode(DEBUG_FLAG);

        final String service = "TupleSpace";

        // create client service
        ClientService clientService = new ClientService(numServers);

        clientService.lookup(service, "A");
        clientService.lookup(service, "B");
        clientService.lookup(service, "C");

        clientService.createChannelsAndStubs();

        CommandProcessor parser = new CommandProcessor(clientService);

        parser.parseInput();

        clientService.shutdownChannels();

    }
}