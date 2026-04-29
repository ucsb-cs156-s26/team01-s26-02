package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBDates */
@Tag(name = "MenuItemReview")
@RequestMapping("/api/menuitemreview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

  @Autowired MenuItemReviewRepository menuItemReviewRepository;

  /**
   * List all MenuItemReviews
   *
   * @return an iterable of UCSBDate
   */
  @Operation(summary = "List all Menu Item Reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allMenuItemReviews() {
    Iterable<MenuItemReview> menuitemreviews = menuItemReviewRepository.findAll();
    return menuitemreviews;
  }

  /**
   * Create a menuitemreview
   *
   * @param itemId
   * @param reviewerEmail
   * @param stars
   * @param dateReviewed
   * @param comments
   * @return the saved menuitemreview
   */
  @Operation(summary = "Create a new menuitemreview")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postMenuItemReview(
      @Parameter(name = "itemId") @RequestParam long itemId,
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(name = "dateReviewed") @RequestParam LocalDateTime dateReviewed,
      @Parameter(name = "comments") @RequestParam String comments)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("MenuItemReviewTime={}", dateReviewed);
    MenuItemReview menuItemReview = new MenuItemReview();
    menuItemReview.setItemId(itemId);
    menuItemReview.setReviewerEmail(reviewerEmail);
    menuItemReview.setStars(stars);
    menuItemReview.setDateReviewed(dateReviewed);
    menuItemReview.setComments(comments);

    MenuItemReview savedMenuItemReview = menuItemReviewRepository.save(menuItemReview);

    return savedMenuItemReview;
  }

  /**
   * Get a single date by menuitemreview
   *
   * @param id the id of the menuitemreview
   * @return a menuitemreview
   */
  @Operation(summary = "Get a single menuitemreview")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public MenuItemReview getById(@Parameter(name = "id") @RequestParam Long id) {
    MenuItemReview menuItemReview =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    return menuItemReview;
  }

  // /**
  //  * Delete a UCSBDate
  //  *
  //  * @param id the id of the date to delete
  //  * @return a message indicating the date was deleted
  //  */
  // @Operation(summary = "Delete a UCSBDate")
  // @PreAuthorize("hasRole('ROLE_ADMIN')")
  // @DeleteMapping("")
  // public Object deleteUCSBDate(@Parameter(name = "id") @RequestParam Long id) {
  //   UCSBDate ucsbDate =
  //       ucsbDateRepository
  //           .findById(id)
  //           .orElseThrow(() -> new EntityNotFoundException(UCSBDate.class, id));

  //   ucsbDateRepository.delete(ucsbDate);
  //   return genericMessage("UCSBDate with id %s deleted".formatted(id));
  // }

  /**
   * Update a single MenuItemReview
   *
   * @param id id of the MenuItemReview to update
   * @param incoming the new MenuItemReview
   * @return the updated MenuItemReview object
   */
  @Operation(summary = "Update a single MenuItemReview")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public MenuItemReview updateMenuItemReview(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid MenuItemReview incoming) {

    MenuItemReview menuItemReview =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    menuItemReview.setItemId(incoming.getItemId());
    menuItemReview.setReviewerEmail(incoming.getReviewerEmail());
    menuItemReview.setStars(incoming.getStars());
    menuItemReview.setDateReviewed(incoming.getDateReviewed());
    menuItemReview.setComments(incoming.getComments());
    menuItemReviewRepository.save(menuItemReview);

    return menuItemReview;
  }
}
