/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.gui.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mytunes.be.Song;
import mytunes.gui.model.SongModel;

/**
 * FXML Controller class
 *
 * @author Rasmus
 */
public class NewEditSongController implements Initializable {

    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtArtist;
    @FXML
    private ComboBox<String> comboGenre;
    @FXML
    private TextField txtDuration;
    @FXML
    private TextField txtFile;

    private Stage stage;

    private SongModel songModel = SongModel.getInstance();

    private Stage primStage;

    private MyTunesController mtController;

    private static final ObservableList<String> genreList = FXCollections.observableArrayList(
            "Rock",
            "POP",
            "Jazz",
            "Opera",
            "Classical",
            "Dubstep",
            "Techno",
            "Country",
            "Hip Hop",
            "Soul",
            "Blues",
            "Reggae");

    private Song currentSong = new Song("", "", "", "");

    public static ObservableList<String> getGenreList() {
        return genreList;
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        songModel = SongModel.getInstance();
        comboGenre.setItems(genreList);
        comboGenre.setVisibleRowCount(6);
        comboGenre.getSelectionModel().selectFirst();
        mtController = MyTunesController.getInstance();
    }

    /**
     * Open modal to add more genres
     *
     * @throws IOException
     */
    @FXML
    private void handleMoreButton() throws IOException {
        stage = (Stage) txtTitle.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mytunes/gui/view/MoreGenreView.fxml"));
        Parent root = loader.load();

        Stage editStage = new Stage();
        editStage.setScene(new Scene(root));

        editStage.initModality(Modality.WINDOW_MODAL);
        editStage.initOwner(stage);

        editStage.show();

    }

    /**
     * Choose song on hdd and load information from it, if available
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @throws InterruptedException
     */
    @FXML
    private void handleChooseButton() {
        Song selectedSong = songModel.getSongFromFile();
        txtTitle.setText(selectedSong.getTitle().get());
        txtArtist.setText(selectedSong.getArtist().get());
        txtFile.setText(selectedSong.getFileName().get());
        comboGenre.getSelectionModel().select(selectedSong.getGenre().get());
        txtDuration.setText(selectedSong.getDuration().get());
    }

    /**
     * Checks if we're editing a song or adding a new one and then either edits
     * or adds
     */
    @FXML
    private void handleSaveButton() {
        if (txtTitle.getText().isEmpty()
                || txtDuration.getText().isEmpty()
                || txtFile.getText().isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Missing information");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all required information to proceed.");
            alert.showAndWait();
        } else {
            if (!currentSong.getTitle().get().equals("")) {
                setSongInfo();
                mtController.refreshTable();
            } else {
                setSongInfo();
                songModel.addSong(currentSong);
            }

            songModel.saveSongs();

            // get a handle to the stage
            stage = (Stage) txtTitle.getScene().getWindow();
            // do what you have to do
            stage.close();
        }

    }

    private void setSongInfo() {
        currentSong.setTitle(txtTitle.getText());
        currentSong.setArtist(txtArtist.getText());
        currentSong.setGenre(comboGenre.getValue());
        currentSong.setDuration(txtDuration.getText());
        currentSong.setFilePath(txtFile.getText());
    }

    public void setTxtTitle(String newString) {
        this.txtTitle.setText(newString);
    }

    public void setTxtArtist(String newString) {
        this.txtArtist.setText(newString);
    }

    public void setTxtDuration(String newString) {
        this.txtDuration.setText(newString);
    }

    public void setTxtFile(String newString) {
        this.txtFile.setText(newString);
    }

    public void setCurrentSong(Song songToEdit) {
        currentSong = songToEdit;
    }

    public void setComboGenre(String comboGenre) {
        this.comboGenre.getSelectionModel().select(comboGenre);
    }

    public void setPath(String path) {
        txtFile.setText(path);
    }

    /**
     * Cancel adding/editing song
     */
    @FXML
    private void handleCancelButton() {
        // get a handle to the stage
        stage = (Stage) txtFile.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

}
