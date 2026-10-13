
package com.animeflix.sa;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;

@Controller
public class EpisodeManagementController {

    private final EpisodeRepository episodeRepository;
    private final AnimeRepository animeRepository;

    public EpisodeManagementController(
            EpisodeRepository episodeRepository,
            AnimeRepository animeRepository) {

        this.episodeRepository = episodeRepository;
        this.animeRepository = animeRepository;
    }

    // Display the episode management page
    @GetMapping("/admin/episodes")
    public String showEpisodes(Model model) {

        preparePage(model, new Episode(), null);

        return "admin-episodes";
    }

    // Load an episode into the form for editing
    @GetMapping("/admin/episodes/edit/{id}")
    public String editEpisode(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Episode episode = episodeRepository.findById(id)
                .orElse(null);

        if (episode == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Episode not found. It may have already been deleted."
            );

            return "redirect:/admin/episodes";
        }

        Long selectedAnimeId = episode.getAnime() != null
                ? episode.getAnime().getId()
                : null;

        preparePage(model, episode, selectedAnimeId);

        return "admin-episodes";
    }

    // Add a new episode or update an existing episode
    @PostMapping("/admin/episodes/save")
    public String saveEpisode(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long animeId,
            @RequestParam(required = false) Integer seasonNumber,
            @RequestParam(required = false) Integer episodeNumber,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String videoUrl,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Load the episode being edited, or create a new one.
        Episode episode;

        if (id != null) {
            episode = episodeRepository.findById(id).orElse(null);

            if (episode == null) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "The episode you are trying to edit no longer exists."
                );

                return "redirect:/admin/episodes";
            }
        } else {
            episode = new Episode();
        }

        // Normalise text inputs.
        String cleanTitle = title == null ? "" : title.trim();
        String cleanDescription =
                description == null ? "" : description.trim();
        String cleanVideoUrl =
                videoUrl == null ? "" : videoUrl.trim();
                // Preserve the entered values if validation fails.
episode.setSeasonNumber(seasonNumber);
episode.setEpisodeNumber(episodeNumber);
episode.setTitle(cleanTitle);
episode.setDescription(
        cleanDescription.isBlank() ? null : cleanDescription
);
episode.setVideoUrl(
        cleanVideoUrl.isBlank() ? null : cleanVideoUrl
);

if (animeId != null) {
    Anime selectedAnime = animeRepository.findById(animeId)
            .orElse(null);

    if (selectedAnime != null) {
        episode.setAnime(selectedAnime);
    }
}

        // Find the selected anime.
        Anime anime = animeId == null
                ? null
                : animeRepository.findById(animeId).orElse(null);

        // Validation: anime selection.
        if (anime == null) {
            return showValidationError(
                    model, episode, animeId,
                    "Please select an existing anime."
            );
        }

        // Validation: season number.
        if (seasonNumber == null
                || seasonNumber < 1
                || seasonNumber > 999) {

            return showValidationError(
                    model, episode, animeId,
                    "Season number must be between 1 and 999."
            );
        }

        // Validation: episode number.
        if (episodeNumber == null
                || episodeNumber < 1
                || episodeNumber > 99999) {

            return showValidationError(
                    model, episode, animeId,
                    "Episode number must be between 1 and 99999."
            );
        }

        // Validation: episode title.
        if (cleanTitle.isBlank()) {
            return showValidationError(
                    model, episode, animeId,
                    "Please enter an episode title."
            );
        }

        if (cleanTitle.length() > 200) {
            return showValidationError(
                    model, episode, animeId,
                    "The episode title cannot exceed 200 characters."
            );
        }

        // Validation: description.
        if (cleanDescription.length() > 10000) {
            return showValidationError(
                    model, episode, animeId,
                    "The description cannot exceed 10000 characters."
            );
        }

        // Validation: video URL.
        if (cleanVideoUrl.length() > 1000) {
            return showValidationError(
                    model, episode, animeId,
                    "The video URL cannot exceed 1000 characters."
            );
        }

        // Accept local paths such as /videos/demo.mp4,
        // or absolute HTTP/HTTPS URLs.
        if (!cleanVideoUrl.isBlank()
                && !cleanVideoUrl.startsWith("/")
                && !cleanVideoUrl.matches("(?i)^https?://.+")) {

            return showValidationError(
                    model, episode, animeId,
                    "Enter a local video path beginning with /, "
                            + "or a valid HTTP/HTTPS URL."
            );
        }

        // Prevent duplicate season/episode numbers for the same anime.
        List<Episode> existingEpisodes =
                episodeRepository
                        .findByAnime_IdOrderBySeasonNumberAscEpisodeNumberAsc(
                                animeId
                        );

        boolean duplicate = existingEpisodes.stream()
                .anyMatch(existing ->
                        Objects.equals(
                                existing.getSeasonNumber(),
                                seasonNumber
                        )
                        && Objects.equals(
                                existing.getEpisodeNumber(),
                                episodeNumber
                        )
                        && !Objects.equals(
                                existing.getId(),
                                episode.getId()
                        )
                );

        if (duplicate) {
            return showValidationError(
                    model, episode, animeId,
                    "This anime already has an episode with that "
                            + "season and episode number."
            );
        }

        // Set the validated episode details.
        episode.setAnime(anime);
        episode.setSeasonNumber(seasonNumber);
        episode.setEpisodeNumber(episodeNumber);
        episode.setTitle(cleanTitle);
        episode.setDescription(
                cleanDescription.isBlank() ? null : cleanDescription
        );
        episode.setVideoUrl(
                cleanVideoUrl.isBlank() ? null : cleanVideoUrl
        );

        boolean isNewEpisode = episode.getId() == null;

        episodeRepository.save(episode);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                isNewEpisode
                        ? "Episode added successfully!"
                        : "Episode updated successfully!"
        );

        return "redirect:/admin/episodes";
    }

    // Delete an episode
    @PostMapping("/admin/episodes/delete/{id}")
    public String deleteEpisode(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Episode episode = episodeRepository.findById(id)
                .orElse(null);

        if (episode == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Episode not found. It may have already been deleted."
            );

            return "redirect:/admin/episodes";
        }

        episodeRepository.delete(episode);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Episode deleted successfully."
        );

        return "redirect:/admin/episodes";
    }

    // Return the management page with the validation message and form data.
    private String showValidationError(
            Model model,
            Episode episode,
            Long selectedAnimeId,
            String message) {

        preparePage(model, episode, selectedAnimeId);

        model.addAttribute("errorMessage", message);

        return "admin-episodes";
    }

    // Shared page setup
    private void preparePage(
            Model model,
            Episode episode,
            Long selectedAnimeId) {

        model.addAttribute(
                "episodes",
                episodeRepository
                        .findAllByOrderByAnime_TitleAscSeasonNumberAscEpisodeNumberAsc()
        );

        model.addAttribute(
                "animeList",
                animeRepository.findAll()
        );

        model.addAttribute("episode", episode);
        model.addAttribute("selectedAnimeId", selectedAnimeId);
        model.addAttribute("isEditing", episode.getId() != null);
    }
}
