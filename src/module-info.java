module zenit {


    requires org.antlr.antlr4.runtime;
    requires com.kodedu.terminalfx;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires reactfx;
    requires richtextfx;
    requires wellbehavedfx;
    requires javafx.controls;
    requires javafx.web;
    requires antlr4;

    opens main.java.zenit to javafx.fxml;
    exports main.java.zenit;
}