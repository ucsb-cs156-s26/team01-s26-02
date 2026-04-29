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
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {
  @MockitoBean MenuItemReviewRepository menuItemReviewRepository;
  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/menuitemreview/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreview/post")
                .param("itemID", "1")
                .param("reviewerEmail", "reviewerEmailstring")
                .param("stars", "5")
                .param("dateReviewed", "2022-01-03T00:00:00")
                .param("comments", "bruh")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreview/post")
                .param("itemID", "1")
                .param("reviewerEmail", "reviewerEmailstring")
                .param("stars", "5")
                .param("dateReviewed", "2022-01-03T00:00:00")
                .param("comments", "bruh")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menuitemreviews() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview menuItemReview1 =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("reviewerEmailstring")
            .stars(5)
            .dateReviewed(ldt1)
            .comments("bruh")
            .build();

    // UCSBDate ucsbDate1 =
    //     UCSBDate.builder()
    //         .name("firstDayOfClasses")
    //         .quarterYYYYQ("20222")
    //         .localDateTime(ldt1)
    //         .build();

    // LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

    // UCSBDate ucsbDate2 =
    //     UCSBDate.builder()
    //         .name("lastDayOfClasses")
    //         .quarterYYYYQ("20222")
    //         .localDateTime(ldt2)
    //         .build();

    ArrayList<MenuItemReview> expectedMenuItemReviews = new ArrayList<>();
    expectedMenuItemReviews.add(menuItemReview1);
    // stopped here 41:57
    when(menuItemReviewRepository.findAll()).thenReturn(expectedMenuItemReviews);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(menuItemReviewRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedMenuItemReviews);

    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_menuitemreview() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview menuItemReview1 =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("reviewerEmailstring")
            .stars(5)
            .dateReviewed(ldt1)
            .comments("bruh")
            .build();
    when(menuItemReviewRepository.save(eq(menuItemReview1))).thenReturn(menuItemReview1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/menuitemreview/post")
                    .param("itemID", "1")
                    .param("reviewerEmail", "reviewerEmailstring")
                    .param("stars", "5")
                    .param("dateReviewed", "2022-01-03T00:00:00")
                    .param("comments", "bruh")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).save(menuItemReview1);
    String expectedJson = mapper.writeValueAsString(menuItemReview1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
