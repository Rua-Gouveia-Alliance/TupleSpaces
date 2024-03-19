package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc.NameServerBlockingStub;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.LookupRequest;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.LookupResponse;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

import java.util.ArrayList;
import java.util.Objects;

public class ClientMain {

    private static final String SERVICE = "TupleSpaces";
    private static boolean DEBUG = false;

    public static void debugMessage(String format, Object... args) {
        if (DEBUG)
            System.err.printf("[DEBUG]" + format + "\n", args);
    }

    private static boolean checkArguments(String[] args) {
        if (args.length == 4 && !args[3].equals("-debug")) {
            return false;
        }
        return checkPorts(args[1], args[2]);
    }

    private static boolean checkPorts(String NSPort, String SeqPort) {
        int ns = isNumeric(NSPort);
        int seq = isNumeric(SeqPort);
        if (ns == -1 || seq == -1 || ns == seq)
            return false;
        
        return (ns >= 1024 && ns <= 65535) && (seq >= 1024 && seq <= 65535); // check for invalid port numbers
    }

    private static int isNumeric(String s) {
        int d;
        if (s == null) {
            return -1;
        }
        try {
            d = Integer.parseInt(s);    // check for numeric argument
        } catch (NumberFormatException e) {
            return -1;
        }
        return d;
    }

    private static String lookupServer(String nsTarget, String qualifier) {

        ManagedChannel channel = ManagedChannelBuilder.forTarget(nsTarget).usePlaintext().build();
        NameServerBlockingStub stub = NameServerGrpc.newBlockingStub(channel);

        // finding the TupleSpace's server address
        LookupRequest request = LookupRequest.newBuilder().setService(SERVICE).setQualifier(qualifier).build();
        LookupResponse response = stub.lookup(request);
        channel.shutdown();
        // checking if name server found any server providing the desired service
        if (response.getServersCount() == 0) {
            debugMessage("No server with qualifier %s providing the desired service at the moment", qualifier);
            return null;
        }

        return response.getServersList().get(0);
    }

    public static void main(String[] args) {

        if (args.length == 4 && args[3].equals("-debug"))
            DEBUG = true;

        // receive and print arguments
        debugMessage("Received %d arguments", args.length);
        for (int i = 0; i < args.length; i++) {
            debugMessage("arg[%d] = %s", i, args[i]);
        }

        if (args.length < 3 || args.length > 4) {
            System.err.println("Wrong number of arguments");
            System.err.println("Usage: mvn exec:java -Dexec.args=<hostNS> <portNS> <portSeq> [-debug]");
            return;
        }

        if (!checkArguments(args)) {
            System.err.println("Invalid arguments");
            System.err.println("Usage: mvn exec:java -Dexec.args=<hostNS> <portNS> <portSeq> [-debug]");
            System.out.println("Exiting...");
            return;
        }

        String host = args[0];
        String port = args[1];
        String portSeq = args[2];
        String target = host + ":" + port;
        String seqTarget = "localhost:" + portSeq;

        ArrayList<String> targets = new ArrayList<>();
        targets.add(lookupServer(target, "A"));
        targets.add(lookupServer(target, "B"));
        targets.add(lookupServer(target, "C"));

        if (targets.stream().anyMatch(Objects::isNull)) {
            System.err.println("Unable to find all replicas");
            System.out.println("Exiting...");
            System.exit(0);
        }

        try {
            CommandProcessor parser = new CommandProcessor(new ClientService(targets, seqTarget));
            parser.parseInput();
        } catch (RuntimeException e) {
            System.err.println("Connection to server failed, exiting...\n" + e.toString());
        }
        System.exit(0);
    }
}
