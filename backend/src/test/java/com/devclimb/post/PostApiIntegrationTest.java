package com.devclimb.post;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/sql/post-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PostApiIntegrationTest {

    private static final String POSTS_PATH = "/api/communities/{communityId}/posts";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createPost_authenticatedMember_returns200AndPersistsTrimmedContent() throws Exception {
        mockMvc.perform(post(POSTS_PATH, 1)
                        .header("X-Dev-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"  First integration post  \"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.communityId").value(1))
                .andExpect(jsonPath("$.author.id").value(1))
                .andExpect(jsonPath("$.author.name").value("Dev User 1"))
                .andExpect(jsonPath("$.content").value("First integration post"))
                .andExpect(jsonPath("$.attachments").isEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        Integer postId = jdbcTemplate.queryForObject(
                "select post_id from post where post_content = ?",
                Integer.class,
                "First integration post"
        );

        assertThat(postId).isNotNull();

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select user_id, comm_id, post_parentid, post_content from post where post_id = ?",
                postId
        );

        assertThat(row)
                .containsEntry("user_id", 1)
                .containsEntry("comm_id", 1)
                .containsEntry("post_parentid", null)
                .containsEntry("post_content", "First integration post");
    }

    @ParameterizedTest(name = "rejects content body: {0}")
    @ValueSource(strings = {
            "{\"content\":\"   \"}",
            "{\"content\":\"\"}",
            "{\"content\":null}",
            "{}"
    })
    void createPost_blankOrMissingContent_returns400(String body) throws Exception {
        mockMvc.perform(post(POSTS_PATH, 1)
                        .header("X-Dev-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POST_CONTENT_EMPTY"));

        assertThat(postCount()).isEqualTo(4);
    }

    @Test
    void createPost_withoutAuthentication_returns401AndDoesNotPersist() throws Exception {
        mockMvc.perform(post(POSTS_PATH, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Anonymous post\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        assertThat(postCount()).isEqualTo(4);
    }

    @Test
    void createPost_withInactiveDevUser_returns401() throws Exception {
        mockMvc.perform(post(POSTS_PATH, 1)
                        .header("X-Dev-User-Id", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Inactive user post\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        assertThat(postCount()).isEqualTo(4);
    }

    @Test
    void createPost_nonMember_returns403AndDoesNotPersist() throws Exception {
        mockMvc.perform(post(POSTS_PATH, 2)
                        .header("X-Dev-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Not a member post\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_COMMUNITY_MEMBER"));

        assertThat(postCount()).isEqualTo(4);
    }

    @Test
    void createPost_missingCommunity_returns404() throws Exception {
        mockMvc.perform(post(POSTS_PATH, 999)
                        .header("X-Dev-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Missing community post\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMUNITY_NOT_FOUND"));
    }

    @Test
    void createPost_inactiveCommunity_returns404() throws Exception {
        mockMvc.perform(post(POSTS_PATH, 3)
                        .header("X-Dev-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Inactive community post\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMUNITY_NOT_FOUND"));
    }

    @Test
    void createPost_malformedJson_returns400ValidationError() throws Exception {
        mockMvc.perform(post(POSTS_PATH, 1)
                        .header("X-Dev-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void getPosts_publicCommunityAsGuest_returnsTopLevelNewestFirstWithAttachments() throws Exception {
        mockMvc.perform(get(POSTS_PATH, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value("Newest public post"))
                .andExpect(jsonPath("$[0].author.name").value("Dev User 1"))
                .andExpect(jsonPath("$[0].attachments", hasSize(1)))
                .andExpect(jsonPath("$[0].attachments[0].type").value("IMAGE"))
                .andExpect(jsonPath("$[0].attachments[0].url").value("https://example.com/attachment.png"))
                .andExpect(jsonPath("$[1].content").value("Older public post"))
                .andExpect(jsonPath("$[?(@.content == 'Reply to older post')]", hasSize(0)));
    }

    @Test
    void getPosts_privateCommunity_enforcesAuthenticationAndMembership() throws Exception {
        mockMvc.perform(get(POSTS_PATH, 2))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        mockMvc.perform(get(POSTS_PATH, 2)
                        .header("X-Dev-User-Id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_COMMUNITY_MEMBER"));

        mockMvc.perform(get(POSTS_PATH, 2)
                        .header("X-Dev-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content").value("Private member post"))
                .andExpect(jsonPath("$[0].author.id").value(2));
    }

    @Test
    void getPosts_missingOrInactiveCommunity_returns404() throws Exception {
        mockMvc.perform(get(POSTS_PATH, 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMUNITY_NOT_FOUND"));

        mockMvc.perform(get(POSTS_PATH, 3))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMUNITY_NOT_FOUND"));
    }

    @Test
    void createdPostIsReturnedAfterReloadWithTheSameId() throws Exception {
        MvcResult created = mockMvc.perform(post(POSTS_PATH, 1)
                        .header("X-Dev-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Reload persistence check\"}"))
                .andExpect(status().isOk())
                .andReturn();

        Integer postId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        assertThat(postId).isNotNull();

        mockMvc.perform(get(POSTS_PATH, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(postId))
                .andExpect(jsonPath("$[0].content").value("Reload persistence check"));
    }

    @Test
    void memberCanCreatePostInPrivateCommunity() throws Exception {
        mockMvc.perform(post(POSTS_PATH, 2)
                        .header("X-Dev-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Private member created post\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.communityId").value(2))
                .andExpect(jsonPath("$.author.id").value(2))
                .andExpect(jsonPath("$.content").value("Private member created post"));

        mockMvc.perform(get(POSTS_PATH, 2)
                        .header("X-Dev-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value("Private member created post"));
    }

    private int postCount() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from post", Integer.class);
        return count == null ? -1 : count;
    }
}
