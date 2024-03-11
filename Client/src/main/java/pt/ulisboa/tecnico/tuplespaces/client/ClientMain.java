package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.ArrayList;
import java.util.Objects;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc.NameServerBlockingStub;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.LookupRequest;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.LookupResponse;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

public class ClientMain {

    public static final int numServers = 3;
    private static final String NS_HOST = "localhost";
    private static final String NS_PORT = "5001";
    private static final String SERVICE = "TupleSpaces";
    private static boolean DEBUG = false;

    public static void debugMessage(String format, Object... args) {
        if (DEBUG)
            System.err.printf("[DEBUG]" + format + "\n", args);
    }

    private static String lookupServer(String qualifier) {
        String nsTarget = NS_HOST + ":" + NS_PORT;

        ManagedChannel channel = ManagedChannelBuilder.forTarget(nsTarget).usePlaintext().build();
        NameServerBlockingStub stub = NameServerGrpc.newBlockingStub(channel);

        // finding the TupleSpace's server address
        LookupRequest request = LookupRequest.newBuilder().setService(SERVICE).setQualifier(qualifier).build();
        LookupResponse response = stub.lookup(request);
        // checking if name server found any server providing the desired service
        if (response.getServersCount() == 0) {
            debugMessage("No server with qualifier %s providing the desired service at the moment", qualifier);
            return null;
        }

        return response.getServersList().get(0);
    }

    public static void main(String[] args) {

        // receive and print arguments
        debugMessage("Received %d arguments", args.length);
        for (int i = 0; i < args.length; i++) {
            debugMessage("arg[%d] = %s", i, args[i]);
            if (args[i].equals("-debug"))
                DEBUG = true;
        }

        if (args.length > 2) {
            System.err.println("Wrong number of arguments.");
            System.err.println("Usage: mvn exec:java -Dexec.args=[qualifier] [-debug]");
            return;
        }

        ArrayList<String> targets = new ArrayList<>();
        // TODO: Should this be hard coded or just get the first three servers of the service
        targets.add(lookupServer("A"));
        targets.add(lookupServer("B"));
        targets.add(lookupServer("C"));

        // TODO: should we crash the client if not available A, B and C??
        if (targets.stream().anyMatch(Objects::isNull)) {
            System.out.println("Exiting.");
            System.exit(0);
        }

        try {
            CommandProcessor parser = new CommandProcessor(new ClientService(targets));
            parser.parseInput();
        } catch (RuntimeException e) {
            System.err.println("Connection to server failed, exiting...");
        }
        System.exit(0);
    }
}
