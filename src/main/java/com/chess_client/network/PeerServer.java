package com.chess_client.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server socket phía client để chờ đối thủ kết nối P2P trong LAN.
 */
public class PeerServer {
    private ServerSocket serverSocket;

    /**
     * Bắt đầu lắng nghe trên một port.
     * Nếu truyền vào 0 thì hệ thống sẽ tự chọn port trống.
     *
     * @param port port muốn lắng nghe, hoặc 0
     * @return port thực tế được sử dụng
     */
    public int start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        return serverSocket.getLocalPort();
    }

    /**
     * Chờ đối thủ kết nối. Gọi hàm này ở thread riêng vì sẽ block.
     */
    public Socket waitForOpponent() throws IOException {
        if (serverSocket == null) {
            throw new IllegalStateException("Server socket chưa được start");
        }
        return serverSocket.accept();
    }

    public void stop() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}
