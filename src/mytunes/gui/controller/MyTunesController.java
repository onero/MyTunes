/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mytunes.be.Playlist;
import mytunes.be.Song;
import mytunes.bll.MathManager;
import mytunes.bll.MusicPlayer;
import mytunes.gui.model.SongModel;

/**
 *
 * @author gta1
 */
public class MyTunesController implements Initializable {

    @FXML
    private TableView<Playlist> tablePlaylists;
    @FXML
    private TableView<Song> tableSongs;
    @FXML
    private Label lblIsPlaying;
    @FXML
    private TextField txtSearch;
    @FXML
    private Slider sliderVolume;
    @FXML
    private TableColumn<Playlist, String> clmPlaylistName;
    @FXML
    private TableColumn<Playlist, String> clmPlaylistSongsAmount;
    @FXML
    private TableColumn<Playlist, String> clmPlaylistTotalDuration;
    @FXML
    private TableColumn<Song, String> clmSongTitle;
    @FXML
    private TableColumn<Song, String> clmSongArtist;
    @FXML
    private TableColumn<Song, String> clmSongDuration;
    @FXML
    private TableView<Song> tableCurrentPlaylist;
    @FXML
    private TableColumn<Song, String> clmCurrentPlaylistTrack;
    @FXML
    private TableColumn<Song, String> clmCurrentPlaylistTitle;
    @FXML
    private TableColumn<Song, String> clmSongGenre;
    @FXML
    private ImageView btnPlay;
    @FXML
    private ImageView speaker;
    @FXML
    private Label lblTotalSongs;
    @FXML
    private Label lblTotalDuration;
    @FXML
    public Slider sliderMusic;
    @FXML
    public Label lblTime;

    private final Image play = new Image(getClass().getResourceAsStream("/mytunes/assets/icons/play.png"));
    private final Image pause = new Image(getClass().getResourceAsStream("/mytunes/assets/icons/pause.png"));
    private final Image normal = new Image(getClass().getResourceAsStream("/mytunes/assets/icons/speaker.png"));
    private final Image mute = new Image(getClass().getResourceAsStream("/mytunes/assets/icons/muted.png"));

    private Song selectedSong;

    private Stage primStage;

    private double lastVolume;

    private SongModel songModel;
    private MathManager mathManager;

    private static final String IDLE_TEXT = "Enjoy your music!";
    private static final String IS_PLAYING = " is playing";
    private static final String IS_PAUSED = " is paused";

    private TableView.TableViewSelectionModel<Song> selectedView;
    private TableView.TableViewSelectionModel<Song> playingView;

    private MusicPlayer player = MusicPlayer.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        songModel = SongModel.getInstance();
        songModel.setMyTunesController(this);
        mathManager = MathManager.getInstance();

        btnPlay.setImage(play);

        speaker.setImage(normal);

        initializeTables();

        //Set a heartily welcome message
        lblIsPlaying.setText(IDLE_TEXT);

        selectedView = tableSongs.getSelectionModel(); //Setting the default view to tableSongs.
        playingView = tableSongs.getSelectionModel(); //Setting the default playingView.

        //Add listeners to tables
        addChangeListenersToTables();

    }

    /**
     * Assigns changelisteners to see where the user clicks
     */
    private void addChangeListenersToTables() {
        //Adds a listener to tableSongs.
        tableSongs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Song>() {
            @Override
            public void changed(ObservableValue<? extends Song> observable, Song oldValue, Song newValue) {
                if (newValue != null) //If newValue (new selection in the model) is not null.
                {
                    //Clears the tableCurrentPlaylist selection.
                    tableCurrentPlaylist.getSelectionModel().clearSelection();
                    //Updates the selectedView to the new selected view.
                    selectedView = tableSongs.getSelectionModel();
                }
            }
        });
        //Same as above, just opposite.
        tableCurrentPlaylist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Song>() {
            @Override
            public void changed(ObservableValue<? extends Song> observable, Song oldValue, Song newValue) {
                if (newValue != null) {
                    tableSongs.getSelectionModel().clearSelection();
                    selectedView = tableCurrentPlaylist.getSelectionModel();
                }
            }
        });
    }

    /**
     * Assigns information to the tables
     */
    private void initializeTables() {
        //Add songs from the model and show them in the tableSongs
        clmSongTitle.setCellValueFactory(i -> i.getValue().getTitle());
        clmSongArtist.setCellValueFactory(i -> i.getValue().getArtist());
        clmSongGenre.setCellValueFactory(i -> i.getValue().getGenre());
        clmSongDuration.setCellValueFactory(i -> i.getValue().getDuration());
        songModel.loadSavedSongs();
        tableSongs.setItems(songModel.getSongs());
        updateTotals();

        //Add song to current playlist
        clmCurrentPlaylistTrack.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(tableCurrentPlaylist.getItems().indexOf(column.getValue())).asString());
        clmCurrentPlaylistTitle.setCellValueFactory(i -> i.getValue().getTitle());
        tableCurrentPlaylist.setItems(songModel.getCurrentPlaylist());

        //add playlists from the model and show them in the tablePlaylists
        clmPlaylistName.setCellValueFactory(i -> i.getValue().getName());
        clmPlaylistSongsAmount.setCellValueFactory(i -> i.getValue().getSongs());
        clmPlaylistTotalDuration.setCellValueFactory(i -> i.getValue().getDuration());
