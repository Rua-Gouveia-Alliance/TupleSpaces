package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.LookupResponse;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.*;


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
        String target = host + ":" + port;
        final String qualifier = args[0];

        // receive TupleService server connection from DNS
        ClientService dnsService = new ClientService(target);

        // A que servidor é que o cliente se deve ligar
        dnsService.lookup("TupleSpaces", qualifier);
        target = dnsService.lookup("TupleSpaces", qualifier).getServers(0);
    
        // check for valid port number
        try {
            int port_number = Integer.parseInt(port);
            if (port_number < 0 || port_number > 65535) {
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
