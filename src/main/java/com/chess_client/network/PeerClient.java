package com.chess_client.network;

import java.io.IOException;
import java.net.Socket;

/**
 * Client socket phía client để chủ động kết nối tới đối thủ P2P.
 */
public class PeerClient {

    public Socket connectToOpponent(String ip, int port) throws IOException {
        return new Socket(ip, port);
    }
}
