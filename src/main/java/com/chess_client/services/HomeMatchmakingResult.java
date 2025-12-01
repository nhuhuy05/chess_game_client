package com.chess_client.services;

import com.chess_client.models.Piece;
import org.json.JSONObject;

import java.net.Socket;

public class HomeMatchmakingResult {

    public enum Status {
        SUCCESS,
        NOT_FOUND,
        ERROR
    }

    private final Status status;
    private final JSONObject matchJson;
    private final Socket socket;
    private final Piece.Color color;
    private final String errorMessage;

    private HomeMatchmakingResult(Status status,
            JSONObject matchJson,
            Socket socket,
            Piece.Color color,
            String errorMessage) {
        this.status = status;
        this.matchJson = matchJson;
        this.socket = socket;
        this.color = color;
        this.errorMessage = errorMessage;
    }

    public static HomeMatchmakingResult success(JSONObject json, Socket socket, Piece.Color color) {
        return new HomeMatchmakingResult(Status.SUCCESS, json, socket, color, null);
    }

    public static HomeMatchmakingResult notFound() {
        return new HomeMatchmakingResult(Status.NOT_FOUND, null, null, null, null);
    }

    public static HomeMatchmakingResult error(String message) {
        return new HomeMatchmakingResult(Status.ERROR, null, null, null, message);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isNotFound() {
        return status == Status.NOT_FOUND;
    }

    public JSONObject getMatchJson() {
        return matchJson;
    }

    public Socket getSocket() {
        return socket;
    }

    public Piece.Color getColor() {
        return color;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
