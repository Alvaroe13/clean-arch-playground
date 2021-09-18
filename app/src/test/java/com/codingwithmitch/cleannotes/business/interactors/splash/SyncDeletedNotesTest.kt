package com.codingwithmitch.cleannotes.business.interactors.splash

import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.interactors.BaseUseCaseToolsTest
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue

/*
Test cases:
1. deleteNetworkNotes_confirmCacheSync()
    a) select some notes for deleting from network
    b) delete from network
    c) perform sync
    d) confirm notes from cache were deleted
 */

@InternalCoroutinesApi
class SyncDeletedNotesTest : BaseUseCaseToolsTest() {

    //system in test
    private lateinit var syncDeletedNotes : SyncDeletedNotes


    init {
        initSystemInTest()
    }


    override fun initSystemInTest() {
         syncDeletedNotes = SyncDeletedNotes(
             noteCacheDataSource = noteCacheDataSource,
             noteRemoteDataSource = noteRemoteDataSource
         )
    }

    @Test
    fun deleteNetworkNotes_confirmCacheSync() = runBlocking {

        // select some notes to be deleted from cache
        val networkNotes = noteRemoteDataSource.getAllNotes()
        val notesToDelete: ArrayList<Note> = ArrayList()
        for(note in networkNotes){
            notesToDelete.add(note)
            noteRemoteDataSource.deleteNote(note.id)
            //noteRemoteDataSource.insertDeletedNote(note) // insert into deletes node
            if(notesToDelete.size > 3){
                break
            }
        }

        // perform sync
        syncDeletedNotes.syncDeletedNotes()

        // confirm notes were deleted from cache
        for(note in notesToDelete){
            val cachedNote = noteCacheDataSource.searchNoteById(note.id)
            assertTrue { cachedNote == null }
        }
    }
}
