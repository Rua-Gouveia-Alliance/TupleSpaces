package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.LookupResponse;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc.NameServerBlockingStub;


public class ClientMain {

    private static final String DNS_HOST = "localhost";
    private static final String DNS_PORT = "5001";
    private static final String SERVICE = "TupleSpaces";

    private static String lookupServer(String qualifier) {
        String dnsTarget = DNS_HOST + ":" + DNS_PORT;

        ManagedChannel channel = ManagedChannelBuilder.forTarget(dnsTarget).usePlaintext().build();
        NameServerBlockingStub stub = NameServerGrpc.newBlockingStub(channel);

        // finding the TupleSpace's server address
        LookupRequest request = LookupRequest.newBuilder().setService("TupleSpaces").setQualifier(qualifier).build();
        LookupResponse response = stub.lookup(request);
        return response.getServers(0);
    }

    public static void main(String[] args) {

        System.out.println(ClientMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        String qualifier = "";
        if (args.length != 0) {
            //qualifier = args[0];  TODO diferenciar por qualificador, para fase 2 do projeto
            qualifier = "";
        }

        String target = lookupServer(qualifier);
        CommandProcessor parser = new CommandProcessor(new ClientService(target));
        System.out.println("Connected to server: " + target);
        parser.parseInput();

    }
}
