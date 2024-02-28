package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.LookupResponse;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc.NameServerBlockingStub;


public class ClientMain {
    public static void main(String[] args) {

        System.out.println(ClientMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // to connect to the DNS, target = localhost:5001
        final String host = "localhost";
        final String port = "5001";
        final String qualifier;
        String target = host + ":" + port;

        if (args.length == 0) {
            qualifier = "";
        } else {
            //qualifier = args[0];  TODO diferenciar por qualificador, para fase 2 do projeto
            qualifier = "";
        }

        // receive TupleService server connection from DNS
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        NameServerBlockingStub stub = NameServerGrpc.newBlockingStub(channel);

        // finding the TupleSpace's server address
        System.out.println(qualifier);
        LookupRequest request = LookupRequest.newBuilder().setService("TupleSpaces").setQualifier(qualifier).build();
        LookupResponse response = stub.lookup(request);
        target = response.getServers(0);
        System.out.println("Connected to server: " + target);
    
        // check for valid port number
        try {
            int port_number = Integer.parseInt(port);
            if (port_number < 1024 || port_number > 65535) {
                System.err.println("Invalid port number!");
                return;
            }
        } catch (Exception e) {
            System.err.println("Invalid port number!");
            return;
        }

        CommandProcessor parser = new CommandProcessor(new ClientService(target));
        parser.parseInput();

    }
}
