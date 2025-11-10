package com.example.saveuplite

import com.example.saveuplite.model.Post
import com.example.saveuplite.repository.PostRepository
import com.example.saveuplite.viewmodel.PostViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PostViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `postList should contain expected data after fetchPosts()`() = runTest {
        // 1. Define the fake data
        val fakePosts = listOf(
            Post(userId = 1, id = 1, title = "Título 1", body = "Contenido 1"),
            Post(userId = 2, id = 2, title = "Título 2", body = "Contenido 2")
        )

        // 2. Create a fake repository that returns the fake data
        val fakeRepository = object : PostRepository() {
            override suspend fun getPosts(): List<Post> {
                return fakePosts
            }
        }

        // 3. Instantiate the REAL ViewModel with the fake repository
        val viewModel = PostViewModel(fakeRepository)

        // 4. Call the method and assert the public StateFlow's value
        viewModel.fetchPosts()
        assertEquals(fakePosts, viewModel.postList.value)
    }
}
