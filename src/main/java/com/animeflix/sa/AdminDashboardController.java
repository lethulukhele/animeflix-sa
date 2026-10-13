
package com.animeflix.sa;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminDashboardController {

    private final AnimeRepository animeRepository;
    private final EpisodeRepository episodeRepository;

    public AdminDashboardController(
            AnimeRepository animeRepository,
            EpisodeRepository episodeRepository) {
        this.animeRepository = animeRepository;
        this.episodeRepository = episodeRepository;
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model) {

        // Load the current database records.
        List<Anime> allAnime = animeRepository.findAll();
        List<Episode> allEpisodes = episodeRepository.findAll();

        // Sort anime by ID, newest record first.
        // Anime currently has no createdAt field, so ID is used
        // as a practical approximation of when it was added.
        List<Anime> recentAnime = allAnime.stream()
                .sorted(
                        Comparator.comparing(
                                Anime::getId,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .limit(5)
                .collect(Collectors.toList());

        // Sort episodes by creation date, newest first.
        List<Episode> recentEpisodes = allEpisodes.stream()
                .sorted(
                        Comparator.comparing(
                                Episode::getCreatedAt,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .limit(5)
                .collect(Collectors.toList());

        // Count distinct, non-empty genres.
        long genreCount = allAnime.stream()
                .map(Anime::getGenre)
                .filter(genre -> genre != null && !genre.isBlank())
                .flatMap(genre ->
                        java.util.Arrays.stream(genre.split(",")))
                .map(String::trim)
                .filter(genre -> !genre.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .count();

        // Send statistics and recent content to the dashboard.
        model.addAttribute("totalAnime", allAnime.size());
        model.addAttribute("totalEpisodes", allEpisodes.size());
        model.addAttribute("totalGenres", genreCount);
        model.addAttribute("animeWithoutEpisodes",
                allAnime.stream()
                        .filter(anime ->
                                allEpisodes.stream().noneMatch(episode ->
                                        episode.getAnime() != null
                                                && episode.getAnime().getId()
                                                .equals(anime.getId())
                                )
                        )
                        .count());

        model.addAttribute("recentAnime", recentAnime);
        model.addAttribute("recentEpisodes", recentEpisodes);

        return "admin-dashboard";
    }
}
