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
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all_orgs() throws Exception {
    UCSBOrganization mahjong =
        UCSBOrganization.builder()
            .orgCode("MHJ")
            .orgTranslationShort("Mahjong Club")
            .orgTranslation("Asian Board Games Club")
            .inactive(true)
            .build();

    UCSBOrganization chess =
        UCSBOrganization.builder()
            .orgCode("CHS")
            .orgTranslationShort("Chess Club")
            .orgTranslation("Chess Club")
            .inactive(true)
            .build();

    ArrayList<UCSBOrganization> organizations = new ArrayList<>();
    organizations.add(mahjong);
    organizations.add(chess);

    when(ucsbOrganizationRepository.findAll()).thenReturn(organizations);

    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization/all"))
            .andExpect(status().is(200))
            .andReturn(); // logged

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(organizations);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Authorization tests for /api/ucsbdiningcommons/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsborganization/post")
                .param("orgCode", "MHJ")
                .param("orgTranslationShort", "Mahjong Club")
                .param("orgTranslation", "Asian Board Games Club")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsborganization/post")
                .param("orgCode", "MHJ")
                .param("orgTranslationShort", "Mahjong Club")
                .param("orgTranslation", "Asian Board Games Club")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {
    // arrange

    UCSBOrganization mahjong =
        UCSBOrganization.builder()
            .orgCode("MHJ")
            .orgTranslationShort("Mahjong Club")
            .orgTranslation("Asian Board Games Club")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(mahjong))).thenReturn(mahjong);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganization/post")
                    .param("orgCode", "MHJ")
                    .param("orgTranslationShort", "Mahjong Club")
                    .param("orgTranslation", "Asian Board Games Club")
                    .param("inactive", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(mahjong);
    String expectedJson = mapper.writeValueAsString(mahjong);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
