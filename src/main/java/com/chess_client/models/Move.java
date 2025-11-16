package com.chess_client.models;

public class Move {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private Piece pieceMoved;
    private Piece pieceCaptured;
    private boolean isEnPassant;
    private boolean isCastling;
    private boolean isPromotion;

    public Move(int fromRow, int fromCol, int toRow, int toCol, Piece pieceMoved) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.pieceMoved = pieceMoved;
        this.pieceCaptured = null;
        this.isEnPassant = false;
        this.isCastling = false;
        this.isPromotion = false;
    }

    public int getFromRow() {
        return fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public int getToRow() {
        return toRow;
    }

    public int getToCol() {
        return toCol;
    }

    public Piece getPieceMoved() {
        return pieceMoved;
    }

    public Piece getPieceCaptured() {
        return pieceCaptured;
    }

    public void setPieceCaptured(Piece piece) {
        this.pieceCaptured = piece;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public void setEnPassant(boolean enPassant) {
        this.isEnPassant = enPassant;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public void setCastling(boolean castling) {
        this.isCastling = castling;
    }

    public boolean isPromotion() {
        return isPromotion;
    }

    public void setPromotion(boolean promotion) {
        this.isPromotion = promotion;
    }

    @Override
    public String toString() {
        return pieceMoved + " từ (" + fromRow + "," + fromCol + ") đến (" + toRow + "," + toCol + ")";
    }
}