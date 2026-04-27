package edu.ucsb.cs156.example.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/ucsbdates/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/RecommendationRequest/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/RecommendationRequest/all")).andExpect(status().is(200)); // logged
  }

  /*
  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/RecommendationRequest").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }
        */

  // Authorization tests for /api/ucsbdates/post
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
        .andExpect(status().isOk()); // only admins can post
  }
}
