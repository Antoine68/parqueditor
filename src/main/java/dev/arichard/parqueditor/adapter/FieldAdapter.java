package dev.arichard.parqueditor.adapter;

import org.apache.avro.Schema;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FieldAdapter {

    private final StringProperty name = new SimpleStringProperty();

    private final ObjectProperty<Schema.Type> type = new SimpleObjectProperty<>(Schema.Type.STRING);

    private final StringProperty defaultValue = new SimpleStringProperty();

    private final BooleanProperty nullable = new SimpleBooleanProperty(true);

    public FieldAdapter(String name) {
        this.name.set(name);
    }

    public FieldAdapter(String name, Schema.Type type, String defaultValue, boolean nullable) {
        this.name.set(name);
        this.type.set(type);
        this.defaultValue.set(defaultValue);
        this.nullable.set(nullable);
    }
    
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public Schema.Type getType() {
        return type.get();
    }

    public void setType(Schema.Type type) {
        this.type.set(type);
    }

    public ObjectProperty<Schema.Type> typeProperty() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue.get();
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue.set(defaultValue);
    }

    public StringProperty defaultValueProperty() {
        return defaultValue;
    }

    public boolean getNullable() {
        return nullable.get();
    }

    public void setNullable(boolean nullable) {
        this.nullable.set(nullable);
    }

    public BooleanProperty nullableProperty() {
        return nullable;
    }

}
