package it.unibo.oop.lab.streams;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 */
public final class MusicGroupImpl implements MusicGroup {

    private final Map<String, Integer> albums = new HashMap<>();
    private final Set<Song> songs = new HashSet<>();

    @Override
    public void addAlbum(final String albumName, final int year) {
        this.albums.put(albumName, year);
    }

    @Override
    public void addSong(final String songName, final Optional<String> albumName, final double duration) {
        if (albumName.isPresent() && !this.albums.containsKey(albumName.get())) {
            throw new IllegalArgumentException("invalid album name");
        }
        this.songs.add(new MusicGroupImpl.Song(songName, albumName, duration));
    }

    @Override
    public Stream<String> orderedSongNames() {
        return this.songs.stream().sorted((s1, s2) -> s1.getSongName().compareTo(s2.getSongName())).map(Song::getSongName);
    }

    @Override
    public Stream<String> albumNames() {
        return this.albums.keySet().stream().map(String::valueOf);
    }

    @Override
    public Stream<String> albumInYear(final int year) {
        if (!this.albums.containsValue(year)) {
            throw new IllegalArgumentException(); //NOPMD: Exercises required it
        }
        return this.albums.keySet().stream().filter(a -> this.albums.get(a).equals(year)).map(String::valueOf);
    }

    @Override
    public int countSongs(final String albumName) {
        if (!this.albums.containsKey(albumName)) {
            throw new IllegalArgumentException();
        }
        return (int) this.songs.stream().filter(s -> s.getAlbumName().equals(Optional.of(albumName))).count();
    }

    @Override
    public int countSongsInNoAlbum() {
        return (int) this.songs.stream().filter(s -> s.getAlbumName().equals(Optional.empty())).count();
    }

    @Override
    public OptionalDouble averageDurationOfSongs(final String albumName) {
        if (!this.albums.containsKey(albumName)) {
            throw new IllegalArgumentException(); //NOPMD: Exercises required it
        }
        return this.songs.stream()
        .filter(s -> s.getAlbumName().equals(Optional.of(albumName)))
        .mapToDouble(Song::getDuration)
        .average();
    }

    @Override
    public Optional<String> longestSong() {
        return this.songs.stream().max((s1, s2) -> Double.compare(s1.getDuration(), s2.getDuration())).map(Song::getSongName);
    }

    @Override
    public Optional<String> longestAlbum() {
        return this.songs.stream()
        .filter(s -> s.getAlbumName().isPresent())
        .collect(groupingBy(Song::getAlbumName, summingDouble(Song::getDuration)))
        .entrySet().stream()
        .max(Comparator.comparingDouble(Entry::getValue))
        .get()
        .getKey();
    }

    private static final class Song {

        private final String songName;
        private final Optional<String> albumName;
        private final double duration;
        private int hash;

        Song(final String name, final Optional<String> album, final double len) {
            super();
            this.songName = name;
            this.albumName = album;
            this.duration = len;
        }

        public String getSongName() {
            return songName;
        }

        public Optional<String> getAlbumName() {
            return albumName;
        }

        public double getDuration() {
            return duration;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = songName.hashCode() ^ albumName.hashCode() ^ Double.hashCode(duration);
            }
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Song) {
                final Song other = (Song) obj;
                return albumName.equals(other.albumName) && songName.equals(other.songName)
                        && duration == other.duration;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Song [songName=" + songName + ", albumName=" + albumName + ", duration=" + duration + "]";
        }

    }

}