//        songModel.loadSavedPlaylists();
        tablePlaylists.setItems(songModel.getPlaylists());
    }

    /**
     * Checks if the "add image" or the "edit image" was clicked and opens the
     * corresponding view
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleSongTableButton(MouseEvent event) throws IOException {
        //Assign the clicked image to a local variable
        ImageView selectedImage = (ImageView) event.getSource();

        //Grab hold of the curret stage
        primStage = (Stage) txtSearch.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mytunes/gui/view/NewEditSongView.fxml"));
        Parent root = loader.load();

        Stage editStage = new Stage();
        editStage.setScene(new Scene(root));

        //Create new modal window from the FXMLLoader
        editStage.initModality(Modality.WINDOW_MODAL);
        editStage.initOwner(primStage);

        //If user wants to add a new song just load the empty modal
        if (selectedImage.getId().equals("add")) {
            editStage.show();
            //If user wants to edit a song load up modal with info
        } else if (selectedImage.getId().equals("editSong")) {
            Song songToEdit = tableSongs.getSelectionModel().getSelectedItem();
            if (songToEdit != null) {
                assignSongValuesToModal(loader, songToEdit);
                editStage.show();
            }
        }
    }

    /**
     * Fills up the modal with information from the song
     *
     * @param loader
     * @param songToEdit
     */
    private void assignSongValuesToModal(FXMLLoader loader, Song songToEdit) {
        NewEditSongController songController = loader.getController();
        songController.setTxtTitle(songToEdit.getTitle().get());
        songController.setTxtArtist(songToEdit.getArtist().get());
        songController.setTxtDuration(songToEdit.getDuration().get());
        songController.setComboGenre(songToEdit.getGenre().get());
        songController.setCurrentSong(songToEdit);
    }

    private void assignPlaylistValuesToModal(FXMLLoader loader, Playlist playlistToEdit) {
        NewEditPlaylistController playlistController = loader.getController();
        playlistController.setTxtName(playlistToEdit.getName().get());
        playlistController.setCurrentPlaylist(playlistToEdit);
    }

    /**
     * Gets the current selected song and calls the play method of the
     * MusicPlaer. Changes the text of the play button appropriately.
     *
     * @param event
     */
    @FXML
    private void handlePlayButton() {
        selectedSong = selectedView.getSelectedItem();
        //If a song is selected and we're not currently playing anything fire up the selected song and change play button to pause
        if (btnPlay.getImage() == play) {
            if (selectedSong != null) {
                playSong(selectedSong);
            }
            //If user clicks the pause button we pause the MusicPlayer
        } else {
            songModel.pausePlaying();
            btnPlay.setImage(play);
            lblIsPlaying.setText(songModel.getCurrentSongPlaying().getTitle().get() + IS_PAUSED);
        }

        songModel.updateSliderTime();
    }

    /**
     * Play a song on a double click
     */
    @FXML

    private void handleDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            songModel.stopPlaying();

            selectedSong = selectedView.getSelectedItem();
            playSong(selectedSong);
        }
    }

    /**
     * Asks the model to play the selected song
     *
     * @param selectedSong
     */
    private void playSong(Song selectedSong) {
        songModel.playSelectedSong(selectedSong);
        playingView = selectedView;
        btnPlay.setImage(pause);
        lblIsPlaying.setText(selectedSong.getTitle().get() + IS_PLAYING);
    }

    /**
     * Stop the song playing and sets the text of the play button to "Play".
     *
     * @param event
     */
    @FXML
    private void handleStopButton() {
        songModel.stopPlaying();
        btnPlay.setImage(play);
        lblIsPlaying.setText(IDLE_TEXT);
    }

    /**
     * Searches for songs containing the query.
     */
    @FXML
    private void handleSearch() {
        String search = txtSearch.getText();
        if (!search.equals("")) {
            songModel.searchSong(search);
        }
    }

    /**
     * Clears the query and shows all songs.
     */
    @FXML
    private void handleClearSearch() {
        songModel.clearSearch();
        txtSearch.setText("");
    }

    /**
     * Selects the next song in the playing view and plays it. Updates the
     * label.
     */
    public void playNextSong() {
        Song nextSong = selectNextSong(true); //sends true value to skip forward
        playSong(nextSong);
    }

    /**
     * Select the next song from current view
     *
     * @return
     */
    private Song selectNextSong(boolean goForward) {
        songModel.stopPlaying();
        Song currentSong = songModel.getCurrentSongPlaying();
        playingView.select(currentSong);
        if (goForward) {
            playingView.selectNext();
        } else {
            playingView.selectPrevious();
        }
        Song nextSong = selectedView.getSelectedItem();
        return nextSong;
    }

    /**
     * Finds which view the current played song is from. Then plays the next
     * song in that view.
     */
    @FXML
    private void handleSkipForwardButton() {
        Song nextSong = selectNextSong(true); //sends true value to skip forward
        playSong(nextSong);
    }

    /**
     * Finds which view the current played song is from. Then plays the previous
     * song in that view.
     */
    @FXML
    private void handleSkipBackwardButton() {
        Song nextSong = selectNextSong(false); //sends false value to go to previous
        playSong(nextSong);
    }

    /**
     * Gets the selected song and prompts user to delete
     *
     * @param event
     */
    @FXML
    private void handleSongDeleteButton(MouseEvent event) {
        try {

            Song songToDelete = tableSongs.getSelectionModel().getSelectedItem();

            //Show popup window and await user confirmation. If user clicks "OK" then we remove the song
            Alert alert = songRemoveDialog(songToDelete);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                //songModel.deleteSongs(songsToDelete);
                updateTotals();
                songModel.deleteSong(songToDelete);
            }
        } catch (NullPointerException npe) {
            System.out.println("Wrong delete button");
        }

    }

    /**
     * Replays the current song
     */
    @FXML
    private void handleReplay() {
        songModel.replaySong();
    }

    /**
     * Adds or a song to the current playlist
     *
     * @param event
     */
    @FXML
    private void handleSongToPlaylist(MouseEvent event) {
        Song songToAdd = tableSongs.getSelectionModel().getSelectedItem();
        songModel.addSongToPlaylist(songToAdd);
    }

    /**
     * Prompts user to remove song from current playlist
     *
     * @param event
     */
    @FXML
    private void handleRemoveSongFromPlaylistButton(MouseEvent event) {
        try {
            Song songToRemoveFromPlaylist = tableCurrentPlaylist.getSelectionModel().getSelectedItem();

            //Show popup window and await user confirmation. If user clicks "OK" then we remove the song
            Alert alert = songRemoveDialog(songToRemoveFromPlaylist);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                songModel.getCurrentPlaylist().remove(songToRemoveFromPlaylist);
            }
        } catch (NullPointerException npe) {
            System.out.println("Wrong delete buttom");
        }
    }

    /**
     * Opens a dialog for remove confirmation
     *
     * @param songToRemove
     * @return
     */
    private Alert songRemoveDialog(Song songToRemove) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Are you sure you want to remove the song: " + "\n\n" + songToRemove.getTitle().get());
        alert.setContentText("Press 'OK' to remove.");
        return alert;
    }

    /**
     * Opens a dialog for remove confirmation
     *
     * @param playlistToRemove
     * @return
     */
    private Alert playlistRemoveDialog(Playlist playlistToRemove) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Are you sure you want to remove the playlist: " + "\n\n" + playlistToRemove.getName().get());
        alert.setContentText("Press 'OK' to remove.");
        return alert;
    }

    /**
     * Add or edit a playlist depending on user choice
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void handlePlaylistTableButton(MouseEvent event) throws IOException {
        //Assign the clicked image to a local variable
        ImageView selectedImage = (ImageView) event.getSource();

        //Grab hold of the curret stage
        primStage = (Stage) txtSearch.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mytunes/gui/view/NewEditPlaylistView.fxml"));
        Parent root = loader.load();

        Stage editStage = new Stage();
        editStage.setScene(new Scene(root));

        //Create new modal window from the FXMLLoader
        editStage.initModality(Modality.WINDOW_MODAL);
        editStage.initOwner(primStage);

        //If user wants to add a new playlist just load the empty modal
        if (selectedImage.getId().equals("createPlaylist")) {
            editStage.show();
            //If user wants to edit a song load up modal with info
        } else if (selectedImage.getId().equals("editPlaylist")) {
            Playlist playlistToEdit = tablePlaylists.getSelectionModel().getSelectedItem();
            if (playlistToEdit != null) {
                assignPlaylistValuesToModal(loader, playlistToEdit);
                editStage.show();
            }
        }
    }

    /**
     * Deletes playlist
     *
     * @param event
     */
    @FXML
    private void handleRemovePlaylist(MouseEvent event) {
        Playlist playlistToDelete = tablePlaylists.getSelectionModel().getSelectedItem();

        Alert alert = playlistRemoveDialog(playlistToDelete);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            songModel.deletePlaylist(playlistToDelete);
        }
    }

    /**
     * Moves the selected song one up when clicked.
     *
     * @param event
     */
    @FXML
    private void handleMoveSongUpButton(MouseEvent event) {
        int selectedIndex = tableCurrentPlaylist.getSelectionModel().getSelectedIndex();
        ArrayList<Song> currentPlaylist = songModel.getCurrentPlaylistAsArrayList();
        if (selectedIndex - 1 >= 0) {
            Collections.swap(currentPlaylist, selectedIndex, selectedIndex - 1);
            songModel.updateCurrentPlaylist(currentPlaylist);
            tableCurrentPlaylist.getSelectionModel().select(selectedIndex - 1);
        }
    }

    /**
     * Moves the selected song one down when clicked.
     *
     * @param event
     */
    @FXML
    private void handleMoveSongDownButton(MouseEvent event) {
        int selectedIndex = tableCurrentPlaylist.getSelectionModel().getSelectedIndex();
        ArrayList<Song> currentPlaylist = songModel.getCurrentPlaylistAsArrayList();
        if (selectedIndex + 1 < currentPlaylist.size() && selectedView == tableCurrentPlaylist.getSelectionModel()) {
            Collections.swap(currentPlaylist, selectedIndex, selectedIndex + 1);
            songModel.updateCurrentPlaylist(currentPlaylist);
            tableCurrentPlaylist.getSelectionModel().select(selectedIndex + 1);
        }
    }

    /**
     * Handle music volume
     */
    @FXML
    private void handleMusicVolume() {
        sliderVolume.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (sliderVolume.isValueChanging()) {
                    songModel.switchVolume(sliderVolume.getValue() / 100.0);
                    if (speaker.getImage().equals(mute))
                    {
                        speaker.setImage(normal);
                    }
                } else if (sliderVolume.isPressed()) {
                    songModel.switchVolume(sliderVolume.getValue() / 100.0);
                    if (speaker.getImage().equals(mute))
                    {
                        speaker.setImage(normal);
                    }
                }
            }

        });
    }

    /**
     * Mute or unmutes the music player
     *
     * @param event
     */
    @FXML
    private void handleMute(MouseEvent event) {
        if (speaker.getImage().equals(normal)) {
            lastVolume = sliderVolume.getValue();
            songModel.switchVolume(0);
            sliderVolume.setValue(0);
            speaker.setImage(mute);
        } else {
            songModel.switchVolume(lastVolume);
            sliderVolume.setValue(lastVolume);
            speaker.setImage(normal);
        }
    }

    /**
     * Shuffles the current playlist
     *
     * @param event
     */
    @FXML
    private void handleShuffle(MouseEvent event) {
        songModel.shuffleCurrentPlaylist();
    }

    /**
     * When a playlist is selected, show it's song in the currentPlaylist view.
     *
     * @param event
     */
    @FXML
    private void handleSelectPlaylist(MouseEvent event) throws NullPointerException {
        try {
            int playlistId = tablePlaylists.getSelectionModel().getSelectedItem().getId();
            songModel.setPlaylistID(playlistId);
            ArrayList<Song> list = tablePlaylists.getSelectionModel().getSelectedItem().getSongsInPlaylist();
            songModel.updateCurrentPlaylist(list);
        } catch (Exception e) {
            System.out.println("Selection error " + e);
        }
    }

    /**
     * Select more than one song
     */
    @FXML
    private void handleMultiSelect(KeyEvent event) {
        if (event.isControlDown() | event.isShiftDown()) {
            selectedView.setSelectionMode(SelectionMode.MULTIPLE);
            tablePlaylists.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } else {
            selectedView.setSelectionMode(SelectionMode.SINGLE);
            tablePlaylists.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        }
    }

    /**
     * Updates the totalSong and totalDuration labels.
     */
    public void updateTotals() {
        lblTotalSongs.setText(songModel.getSongs().size() + "");
        double duration = mathManager.convertToMinutes(songModel.getTotalDurationAllSongs());
        lblTotalDuration.setText(String.format("%.2f", duration));
    }
}
