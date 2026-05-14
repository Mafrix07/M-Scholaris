package com.scholaris.controller;

import com.scholaris.dao.ClasseDAO;
import com.scholaris.dao.MatiereDAO;
import com.scholaris.model.Classe;
import com.scholaris.model.Matiere;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.Optional;

public class ParametresController extends BaseController {

    @FXML private TableView<Classe> classeTable;
    @FXML private TableColumn<Classe, String> colClasseNom, colClasseNiveau, colClasseAnnee;
    @FXML private TableColumn<Classe, Void> colClasseActions;

    @FXML private TableView<Matiere> matiereTable;
    @FXML private TableColumn<Matiere, String> colMatiereCode, colMatiereNom, colMatiereCoef;
    @FXML private TableColumn<Matiere, Void> colMatiereActions;

    private ClasseDAO classeDAO;
    private MatiereDAO matiereDAO;

    @FXML
    public void initialize() {
        try {
            this.classeDAO = new ClasseDAO();
            this.matiereDAO = new MatiereDAO();
            
            setupTables();
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTables() {
        // Classes
        colClasseNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colClasseNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colClasseAnnee.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getAnneeScolaire())));
        
        colClasseActions.setCellFactory(tc -> new TableCell<>() {
            private final Button btnDelete = new Button("Supprimer");
            {
                btnDelete.getStyleClass().add("btn-text-red");
                btnDelete.setOnAction(e -> handleSupprimerClasse(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });

        // Matières
        colMatiereCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colMatiereNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colMatiereCoef.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCoefficient())));

        colMatiereActions.setCellFactory(tc -> new TableCell<>() {
            private final Button btnDelete = new Button("Supprimer");
            {
                btnDelete.getStyleClass().add("btn-text-red");
                btnDelete.setOnAction(e -> handleSupprimerMatiere(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });
    }

    private void loadData() throws SQLException {
        classeTable.setItems(FXCollections.observableArrayList(classeDAO.trouverTous()));
        matiereTable.setItems(FXCollections.observableArrayList(matiereDAO.trouverTous()));
    }

    @FXML
    private void handleAjouterClasse() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nouvelle Classe");
        dialog.setHeaderText("Entrez le nom de la nouvelle classe");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            try {
                Classe c = new Classe();
                c.setNom(name);
                c.setNiveau("6ème");
                c.setAnneeScolaire(java.time.LocalDate.now().getYear());
                classeDAO.ajouter(c);
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleAjouterMatiere() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nouvelle Matière");
        dialog.setHeaderText("Entrez le nom de la nouvelle matière");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            try {
                Matiere m = new Matiere();
                m.setNom(name);
                m.setCode(name.substring(0, Math.min(3, name.length())).toUpperCase());
                m.setCoefficient(2.0);
                matiereDAO.ajouter(m);
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleSupprimerClasse(Classe c) {
        try {
            classeDAO.supprimer(c.getId());
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSupprimerMatiere(Matiere m) {
        try {
            matiereDAO.supprimer(m.getId());
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
