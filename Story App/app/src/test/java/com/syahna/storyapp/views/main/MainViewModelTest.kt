package com.syahna.storyapp.views.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.syahna.storyapp.DummyDataTesting
import com.syahna.storyapp.ListMainDispatcherRule
import com.syahna.storyapp.data.repositories.StoriesRepository
import com.syahna.storyapp.data.repositories.UserRepository
import com.syahna.storyapp.getOrAwaitValue
import com.syahna.storyapp.remote.response.ListStoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi

@RunWith(MockitoJUnitRunner::class)

class MainViewModelTest{

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = ListMainDispatcherRule()

    @Mock
    private lateinit var storiesRepository: StoriesRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }
    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val dummyStories = DummyDataTesting.generateDummyStoryResponse()
        val data: PagingData<ListStoryItem> = StoriesPagingSource.snapshot(dummyStories)
        val expectedQuote = MutableLiveData<PagingData<ListStoryItem>>()
        expectedQuote.value = data

        Mockito.`when`(storiesRepository.getListStoriesPaging()).thenReturn(expectedQuote)
        val mainViewModel = MainViewModel(userRepository, storiesRepository)
        val actualQuote: PagingData<ListStoryItem> = mainViewModel.pagingStories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = MainAdapterPaging.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStories.size, differ.snapshot().size)
        Assert.assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedQuote = MutableLiveData<PagingData<ListStoryItem>>()
        expectedQuote.value = data
        Mockito.`when`(storiesRepository.getListStoriesPaging()).thenReturn(expectedQuote)
        val mainViewModel = MainViewModel(userRepository, storiesRepository)
        val actualQuote: PagingData<ListStoryItem> = mainViewModel.pagingStories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = MainAdapterPaging.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)
        Assert.assertEquals(0, differ.snapshot().size)
    }
}

class StoriesPagingSource : PagingSource<Int, LiveData<List<ListStoryItem>>>() {
    companion object {
        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

    val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
}