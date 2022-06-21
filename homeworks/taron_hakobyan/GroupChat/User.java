import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class User {
    private String name;
    private Socket connection;
    private BufferedReader reader;
    private PrintWriter writer;

    public User(String name) {
        this.name = name;
    }

    static class UserSenderRunnable implements Runnable {
        private User user;
        private PrintWriter writer;
        private Scanner scanner = new Scanner(System.in);

        UserSenderRunnable(PrintWriter writer, User user) {
            this.user = user;
            this.writer = writer;
        }

        @Override
        public void run() {
            while (true) {
                String content = scanner.nextLine();
                if (content.startsWith("/leaveChat")) {
                    leaveChat(user);
                }
                writer.println(content);
            }
        }
    }

    static class UserGetterRunnable implements Runnable {
        private BufferedReader reader;

        UserGetterRunnable(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    boolean ready = reader.ready();
                    if (ready) {
                        System.out.println(reader.readLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void leaveChat(User user) {
        try {
            user.writer.println("/exit");
            user.reader.close();
            user.writer.close();
            user.connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Socket connectToGroupChatServer() {
        try {
            return new Socket("127.0.0.1", 9090);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) {
        System.out.println("Enter your name:");
        Scanner scanner = new Scanner(System.in);
        User user = new User(scanner.nextLine());
        try {
            user.connection = connectToGroupChatServer();
            PrintWriter writer = new PrintWriter(user.connection.getOutputStream(), true);
            writer.println(user.name);
            user.reader = new BufferedReader(
                    new InputStreamReader(user.connection.getInputStream())
            );
            Thread thread1 = new Thread(new UserSenderRunnable(writer, user));
            thread1.start();
            Thread thread2 = new Thread(new UserGetterRunnable(user.reader));
            thread2.start();

        } catch (IOException e) {
            try {
                user.connection.close();
            } catch (IOException e1) {
                e.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}


