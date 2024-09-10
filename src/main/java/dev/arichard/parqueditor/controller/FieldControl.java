package dev.arichard.parqueditor.controller;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.apache.avro.Schema;

import dev.arichard.parqueditor.adapter.FieldAdapter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class FieldControl extends VBox {

    @FXML
    private TextField name;

    @FXML
    private ComboBox<Schema.Type> type;

    @FXML
    private TextField defaultValue;

    @FXML
    private CheckBox nullable;

    @FXML
    private VBox detailContainer;

    @FXML
    private Text toggle;

    private BooleanProperty detailVisible = new SimpleBooleanProperty(true);

    private final ObservableList<Schema.Type> typeChoices = FXCollections.observableArrayList(Schema.Type.BOOLEAN,
            Schema.Type.DOUBLE, Schema.Type.FLOAT, Schema.Type.INT, Schema.Type.LONG, Schema.Type.STRING, Schema.Type.BYTES);

    private final ResourceBundle resources;

    private Runnable onRemove;

    private final FieldAdapter field;

    public FieldControl(BiPredicate<String, Object> viewBuilder, ResourceBundle resources, FieldAdapter field) {
        this.resources = resources;
        this.field = field;
        if (viewBuilder.test("/fxml/field_control.fxml", this)) {
            this.type.getItems().setAll(typeChoices);
            this.setUserData(this.field);
            createListeners();
            toggle();
        }
    }

    @FXML
    private void remove() {
        if (onRemove != null) {
            onRemove.run();
        }
    }

    @FXML
    private void toggle() {
        detailVisible.set(!detailVisible.get());
    }

    private void createListeners() {
        name.textProperty().bindBidirectional(field.nameProperty());
        type.valueProperty().bindBidirectional(field.typeProperty());
        defaultValue.focusedProperty().addListener((obs, old, val) -> {
            if (!val) {
                field.setDefaultValue(defaultValue.getText());
            }
        });
        nullable.selectedProperty().bindBidirectional(field.nullableProperty());
        detailVisible.addListener((obs, old, val) -> {
            detailContainer.setVisible(val);
            detailContainer.setManaged(val);
            toggle.setText(val ? "˅" : ">");
        });
    }

    public FieldAdapter getField() {
        return field;
    }

    public void setOnRemove(Runnable onRemove) {
        this.onRemove = onRemove;
    }

}
