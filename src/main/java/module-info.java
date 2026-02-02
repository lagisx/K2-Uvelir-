module org.example.uvelirkurs {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires org.json;
    opens org.example.uvelirkurs to javafx.fxml;

    opens org.example.uvelirkurs.controllers to javafx.fxml;

    exports org.example.uvelirkurs;
}
