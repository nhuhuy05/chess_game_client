package com.chess_client.models;

public class Piece {
    public enum Type {
        KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
    }

    public enum Color {
        WHITE, BLACK
    }

    private Type type;
    private Color color;
    private boolean hasMoved; // Dùng cho nhập thành và ăn en passant

    public Piece(Type type, Color color) {
        this.type = type;
        this.color = color;
        this.hasMoved = false;
    }

    public Type getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setMoved(boolean moved) {
        this.hasMoved = moved;
    }

    public void setType(Type type) {
        this.type = type;
    }

    // Lấy ký hiệu Unicode cho quân cờ
    public String getUnicode() {
        switch (type) {
            case KING:
                return color == Color.WHITE ? "♔" : "♚";
            case QUEEN:
                return color == Color.WHITE ? "♕" : "♛";
            case ROOK:
                return color == Color.WHITE ? "♖" : "♜";
            case BISHOP:
                return color == Color.WHITE ? "♗" : "♝";
            case KNIGHT:
                return color == Color.WHITE ? "♘" : "♞";
            case PAWN:
                return color == Color.WHITE ? "♙" : "♟";
            default:
                return "";
        }
    }

    @Override
    public String toString() {
        return color + " " + type;
    }
}