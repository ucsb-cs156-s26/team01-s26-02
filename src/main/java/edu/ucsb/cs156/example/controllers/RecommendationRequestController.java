package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBDates */
@Tag(name = "RecommendationRequest")
@RequestMapping("/api/RecommendationRequest")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

  @Autowired RecommendationRequestRepository recommendationRequestsRepository;

  /**
   * List all recommendation requests
   *
   * @return an iterable of RecommendationRequest
   */
  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allRecommendationRequests() {
    Iterable<RecommendationRequest> requests = recommendationRequestsRepository.findAll();
    return requests;
  }

  /**
   * Get a single recommendation request by id
   *
   * @param id the id of the date
   * @return a RecommendationRequest
   */
  @Operation(summary = "Get a single recommendation request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public RecommendationRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    RecommendationRequest recReq =
        recommendationRequestsRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    return recReq;
  }

  /**
   * Create a new recommendation request
   *
   * @param requesterEmail
   * @param professorEmail
   * @param explanation
   * @param dateRequested
   * @param dateNeeded
   * @param done
   * @return the saved recommencation request
   */
  @Operation(summary = "Create a new recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "professorEmail") @RequestParam String professorEmail,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "done") @RequestParam Boolean done,
      @Parameter(
              name = "dateRequested",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateRequested")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateRequested,
      @Parameter(
              name = "dateNeeded",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateNeeded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateNeeded)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("explanation={}", explanation);

    RecommendationRequest recommendationRequest = new RecommendationRequest();
    recommendationRequest.setRequesterEmail(requesterEmail);
    recommendationRequest.setProfessorEmail(professorEmail);
    recommendationRequest.setExplanation(explanation);
    recommendationRequest.setDateRequested(dateRequested);
    recommendationRequest.setDateNeeded(dateNeeded);
    recommendationRequest.setDone(done);

    RecommendationRequest savedRecommendationRequest =
        recommendationRequestsRepository.save(recommendationRequest);

    return savedRecommendationRequest;
  }

  /**
   * Delete a Recommendation Request
   *
   * @param id the id of the recommendation request to delete
   * @return a message indicating the recommendation request was deleted
   */
  @Operation(summary = "Delete a RecommendationRequest")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteRecommendationRequest(@Parameter(name = "id") @RequestParam Long id) {
    RecommendationRequest recommendationRequest =
        recommendationRequestsRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    recommendationRequestsRepository.delete(recommendationRequest);
    return genericMessage("RecommendationRequest with id %s deleted".formatted(id));
  }

  /**
   * Update a single recommendation request
   *
   * @param id id of the recommendation request to update
   * @param incoming the new recommendation request
   * @return the updated recommendation request object
   */
  @Operation(summary = "Update a single recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public RecommendationRequest updateRecommendationRequest(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid RecommendationRequest incoming) {

    RecommendationRequest recReq =
        recommendationRequestsRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    recReq.setRequesterEmail(incoming.getRequesterEmail());
    recReq.setProfessorEmail(incoming.getProfessorEmail());
    recReq.setExplanation(incoming.getExplanation());
    recReq.setDateRequested(incoming.getDateRequested());
    recReq.setDateNeeded(incoming.getDateNeeded());
    recReq.setDone(incoming.getDone());

    recommendationRequestsRepository.save(recReq);

    return recReq;
  }
}
