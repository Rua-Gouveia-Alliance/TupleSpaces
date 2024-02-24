package pt.ulisboa.tecnico.tuplespaces.server;

public class ServerMain {

  private static boolean checkArguments(String[] args) {
    if (checkPort(args[0]) && checkQualifier(args[1])) {
      return true;
    }
    return false;
  }

  private static boolean checkQualifier(String s) {
    if (s != "A" && s != "B" && s != "C") return false;
    return true;
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
    if (d < 0 || d > 65535) {   // check for invalid port numbers
      return false;
    }
    return true;
  }

  public static void main(String[] args) {

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
      System.exit(0);
    }

    final String port = args[0];
    final String qualifier = args[1];

  }
}

