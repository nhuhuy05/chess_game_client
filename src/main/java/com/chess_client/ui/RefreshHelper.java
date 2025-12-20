package com.chess_client.ui;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Function;

public class RefreshHelper {

    private static final String EMPTY_STYLE = "-fx-text-fill: #b0b0b0; -fx-font-size: 14px;";

    /**
     * Refresh một container với dữ liệu từ async task
     */
    public static <T> void refreshContainer(VBox container,
            T data,
            String emptyMessage,
            Function<T, Integer> getLength,
            Function<T, Function<Integer, JSONObject>> getItem,
            Function<JSONObject, javafx.scene.Node> createItem) {
        container.getChildren().clear();

        if (getLength.apply(data) == 0) {
            Label emptyLabel = new Label(emptyMessage);
            emptyLabel.setStyle(EMPTY_STYLE);
            container.getChildren().add(emptyLabel);
        } else {
            for (int i = 0; i < getLength.apply(data); i++) {
                JSONObject item = getItem.apply(data).apply(i);
                container.getChildren().add(createItem.apply(item));
            }
        }
    }

    /**
     * Helper để refresh với JSONArray
     */
    public static void refreshJSONArrayContainer(VBox container,
            JSONArray array,
            String emptyMessage,
            Function<JSONObject, javafx.scene.Node> createItem) {
        Platform.runLater(() -> {
            container.getChildren().clear();
            if (array.length() == 0) {
                Label emptyLabel = new Label(emptyMessage);
                emptyLabel.setStyle(EMPTY_STYLE);
                container.getChildren().add(emptyLabel);
            } else {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    container.getChildren().add(createItem.apply(item));
                }
            }
        });
    }
}
