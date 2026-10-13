
package com.animeflix.sa;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/anime")
public class AnimeController {

    private final AnimeRepository animeRepository;

    public AnimeController(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    // Get all anime
    @GetMapping
    public List<Anime> getAllAnime() {
        return animeRepository.findAll();
    }

    // Get one anime by ID
    @GetMapping("/{id}")
    public Anime getAnimeById(@PathVariable Long id) {
        return animeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Anime not found"));
    }

    // Search by title
    @GetMapping("/search")
    public List<Anime> searchAnime(@RequestParam String title) {
        return animeRepository.findByTitleContainingIgnoreCase(title);
    }

    // Filter by genre
    @GetMapping("/genre")
    public List<Anime> getAnimeByGenre(@RequestParam String name) {
        return animeRepository.findByGenreIgnoreCase(name);
    }

    // Add new anime
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Anime addAnime(@RequestBody Anime anime) {
        if (anime.getTitle() == null ||
                anime.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Anime title is required");
        }

        anime.setTitle(anime.getTitle().trim());

        // Ensure PostgreSQL generates a new ID.
        return animeRepository.save(anime);
    }

    // Update existing anime
    @PutMapping("/{id}")
    public Anime updateAnime(
            @PathVariable Long id,
            @RequestBody Anime updatedAnime) {

        Anime existingAnime = animeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Anime not found"));

        if (updatedAnime.getTitle() == null ||
                updatedAnime.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Anime title is required");
        }

        existingAnime.setTitle(updatedAnime.getTitle().trim());
        existingAnime.setGenre(updatedAnime.getGenre());
        existingAnime.setReleaseYear(updatedAnime.getReleaseYear());
        existingAnime.setRating(updatedAnime.getRating());
        existingAnime.setDescription(updatedAnime.getDescription());
        existingAnime.setPosterUrl(updatedAnime.getPosterUrl());
        existingAnime.setVideoUrl(updatedAnime.getVideoUrl());

        return animeRepository.save(existingAnime);
    }

    // Delete anime
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnime(@PathVariable Long id) {
        if (!animeRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Anime not found");
        }

        animeRepository.deleteById(id);
    }
}
