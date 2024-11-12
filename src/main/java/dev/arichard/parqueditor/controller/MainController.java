package dev.arichard.parqueditor.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import dev.arichard.parqueditor.adapter.FieldAdapter;
import dev.arichard.parqueditor.adapter.ParquetFileAdapter;
import dev.arichard.parqueditor.processor.ParquetFileAdapterProcessor;
import dev.arichard.parqueditor.processor.ParquetSchemaProcessor;
import dev.arichard.parqueditor.processor.Processor;
import dev.arichard.parqueditor.processor.RecordsProcessor;
import dev.arichard.parqueditor.service.FileService;
import dev.arichard.parqueditor.service.FxmlService;
import dev.arichard.parqueditor.service.ParquetFileService;
import dev.arichard.parqueditor.service.ThreadService;
import dev.arichard.parqueditor.writer.ParquetWriter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

@Component
@Scope("prototype")
public class MainController implements Initializable {

    @Autowired
    private FxmlService fxmlService;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private FileService<GenericRecord> parquetFileService;

    @FXML
    private TableView<Map<FieldAdapter, StringProperty>> contentTable;

    @FXML
    private VBox fieldContainer;
    
    private ObjectProperty<File> currentFile = new SimpleObjectProperty<>();
 
    private final ExtensionFilter openExtensionFilter = new ExtensionFilter("Parquet", List.of("*.parquet"));

    private final ObservableList<FieldAdapter> fields = FXCollections.observableArrayList();
    
    private final BooleanProperty loading = new SimpleBooleanProperty(false);

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
        loading.addListener((obs, old, val) -> {
            contentTable.getScene().setCursor(val ? Cursor.WAIT : Cursor.DEFAULT);
        });
    }

    @FXML
    private void addField() {
        fields.add(new FieldAdapter(resources.getString("Field") + (fields.size() + 1)));
    }

    @FXML
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(openExtensionFilter);
        fileChooser.setSelectedExtensionFilter(openExtensionFilter);
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            currentFile.set(file);
            contentTable.getItems().clear();
            fields.clear();
            loading.set(true);
            threadService.executeTaskThenUpdateUi(() -> {
                Processor<GenericRecord, ParquetFileAdapter> processor = new ParquetFileAdapterProcessor();
                parquetFileService.consumeFile(file, processor::processLine);
                return processor.getProcessedValue();
            }, adapter -> {
                fields.setAll(adapter.getFields());
                contentTable.getItems().setAll(adapter.getLines());
                loading.set(false);
            }, e -> {
                loading.set(false);
                fxmlService.showAlert(AlertType.ERROR, resources.getString("Error.during.open"), e.getSource().getException().toString());
            });
        }
    }
    
    @FXML
    private void save() {
        if (currentFile == null) {
            openSaveAs();
            return;
        }
        save(currentFile.get());        
    }
    
    @FXML
    private void openSaveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(openExtensionFilter);
        fileChooser.setSelectedExtensionFilter(openExtensionFilter);
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            save(file);
        }
    }
    
    private void save(File file) {
        loading.set(true);
        threadService.executeTaskThenUpdateUi(() -> {
            ParquetSchemaProcessor schemaProcessor = new ParquetSchemaProcessor("schema", "");
            for (FieldAdapter fieldAdapter: fields) {
                schemaProcessor.processLine(fieldAdapter);
            }
            Schema schema = schemaProcessor.getProcessedValue();
            RecordsProcessor recordsProcessor = new RecordsProcessor(schema);
            for (Map<FieldAdapter, StringProperty> line: contentTable.getItems()) {
                recordsProcessor.processLine(line);
            }
            ParquetWriter writer = new ParquetWriter(schema, recordsProcessor.getProcessedValue());
            writer.write(file);           
            return file;
        }, f -> {
            if (!Objects.equals(currentFile.get(), f)) {
                currentFile.set(f);
            }
            loading.set(false);
            fxmlService.showAlert(AlertType.INFORMATION, resources.getString("File.saved"), f.getAbsolutePath());
        }, e -> {
            loading.set(false);
            fxmlService.showAlert(AlertType.ERROR, resources.getString("Error.during.save"), e.getSource().getException().toString());
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
    
    private void setStageTitle(String title) {
        Stage primStage = (Stage) contentTable.getScene().getWindow();
        String baseTitle = primStage.getTitle().split("-")[0].trim();
        primStage.setTitle(baseTitle + " - " + title);
    }

    private void createListeners() {
        currentFile.addListener((obs, old, val) -> {
            if (val == null) return;
            setStageTitle(val.getAbsolutePath());
        });
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
        col.setCellValueFactory(param -> param.getValue().computeIfAbsent(field, f -> new SimpleStringProperty()));
        col.setCellFactory(param -> new EditableTableCell<Map<FieldAdapter, StringProperty>>());
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
