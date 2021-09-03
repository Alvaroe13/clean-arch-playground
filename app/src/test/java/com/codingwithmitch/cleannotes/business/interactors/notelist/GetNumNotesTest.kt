package com.codingwithmitch.cleannotes.business.interactors.notelist

import com.codingwithmitch.cleannotes.business.domain.state.DataState
import com.codingwithmitch.cleannotes.business.interactors.BaseUseCaseToolsTest
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListStateEvent
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListViewState
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
Test cases:
1. getNumNotes_success_confirmCorrect()
    a) get the number of notes in cache
    b) listen for GET_NUM_NOTES_SUCCESS from flow emission
    c) compare with the number of notes in the fake data set
*/
@InternalCoroutinesApi
class GetNumNotesTest : BaseUseCaseToolsTest() {

    //system in test
    private lateinit var getNumNotes: GetNumNotes

    init {
        initSystemInTest()
    }

    override fun initSystemInTest() {
        getNumNotes = GetNumNotes(
            noteCacheDataSource = noteCacheDataSource
        )
    }

    @Test
    fun getNumNotes_success_confirmCorrect() = runBlocking {

        var numNotes = 0

        getNumNotes.getNumNotes(
            stateEvent = NoteListStateEvent.GetNumNotesInCacheEvent()
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    GetNumNotes.GET_NUM_NOTES_SUCCESS
                )
                numNotes = value?.data?.numNotesInCache ?: 0
            }

        })

        //compare the value returned directly from cache with value emitted by flow, if the same, everything is ok.
        val actualNumNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue{ actualNumNotesInCache == numNotes}
    }

}