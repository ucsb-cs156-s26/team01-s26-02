package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

  // /**
  //  * Get a single date by id
  //  *
  //  * @param id the id of the date
  //  * @return a UCSBDate
  //  */
  // @Operation(summary = "Get a single date")
  // @PreAuthorize("hasRole('ROLE_USER')")
  // @GetMapping("")
  // public UCSBDate getById(@Parameter(name = "id") @RequestParam Long id) {
  //   UCSBDate ucsbDate =
  //       ucsbDateRepository
  //           .findById(id)
  //           .orElseThrow(() -> new EntityNotFoundException(UCSBDate.class, id));

  //   return ucsbDate;
  // }

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

  // /**
  //  * Update a single date
  //  *
  //  * @param id id of the date to update
  //  * @param incoming the new date
  //  * @return the updated date object
  //  */
  // @Operation(summary = "Update a single date")
  // @PreAuthorize("hasRole('ROLE_ADMIN')")
  // @PutMapping("")
  // public UCSBDate updateUCSBDate(
  //     @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid UCSBDate incoming) {

  //   UCSBDate ucsbDate =
  //       ucsbDateRepository
  //           .findById(id)
  //           .orElseThrow(() -> new EntityNotFoundException(UCSBDate.class, id));

  //   ucsbDate.setQuarterYYYYQ(incoming.getQuarterYYYYQ());
  //   ucsbDate.setName(incoming.getName());
  //   ucsbDate.setLocalDateTime(incoming.getLocalDateTime());

  //   ucsbDateRepository.save(ucsbDate);

  //   return ucsbDate;
  // }
}
