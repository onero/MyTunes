/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.bll;

import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import mytunes.be.Song;
import mytunes.gui.controller.MyTunesController;
import mytunes.gui.model.SongModel;

public class MusicPlayer {

    private static MusicPlayer instance;

    private SongModel songModel;

    private final MyTunesController mtController = MyTunesController.getInstance();

    private static MediaPlayer myTunesPlayer;

    private boolean isPlaying = false;

    private Song currentSong;

    private Media pick;

    public static MusicPlayer getInstance() {
        if (instance == null) {
            instance = new MusicPlayer();
        }
        return instance;
    }

    /**
     * Sets the songModel so the musicPlayer has a reference to the SongModel.
     *
     * @param sModel
     */
    public void setSongModel(SongModel sModel) {
        songModel = sModel;
    }

    /**
     * Plays or pauses the song parsed depending if the son is currently played
     * or not.
     *
     * @param song to be played.
     * @throws MediaException
     */
    public void playSong(Song song) throws MediaException {
        //Pick the song to be played and put it in the myTunesPlayer.
        pick = new Media(song.getFileName().get());
        myTunesPlayer = new MediaPlayer(pick);
        myTunesPlayer.play();
        isPlaying = true;
        currentSong = song;

        myTunesPlayer.setOnEndOfMedia(new Runnable() //Listens for when a song ends.
        {
            @Override
            public void run() {
                try {
                    songModel.playNextSong(); //Plays the next song.
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
    }

    /**
     * Pauses the current song
     */
    public void pausePlaying() {
        myTunesPlayer.pause();

    }

    /**
     * Resume current song
     */
    public void resumeSong() {
        myTunesPlayer.play();
    }

    /**
     * Stops the current song
     */
    public void stopPlaying() {
        myTunesPlayer.stop();
        isPlaying = false;
    }

    /**
     *
     * @return status of MyTunesPlayer playing
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     *
     * @return current song
     */
    public Song getCurrentSong() {
        return currentSong;
    }

    /**
     * Set the music volume
     *
     * @param volume
     */
    public void setVolume(double volume) {
        myTunesPlayer.setVolume(volume);
    }

    /**
     * Get musicplayer
     */
    public static MediaPlayer getPlayer() {
        return myTunesPlayer;
    }

    /**
     * Starts tracking the songs time and updates the display for it
     */
    public void trackTime() {
        myTunesPlayer.currentTimeProperty().addListener((Observable ov) -> {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration duration = MusicPlayer.getPlayer().getTotalDuration();
                    Duration currentTime = MusicPlayer.getPlayer().getCurrentTime();
                    mtController.getTimeLabel().setText(TimeManager.formatTime(currentTime, duration));
                    mtController.getMusicSlider().setDisable(duration.isUnknown());
                    if (!mtController.getMusicSlider().isDisabled() && duration.greaterThan(Duration.ZERO)) {
                        mtController.getMusicSlider().setProgress(currentTime.toSeconds() / duration.toSeconds());
                    }

                }
            });
        });
    }

    /**
     * Changes the song to the current time provided
     *
     * @param time
     */
    public void setNewTime(Double time) {
        myTunesPlayer.seek(myTunesPlayer.getTotalDuration()
                .multiply(time));
    }
}
