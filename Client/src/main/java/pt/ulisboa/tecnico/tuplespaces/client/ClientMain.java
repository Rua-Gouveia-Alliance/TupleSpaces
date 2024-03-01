package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.LookupResponse;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc.NameServerBlockingStub;


public class ClientMain {

    private static final String NS_HOST = "localhost";
    private static final String NS_PORT = "5001";
    private static final String SERVICE = "TupleSpaces";

    private static String lookupServer(String qualifier) {
        String nsTarget = NS_HOST + ":" + NS_PORT;

        ManagedChannel channel = ManagedChannelBuilder.forTarget(nsTarget).usePlaintext().build();
        NameServerBlockingStub stub = NameServerGrpc.newBlockingStub(channel);

        // finding the TupleSpace's server address
        LookupRequest request = LookupRequest.newBuilder().setService(SERVICE).setQualifier(qualifier).build();
        LookupResponse response = stub.lookup(request);
        // checking if name server found any server providing the desired service
        if (response.getServersCount() == 0) {
            return null;
        }
        return response.getServers(0);
    }

    public static void main(String[] args) {

        String qualifier = "";
        String target = lookupServer(qualifier);
        if (target == null) {
            System.out.println("No server providing the desired service at the moment.");
            System.out.println("Exiting.");
            System.exit(0);
        }

        try {
            CommandProcessor parser = new CommandProcessor(new ClientService(target));
            parser.parseInput();
        } catch (RuntimeException e) {
            System.err.println("Connection to server failed, exiting.");
        }
        System.exit(0);
    }
}
