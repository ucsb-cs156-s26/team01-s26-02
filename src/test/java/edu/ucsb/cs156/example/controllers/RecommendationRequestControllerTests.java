package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/RecommendationRequest/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    RecommendationRequest recReq1 =
        RecommendationRequest.builder()
            .requesterEmail("testRequesterEmail1")
            .professorEmail("testProfessorEmail1")
            .explanation("This is an explanation1")
            .dateRequested(LocalDateTime.parse("2022-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2023-01-03T00:00:00"))
            .done(true)
            .build();
    RecommendationRequest recReq2 =
        RecommendationRequest.builder()
            .requesterEmail("testRequesterEmail2")
            .professorEmail("testProfessorEmail2")
            .explanation("This is an explanation2")
            .dateRequested(LocalDateTime.parse("2024-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2025-01-03T00:00:00"))
            .done(true)
            .build();

    ArrayList<RecommendationRequest> expectedReqs = new ArrayList<>();
    expectedReqs.addAll(Arrays.asList(recReq1, recReq2));

    when(recommendationRequestRepository.findAll()).thenReturn(expectedReqs);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedReqs);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/RecommendationRequest").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  // // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    RecommendationRequest recReq1 =
        RecommendationRequest.builder()
            .requesterEmail("testRequesterEmail1")
            .professorEmail("testProfessorEmail1")
            .explanation("This is an explanation1")
            .dateRequested(LocalDateTime.parse("2022-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2023-01-03T00:00:00"))
            .done(true)
            .build();

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(recReq1));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(recReq1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  // Authorization tests for /api/RecommendationRequest/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/RecommendationRequest/post")
                .param("requesterEmail", "testRequesterEmail")
                .param("professorEmail", "testProfessorEmail")
                .param("explanation", "This is an explanation")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2023-01-03T00:00:00")
                .param("done", "True")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/RecommendationRequest/post")
                .param("requesterEmail", "testRequesterEmail")
                .param("professorEmail", "testProfessorEmail")
                .param("explanation", "This is an explanation")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2023-01-03T00:00:00")
                .param("done", "True")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void logged_in_admin_can_post() throws Exception {
    RecommendationRequest recReq =
        RecommendationRequest.builder()
            .requesterEmail("testRequesterEmail")
            .professorEmail("testProfessorEmail")
            .explanation("This is an explanation")
            .dateRequested(LocalDateTime.parse("2022-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2023-01-03T00:00:00"))
            .done(true)
            .build();

    when(recommendationRequestRepository.save(eq(recReq))).thenReturn(recReq);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/RecommendationRequest/post")
                    .param("requesterEmail", "testRequesterEmail")
                    .param("professorEmail", "testProfessorEmail")
                    .param("explanation", "This is an explanation")
                    .param("dateRequested", "2022-01-03T00:00:00")
                    .param("dateNeeded", "2023-01-03T00:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).save(recReq);
    String expectedJson = mapper.writeValueAsString(recReq);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_recommendationrequest() throws Exception {
    // arrange

    RecommendationRequest recReqOriginal =
        RecommendationRequest.builder()
            .requesterEmail("testRequesterEmail")
            .professorEmail("testProfessorEmail")
            .explanation("This is an explanation")
            .dateRequested(LocalDateTime.parse("2022-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2023-01-03T00:00:00"))
            .done(true)
            .build();

    RecommendationRequest recReqEdited =
        RecommendationRequest.builder()
            .requesterEmail("testRequesterEmailEdited")
            .professorEmail("testProfessorEmailEdited")
            .explanation("This is an explanationEdited")
            .dateRequested(LocalDateTime.parse("2024-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2025-01-03T00:00:00"))
            .done(false)
            .build();

    String requestBody = mapper.writeValueAsString(recReqEdited);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(recReqOriginal));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/RecommendationRequest")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    verify(recommendationRequestRepository, times(1))
        .save(recReqEdited); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_recommendationrequest_that_does_not_exist() throws Exception {
    // arrange

    RecommendationRequest recReqEdited =
        RecommendationRequest.builder()
            .requesterEmail("testRequesterEmail")
            .professorEmail("testProfessorEmail")
            .explanation("This is an explanation")
            .dateRequested(LocalDateTime.parse("2022-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2023-01-03T00:00:00"))
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(recReqEdited);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/RecommendationRequest")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
  }
}
