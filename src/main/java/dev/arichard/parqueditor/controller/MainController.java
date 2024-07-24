package dev.arichard.parqueditor.controller;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import dev.arichard.parqueditor.adapter.FieldAdapter;
import dev.arichard.parqueditor.service.FxmlService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

@Component
@Scope("prototype")
public class MainController implements Initializable {
    
    @Autowired
    private FxmlService fxmlService;

    @FXML
    private TableView<Map<FieldAdapter, StringProperty>> contentTable;

    @FXML
    private VBox fieldContainer;

    private final ObservableList<FieldAdapter> fields = FXCollections.observableArrayList();

    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        contentTable.getItems().add(new HashMap<>());
        createListeners();
        contentTable.setRowFactory(
                new Callback<TableView<Map<FieldAdapter, StringProperty>>, TableRow<Map<FieldAdapter, StringProperty>>>() {
                    @Override
                    public TableRow<Map<FieldAdapter, StringProperty>> call(
                            TableView<Map<FieldAdapter, StringProperty>> param) {
                        final TableRow<Map<FieldAdapter, StringProperty>> row = new TableRow<>();
                        final ContextMenu rowMenu = new ContextMenu();
                        rowMenu.getItems()
                                .addAll(createMenuItem(concatLocales(" ", "Add", "row", "before"),
                                        e -> addRow(row.getItem(), 0)),
                                        createMenuItem(concatLocales(" ", "Add", "row", "after"),
                                                e -> addRow(row.getItem(), 1)),
                                        createMenuItem(concatLocales(" ", "Delete", "row"),
                                                e -> contentTable.getItems().remove(row.getItem())));
                        row.contextMenuProperty()
                                .bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(rowMenu));
                        return row;
                    }
                });
    }

    private String concatLocales(String sep, String... locales) {
        return String.join(sep, Arrays.stream(locales).map(l -> resources.getString(l)).toArray(String[]::new));
    }

    private MenuItem createMenuItem(String text, Consumer<ActionEvent> action) {
        MenuItem menuItem = new MenuItem(text);
        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                action.accept(event);
            }
        });
        return menuItem;
    }

    private void addRow(Map<FieldAdapter, StringProperty> reference, int delta) {
        int idx = contentTable.getItems().indexOf(reference);
        if (idx < 0)
            return;
        contentTable.getItems().add(Math.max(0, idx + delta), new HashMap<>());
    }

    @FXML
    private void addField() {
        fields.add(new FieldAdapter(resources.getString("Field") + (fields.size() + 1)));
    }

    private void createListeners() {
        fields.addListener(new ListChangeListener<FieldAdapter>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends FieldAdapter> c) {
                while (c.next()) {
                    if (c.getAddedSize() > 0) {
                        addFieldControls(c.getAddedSubList());
                    }
                    if (c.getRemovedSize() > 0) {
                        removeFieldControls(c.getRemoved());
                    }
                }
            }
        });
    }

    private void addFieldControls(List<? extends FieldAdapter> fields) {
        for (FieldAdapter field : fields) {
            addFieldControl(field);
        }
    }

    private void addFieldControl(FieldAdapter field) {
        AtomicBoolean success = new AtomicBoolean(false);
        FieldControl fieldControl = new FieldControl((view, control) -> {
            success.set(fxmlService.safeLoad(view, control) != null);
            return success.get();
        }, resources, field);
        if (!success.get()) {
            return;
        }
        fieldControl.setOnRemove(() -> {
            fields.remove(field);
        });
        fieldContainer.getChildren().add(fieldControl);
        TableColumn<Map<FieldAdapter, StringProperty>, String> col = new TableColumn<>();
        col.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Map<FieldAdapter, StringProperty>, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(
                            CellDataFeatures<Map<FieldAdapter, StringProperty>, String> param) {
                        return param.getValue().computeIfAbsent(field, f -> {
                            StringProperty s = new SimpleStringProperty();
                            s.addListener((obs, old, val) -> {
                                if (field.getDefaultValue() != null && !field.getDefaultValue().isBlank()
                                        && (val == null || val.isBlank())) {
                                    s.set(field.getDefaultValue());
                                }
                            });
                            field.defaultValueProperty().addListener((obs, old, val) -> {
                                if (val != null && !val.isBlank() && (s.get() == null || s.get().isBlank())) {
                                    s.set(val);
                                }
                            });
                            return s;
                        });
                    }
                });
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setUserData(field);
        col.textProperty().bind(field.nameProperty());
        contentTable.getColumns().add(col);
    }

    private void removeFieldControls(List<? extends FieldAdapter> fields) {
        for (FieldAdapter field : fields) {
            removeFieldControl(field);
        }
    }

    private void removeFieldControl(FieldAdapter field) {
        removeFieldControlFromList(fieldContainer.getChildren(), field);
        removeFieldControlFromList(contentTable.getColumns(), field);
        for (Map<FieldAdapter, StringProperty> map : contentTable.getItems()) {
            map.remove(field);
        }
    }

    private <T> void removeFieldControlFromList(List<T> nodes, FieldAdapter field) {
        int idx = -1;
        int i = 0;
        while (i < nodes.size() && idx < 0) {
            T node = nodes.get(i);
            if (node instanceof Node && field.equals((FieldAdapter) ((Node) node).getUserData())) {
                idx = i;
            } else if (node instanceof TableColumn
                    && field.equals((FieldAdapter) ((TableColumn<?, ?>) node).getUserData())) {
                idx = i;
            }
            i++;
        }
        if (idx >= 0) {
            nodes.remove(idx);
        }
    }
}
