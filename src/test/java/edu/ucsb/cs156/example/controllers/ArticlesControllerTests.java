package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {
  @MockitoBean ArticlesRepository articlesRepository;
  @MockitoBean UserRepository userRepository;

  // GET tests
  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(403));
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/articles").param("id", "0")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    Articles article =
        Articles.builder()
            .title("Test Article")
            .url("https://example.com/test")
            .explanation("Test article explanation")
            .build();

    when(articlesRepository.findById(eq(1L))).thenReturn(Optional.of(article));

    MvcResult response =
        mockMvc
            .perform(get("/api/articles").param("id", "1"))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).findById(eq(1L));
    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    when(articlesRepository.findById(eq(1L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/articles").param("id", "1"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(articlesRepository, times(1)).findById(eq(1L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Articles with id 1 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(200));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {

    Articles article1 =
        Articles.builder()
            .title("First Article")
            .url("https://example.com/1")
            .explanation("Test article 1")
            .build();

    Articles article2 =
        Articles.builder()
            .title("Second Article")
            .url("https://example.com/2")
            .explanation("Test article 2")
            .build();

    ArrayList<Articles> expectedArticles = new ArrayList<>();
    expectedArticles.addAll(Arrays.asList(article1, article2));

    when(articlesRepository.findAll()).thenReturn(expectedArticles);

    MvcResult response =
        mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk()).andReturn();

    verify(articlesRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedArticles);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // POST TESTS
  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "First Article")
                .param("url", "https://example.com/1")
                .param("explanation", "Test explanation")
                .param("email", "test@ucsb.edu")
                .param("localDateTime", "2022-01-03T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "First Article")
                .param("url", "https://example.com/1")
                .param("explanation", "Test explanation")
                .param("email", "test@ucsb.edu")
                .param("localDateTime", "2022-01-03T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {

    LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

    Articles expected =
        Articles.builder()
            .title("First Article")
            .url("https://example.com/1")
            .explanation("Test explanation")
            .email("test@ucsb.edu")
            .dateAdded(ldt)
            .build();

    when(articlesRepository.save(any(Articles.class))).thenReturn(expected);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/articles/post")
                    .param("title", "First Article")
                    .param("url", "https://example.com/1")
                    .param("explanation", "Test explanation")
                    .param("email", "test@ucsb.edu")
                    .param("localDateTime", "2022-01-03T00:00:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository)
        .save(
            argThat(
                article ->
                    article.getTitle().equals("First Article")
                        && article.getUrl().equals("https://example.com/1")
                        && article.getExplanation().equals("Test explanation")
                        && article.getEmail().equals("test@ucsb.edu")
                        && article.getDateAdded().equals(ldt)));

    verify(articlesRepository, times(1)).save(any(Articles.class));

    String expectedJson = mapper.writeValueAsString(expected);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }
}
