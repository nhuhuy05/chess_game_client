package com.chess_client.controllers.admin.utils;

import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class để inject components từ FXML views
 */
public class ComponentInjector {

    /**
     * Extract TableColumns từ TableView và trả về Map với key là column ID
     */
    public static <T> Map<String, TableColumn<T, ?>> extractTableColumns(TableView<T> tableView) {
        Map<String, TableColumn<T, ?>> columnMap = new HashMap<>();
        if (tableView != null) {
            for (TableColumn<T, ?> col : tableView.getColumns()) {
                String id = col.getId();
                if (id != null) {
                    columnMap.put(id, col);
                }
            }
        }
        return columnMap;
    }

    /**
     * Lookup component từ root bằng ID
     */
    @SuppressWarnings("unchecked")
    public static <T> T lookup(Parent root, String id, Class<T> type) {
        return (T) root.lookup("#" + id);
    }
}
