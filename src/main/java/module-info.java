module pob.pob_project {
    requires javafx.controls;
    requires javafx.fxml;


    opens pob.pob_project.gui to javafx.fxml;
    exports pob.pob_project.gui;
    exports pob.pob_project.simulation;
    exports pob.pob_project.network;
}