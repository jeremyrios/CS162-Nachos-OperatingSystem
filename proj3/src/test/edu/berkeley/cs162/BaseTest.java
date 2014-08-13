package edu.berkeley.cs162;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class BaseTest {

    private static volatile KVServer _kvServer;
    private static volatile SocketServer _socketServer;
    private static final AtomicBoolean _serverRunning = new AtomicBoolean();

    private static Thread _serverThread;

    private static final int _numSets = 100;
    private static final int _maxElemsPerSet = 10;

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8080;


    protected static void startServer() throws Exception {

        _serverRunning.set(false);

        final Runnable serverRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    _kvServer = new KVServer(_numSets, _maxElemsPerSet);
                    _socketServer = new SocketServer(HOSTNAME, PORT);
                    final NetworkHandler handler = new KVClientHandler(_kvServer);
                    _socketServer.addHandler(handler);
                    _socketServer.connect();
                    _serverRunning.set(true);
                    _socketServer.run();
                } catch (final NullPointerException e) {
                    // eat it and let the thread die.
                } catch (final IOException e) {
                    e.printStackTrace();
                    System.err.println("Unable to start server!");
                }
            }
        };

        _serverThread = new Thread(serverRunner);

        _serverThread.start();
        while (!_serverRunning.get()) {
            Thread.sleep(1);
        }

    }

    protected static KVClient newClient() throws Exception {
        return new KVClient(HOSTNAME, PORT);
    }

    @SuppressWarnings("deprecation")
    protected static void stopServer() throws Exception {
        _serverThread.stop(); // hack
        _serverRunning.set(false);
        if (_socketServer != null) _socketServer.stop();
        _kvServer = null;
        _socketServer = null;
    }

}
