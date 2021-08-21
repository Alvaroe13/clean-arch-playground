package com.codingwithmitch.cleannotes.business.data.cache.abstraction

import com.codingwithmitch.cleannotes.business.domain.model.Note

/**
 * here are the functionalities the cache will cover throughout this app
 */
interface NoteCacheDataSource {

    suspend fun insertNote(note: Note): Long

    suspend fun deleteNote(primaryKey: String): Int

    suspend fun deleteNotes(noteList: List<Note>): Int

    suspend fun updateNote(primaryKey: String, newTitle: String, newBody: String): Int

    suspend fun searcNotes(query: String, filterAndOrder: String, page: Int): List<Note>

    suspend fun searchNoteById(primaryKey: String): Note?

    suspend fun getNumNotes(): Int

    /** for testing only */
    suspend fun insertNotes(notes: List<Note>): LongArray

}