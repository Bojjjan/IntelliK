module zenit {
    requires antlr4;
    requires com.kodedu.terminalfx;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires javafx.fxml;
    requires org.antlr.antlr4.runtime;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires richtextfx;
    requires wellbehavedfx;
    requires reactfx;
    requires javafx.web;

    opens zenit to javafx.fxml;
    exports zenit;
}