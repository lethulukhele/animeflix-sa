
package com.animeflix.sa;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class AnimeWatchController {

    private final AnimeRepository animeRepository;
    private final EpisodeRepository episodeRepository;

    public AnimeWatchController(
            AnimeRepository animeRepository,
            EpisodeRepository episodeRepository) {
        this.animeRepository = animeRepository;
        this.episodeRepository = episodeRepository;
    }

    @GetMapping("/watch/{id}")
    public String watchAnime(
            @PathVariable Long id,
            @RequestParam(required = false) Long episodeId,
            @RequestParam(required = false) Integer seasonNumber,
            Model model) {

        Anime anime = animeRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Anime not found"
                        ));

        List<Episode> allEpisodes =
                episodeRepository
                        .findByAnime_IdOrderBySeasonNumberAscEpisodeNumberAsc(id);

        List<Integer> seasonNumbers = allEpisodes.stream()
                .map(Episode::getSeasonNumber)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Find the requested episode without referencing a mutable variable
        // inside a lambda.
        Episode requestedEpisode = null;

        if (episodeId != null) {
            for (Episode episode : allEpisodes) {
                if (episode.getId().equals(episodeId)) {
                    requestedEpisode = episode;
                    break;
                }
            }
        }

        // If an episode is explicitly requested alongside a season,
        // ensure the season matches that episode.
        if (requestedEpisode != null
                && seasonNumber != null
                && !Objects.equals(
                        requestedEpisode.getSeasonNumber(),
                        seasonNumber)) {
            seasonNumber = requestedEpisode.getSeasonNumber();
        }

        final Integer activeSeason = seasonNumber;

        List<Episode> episodes;

        if (activeSeason == null) {
            episodes = allEpisodes;
        } else {
            episodes = allEpisodes.stream()
                    .filter(episode ->
                            Objects.equals(
                                    episode.getSeasonNumber(),
                                    activeSeason))
                    .collect(Collectors.toList());
        }

        Episode selectedEpisode = null;

        if (episodeId != null) {
            for (Episode episode : episodes) {
                if (episode.getId().equals(episodeId)) {
                    selectedEpisode = episode;
                    break;
                }
            }
        }

        if (selectedEpisode == null && !episodes.isEmpty()) {
            selectedEpisode = episodes.get(0);
        }

        Episode previousEpisode = null;
        Episode nextEpisode = null;

        if (selectedEpisode != null) {
            int currentIndex = episodes.indexOf(selectedEpisode);

            if (currentIndex > 0) {
                previousEpisode = episodes.get(currentIndex - 1);
            }

            if (currentIndex >= 0
                    && currentIndex < episodes.size() - 1) {
                nextEpisode = episodes.get(currentIndex + 1);
            }
        }

        model.addAttribute("siteName", "AnimeFlix SA");
        model.addAttribute("anime", anime);
        model.addAttribute("episodes", episodes);
        model.addAttribute("allEpisodes", allEpisodes);
        model.addAttribute("seasonNumbers", seasonNumbers);
        model.addAttribute("selectedSeasonNumber", activeSeason);
        model.addAttribute("selectedEpisode", selectedEpisode);
        model.addAttribute("previousEpisode", previousEpisode);
        model.addAttribute("nextEpisode", nextEpisode);

        return "watch";
    }
}
