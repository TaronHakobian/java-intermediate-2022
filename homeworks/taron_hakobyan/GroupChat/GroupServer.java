import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class GroupServer {
    private static ServerSocket server;
    private static ArrayList<Socket> connections = new ArrayList<>();
    private static HashMap<String, BufferedReader> mapRead = new HashMap<>();
    private static HashMap<String, PrintWriter> mapWrite = new HashMap<>();

    static {
        try {
            server = new ServerSocket(9090);
        } catch (IOException e) {
            try {
                server.close();
            } catch (IOException e1) {
                e.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private static void serverAccept() {
        while (true) {
            try {
                Socket connection = server.accept();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );
                String connectionName = reader.readLine();
                connections.add(connection);
                mapRead.put(connectionName, new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                ));
                mapWrite.put(connectionName, new PrintWriter(
                        connection.getOutputStream(), true
                ));

                Thread thread = new Thread(
                        new ChattingConnectorForGroupChat(connectionName
                                , connection)
                );
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ChattingConnectorForGroupChat implements Runnable {
        private String connectionName;
        private Socket socket;

        ChattingConnectorForGroupChat(String connectionName, Socket socket) {
            this.connectionName = connectionName;
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader reader = mapRead.get(connectionName);
            PrintWriter writer = mapWrite.get(connectionName);
            while (true) {
                try {
                    boolean readyToRead = reader.ready();
                    while (readyToRead) {
                        String content = reader.readLine();
                        if (content.startsWith("/leaveChat")) {
                            removeUser(connectionName, socket);
                            break;
                        }
                        for (Socket s : connections) {
                            PrintWriter printer = new PrintWriter(
                                    s.getOutputStream(), true
                            );
                            printer.println(connectionName + ": " + content);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void removeUser(String connectionName, Socket connection) {
        try {
            connection.close();
            connections.remove(connection);
            mapRead.get(connectionName).close();
            mapRead.remove(connectionName);
            mapWrite.get(connectionName).close();
            mapWrite.remove(connectionName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        serverAccept();
    }
}