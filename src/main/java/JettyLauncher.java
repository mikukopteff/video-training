import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.TimeZone;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.jetty.nio.SelectChannelConnector;

public class JettyLauncher {
    private static final int DEFAULT_PORT = 8080;
    private static final int SHUTDOWN_TIMEOUT = 3000;
    private static Server server = new Server();

    public static void main(String[] args) throws Exception {
        int port = Integer.getInteger("port", DEFAULT_PORT);

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setMaxIdleTime(1000 * 60 * 10);
        connector.setPort(port);
        connector.setAcceptQueueSize(350);
        server.setConnectors(new Connector[]{connector});

        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setContextPath("/igor");

        ProtectionDomain protectionDomain = JettyLauncher.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        context.setWar(location.toExternalForm());

        new JettyStopMonitorThread(port + 1).start();
        server.setGracefulShutdown(SHUTDOWN_TIMEOUT);
        server.addHandler(context);
        server.start();
        server.join();
        System.out.println("*** embedded server stopped");
        System.exit(0);
    }

    private static class JettyStopMonitorThread extends Thread {
        private ServerSocket socket;
        public JettyStopMonitorThread(int port) {
            setDaemon(true);
            setName("StopMonitor");
            try {
                socket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            System.out.println("*** running jetty 'stop' thread");
            Socket accept;
            try {
                for(;;) {
                    accept = socket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
                    String line = reader.readLine();
                    accept.close();
                    if (line.startsWith("GET /stopjetty"))
                        break;
                    accept.close();
                }
                System.out.println("*** stopping jetty embedded server");
                server.stop();
                socket.close();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
