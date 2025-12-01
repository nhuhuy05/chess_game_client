package com.chess_client.services;

import com.chess_client.models.Piece;
import com.chess_client.network.PeerClient;
import com.chess_client.network.PeerServer;
import com.chess_client.network.PeerService;
import org.json.JSONObject;

import java.net.Socket;

public class HomeService {

    private final PeerService peerService;

    public HomeService(String baseUrl, String jwtToken) {
        this.peerService = new PeerService(baseUrl, jwtToken);
    }

    /**
     * Thực hiện toàn bộ quy trình ghép trận + thiết lập kết nối P2P.
     * Trả về HomeMatchmakingResult để Controller quyết định hiển thị UI.
     */
    public HomeMatchmakingResult startRandomMatch() throws Exception {
        PeerServer peerServer = new PeerServer();
        int localPort = peerServer.start(0);

        JSONObject res = peerService.joinMatchmaking(localPort);
        int status = res.optInt("statusCode", 0);

        if (status == 200 && "Match Found!".equals(res.optString("message"))) {
            return buildMatchResult(res, peerServer);
        } else if (status == 202) {
            boolean found = false;
            JSONObject finalRes = null;

            for (int i = 0; i < 15; i++) {
                Thread.sleep(1000);
                JSONObject statusRes = peerService.checkMatchStatus();
                int st = statusRes.optInt("statusCode", 0);
                if (st == 200 && "Match Found!".equals(statusRes.optString("message"))) {
                    found = true;
                    finalRes = statusRes;
                    break;
                } else if (st == 404) {
                    break;
                }
            }

            if (!found) {
                return HomeMatchmakingResult.notFound();
            }

            return buildMatchResult(finalRes, peerServer);
        } else {
            return HomeMatchmakingResult.error(
                    res.optString("message", "Ghép trận thất bại.")
            );
        }
    }

    private HomeMatchmakingResult buildMatchResult(JSONObject res, PeerServer peerServer) throws Exception {
        String colorStr = res.getString("color");
        JSONObject opponent = res.getJSONObject("opponent");

        String opponentIp = opponent.getString("ip");
        int opponentPort = opponent.getInt("port");

        Piece.Color color = "white".equalsIgnoreCase(colorStr)
                ? Piece.Color.WHITE
                : Piece.Color.BLACK;

        Socket socket;
        if (color == Piece.Color.WHITE) {
            socket = peerServer.waitForOpponent();
        } else {
            PeerClient client = new PeerClient();
            socket = client.connectToOpponent(opponentIp, opponentPort);
        }

        return HomeMatchmakingResult.success(res, socket, color);
    }
}


