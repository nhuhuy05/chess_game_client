package com.chess_client.ai;

import com.chess_client.models.Board;
import com.chess_client.models.Move;
import com.chess_client.models.Piece;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StockfishEngine {
    private Process engineProcess;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String enginePath;
    private int skillLevel = 10; // 0-20, 20 là mạnh nhất
    private int thinkingTime = 1000; // milliseconds

    public StockfishEngine(String enginePath) {
        this.enginePath = enginePath;
    }

    // Khởi động engine
    public boolean startEngine() {
        try {
            ProcessBuilder pb = new ProcessBuilder(enginePath);
            pb.redirectErrorStream(true); // gộp stderr vào stdout
            engineProcess = pb.start();

            reader = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(engineProcess.getOutputStream()));

            // gửi lệnh UCI
            sendCommand("uci");

            // chờ "uciok" với timeout
            if (!waitForOutput("uciok", 2000)) {
                System.err.println("Stockfish không phản hồi uciok!");
                return false;
            }

            // cấu hình engine
            sendCommand("setoption name Skill Level value " + skillLevel);
            sendCommand("isready");

            // chờ readyok
            if (!waitForOutput("readyok", 2000)) {
                System.err.println("Stockfish không phản hồi readyok!");
                return false;
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean waitForOutput(String keyword, long timeoutMs) {
        long start = System.currentTimeMillis();

        try {
            String line;
            while (System.currentTimeMillis() - start < timeoutMs) {
                if (reader.ready() && (line = reader.readLine()) != null) {
                    if (line.contains(keyword)) return true;
                }
                Thread.sleep(10);
            }
        } catch (Exception ignored) {}

        return false;
    }

    // Gửi lệnh đến engine
    private void sendCommand(String command) throws IOException {
        writer.write(command + "\n");
        writer.flush();
    }

    // Lấy nước đi tốt nhất từ Stockfish
    public Move getBestMove(Board board, List<Move> moveHistory) {
        try {
            // Tạo chuỗi FEN (Forsyth-Edwards Notation) hoặc dùng UCI moves
            String movesString = getUCIMoves(moveHistory);

            // Gửi vị trí hiện tại
            sendCommand("position startpos" + (movesString.isEmpty() ? "" : " moves " + movesString));

            // Yêu cầu tính toán
            sendCommand("go movetime " + thinkingTime);

            // Đọc phản hồi
            String bestMove = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 2) {
                        bestMove = parts[1];
                    }
                    break;
                }
            }

            if (bestMove != null && bestMove.length() >= 4) {
                return parseUCIMove(bestMove, board);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Chuyển đổi lịch sử nước đi sang định dạng UCI
    private String getUCIMoves(List<Move> moveHistory) {
        if (moveHistory == null || moveHistory.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Move move : moveHistory) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(moveToUCI(move));
        }
        return sb.toString();
    }

    // Chuyển Move thành UCI format (e.g., "e2e4")
    private String moveToUCI(Move move) {
        String from = "" + (char)('a' + move.getFromCol()) + (8 - move.getFromRow());
        String to = "" + (char)('a' + move.getToCol()) + (8 - move.getToRow());

        // Thêm ký tự phong cấp nếu có
        if (move.isPromotion()) {
            return from + to + "q"; // Mặc định phong hậu
        }

        return from + to;
    }

    // Parse UCI move thành Move object
    private Move parseUCIMove(String uciMove, Board board) {
        if (uciMove.length() < 4) {
            return null;
        }

        int fromCol = uciMove.charAt(0) - 'a';
        int fromRow = 8 - (uciMove.charAt(1) - '0');
        int toCol = uciMove.charAt(2) - 'a';
        int toRow = 8 - (uciMove.charAt(3) - '0');

        Piece piece = board.getPiece(fromRow, fromCol);
        if (piece == null) {
            return null;
        }

        Move move = new Move(fromRow, fromCol, toRow, toCol, piece);

        // Kiểm tra phong cấp
        if (uciMove.length() >= 5) {
            move.setPromotion(true);
        }

        // Kiểm tra nhập thành
        if (piece.getType() == Piece.Type.KING && Math.abs(fromCol - toCol) == 2) {
            move.setCastling(true);
        }

        // Kiểm tra en passant
        if (piece.getType() == Piece.Type.PAWN && Math.abs(fromCol - toCol) == 1
                && board.getPiece(toRow, toCol) == null) {
            move.setEnPassant(true);
        }

        return move;
    }

    // Đặt độ khó (0-20)
    public void setSkillLevel(int level) {
        this.skillLevel = Math.max(0, Math.min(20, level));
        try {
            sendCommand("setoption name Skill Level value " + skillLevel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Đặt thời gian suy nghĩ (milliseconds)
    public void setThinkingTime(int milliseconds) {
        this.thinkingTime = milliseconds;
    }

    // Đánh giá vị trí (centipawns)
    public int evaluatePosition(Board board, List<Move> moveHistory) {
        try {
            String movesString = getUCIMoves(moveHistory);
            sendCommand("position startpos" + (movesString.isEmpty() ? "" : " moves " + movesString));
            sendCommand("go depth 10");

            String line;
            int evaluation = 0;
            while ((line = reader.readLine()) != null) {
                if (line.contains("score cp")) {
                    String[] parts = line.split(" ");
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (parts[i].equals("cp")) {
                            evaluation = Integer.parseInt(parts[i + 1]);
                            break;
                        }
                    }
                }
                if (line.startsWith("bestmove")) {
                    break;
                }
            }
            return evaluation;
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Dừng engine
    public void stopEngine() {
        try {
            if (writer != null) {
                sendCommand("quit");
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (engineProcess != null) {
                engineProcess.destroy();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return engineProcess != null && engineProcess.isAlive();
    }
}