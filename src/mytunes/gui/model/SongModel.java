/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.gui.model;

import java.io.File;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import mytunes.be.Song;
import mytunes.bll.MusicPlayer;

public class SongModel {

    private static SongModel instance;
    private final MusicPlayer musicPlayer = MusicPlayer.getInstance();

    private final ObservableList<Song> songs;

    public static SongModel getInstance() {
        if (instance == null) {
            instance = new SongModel();
        }
        return instance;
    }

    public SongModel() {
        songs = FXCollections.observableArrayList();
        mockupSongs();
    }

    /**
     * Adds the parsed song to the ObservableList of songs
     *
     * @param songToSave
     */
    public void saveSong(Song songToSave) {
        songs.add(songToSave);
        //TODO ALH: FIX THIS SHIT!
        //deleteMockSong();
    }

    /**
     * Looks for empty mock song
     */
    private void deleteMockSong() {
        for (Song song : songs) {
            if (song.getTitle().equals("")) {
                songs.remove(song);
            }
        }
    }

    /**
     * Deletes the selected song
     *
     * @param selectedSong
     */
    public void deleteSong(Song selectedSong) {
        for (Song song : songs) {
            if (song.getId() == (selectedSong.getId())) {
                songs.remove(song);
                break;
            }
        }
    }

    /**
     *
     * @return songs
     */
    public ObservableList<Song> getSongs() {
        return songs;
    }

    /**
     * Creates some mockup songs
     */
    private void mockupSongs() {
        Song song1 = new Song("Song One", "SomeArtist", "Classical", "7.52", "");
        Song song2 = new Song("Song Two", "SomeArtist", "Rock", "5.52", "");
        Song song3 = new Song("Song Three", "SomeArtist", "POP", "3.52", "");
        Song song4 = new Song("Song Four", "SomeArtist", "Funk", "2.52", "");
        Song baby = new Song(
                "Baby",
                "Justin Bieber",
                "POP",
                "2.5",
                "D:/Programmering/Java/Netbeans/MyTunes/src/mytunes/assets/mp3/baby.mp3");

        songs.add(song1);
        songs.add(song2);
        songs.add(song3);
        songs.add(song4);
        songs.add(baby);
    }

    /**
     * Parse the selected song to the MusicPlayer to play it
     *
     * @param selectedSong
     */
    public void playSelectedSong(Song selectedSong) {
        Media songToPlay = new Media(new File(selectedSong.getPath()).toURI().toString());
        System.out.println(songToPlay.getSource());
        musicPlayer.play(songToPlay);
    }

    /**
     * Stops the myTunesPlayer
     */
    public void stopMediaPlayer() {
        musicPlayer.stop();
    }
}
