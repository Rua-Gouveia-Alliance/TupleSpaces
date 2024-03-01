package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.*;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc.NameServerBlockingStub;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.DeleteRequest;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.DeleteResponse;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.RegisterRequest;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.RegisterResponse;
import pt.ulisboa.tecnico.tuplespaces.server.grpc.TupleSpacesImpl;
import sun.misc.Signal;

import java.io.IOException;

public class ServerMain {

	private static final String ADDRESS = "localhost";
	private static final String NS_HOST = "localhost";
	private static final String NS_PORT = "5001";
	private static final String SERVICE = "TupleSpaces";

	private static boolean checkArguments(String[] args) {
		return checkPort(args[0]);
	}

	private static boolean checkPort(String s) {
		double d = -1;
		if (s == null) {
			return false;
		}
		try {
			d = Double.parseDouble(s);    // check for numeric argument
		} catch (NumberFormatException e) {
			return false;
		}
		return (d >= 1024 && d <= 65535); // check for invalid port numbers
	}

	private static void registerServer(int port, String qualifier) {
		ManagedChannel channel;
		NameServerBlockingStub stub;
		String nsAddr = NS_HOST + ":" + NS_PORT;
		String serverAddr = ADDRESS + ":" + port;

		try {
			channel = ManagedChannelBuilder.forTarget(nsAddr).usePlaintext().build();
			stub = NameServerGrpc.newBlockingStub(channel);

			RegisterRequest request = RegisterRequest.newBuilder().setService(SERVICE).setAddr(serverAddr).setQualifier(qualifier).build();

			@SuppressWarnings("unused")
			RegisterResponse response = stub.register(request);

			channel.shutdown();
		} catch (StatusRuntimeException e) {
			System.err.println("Problem trying to register the server: " + e.getStatus().getDescription());
			System.exit(0);
		}
	}

	private static void deleteServer(int port) {
		ManagedChannel channel;
		NameServerBlockingStub stub;
		String nsAddr = NS_HOST + ":" + NS_PORT;
		String serverAddr = ADDRESS + ":" + port;
		channel = ManagedChannelBuilder.forTarget(nsAddr).usePlaintext().build();

		try {
			stub = NameServerGrpc.newBlockingStub(channel);
			DeleteRequest request = DeleteRequest.newBuilder().setService(SERVICE).setAddr(serverAddr).build();

			@SuppressWarnings("unused")
			DeleteResponse response = stub.delete(request);

			channel.shutdown();
		} catch (StatusRuntimeException e) {
			channel.shutdown();
			System.err.println("Problem trying to delete the server: " + e.getStatus().getDescription());
		}
		System.exit(0);
	}

	private static void startServer(int port, String qualifier) throws IOException, InterruptedException {
		final BindableService impl = new TupleSpacesImpl();

		// create new server
		Server server = ServerBuilder.forPort(port).addService(impl).build();

		// start the server
		try {
			server.start();
		} catch (IOException e) {
			System.err.println(e.toString());
			System.err.println("Port already in use!");
			System.exit(0);
		}
		
		System.out.println("Server started");

		// register in name server
		registerServer(port, qualifier);

		// handle ctrl+c to delete in name server
		Signal.handle(new Signal("INT"), signal -> deleteServer(port));

		// wait until server is terminated
		server.awaitTermination();

		// delete in name server
		deleteServer(port);
	}

	public static void main(String[] args) throws IOException, InterruptedException {


		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		if (args.length != 2) {
			System.err.println("Wrong number of arguments.");
			System.err.println("Usage: mvn exec:java -Dexec.args=<port> <qualifier>");
			return;
		}

		if (!checkArguments(args)) {
			System.err.println("Invalid arguments.");
			return;
		}

		final String port = args[0];
		final String qualifier = args[1];

		startServer(Integer.parseInt(port), qualifier);
	}
}

