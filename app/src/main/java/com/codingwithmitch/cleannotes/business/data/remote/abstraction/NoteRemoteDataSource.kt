package com.codingwithmitch.cleannotes.business.data.remote.abstraction

import com.codingwithmitch.cleannotes.business.domain.model.Note

interface NoteRemoteDataSource {

    suspend fun insertOrUpdateNote(note: Note)
    suspend fun deleteNote(primaryKey: String)
    suspend fun insertDeletedNote(note: Note)
    suspend fun insertDeletedNotes(notes: List<Note>)
    suspend fun deleteDeletedNote(note: Note)
    suspend fun getDeletedNotes(): List<Note>
    suspend fun deleteAllNotes()
    suspend fun searchNote(note: Note)
    suspend fun insertOrUpdates(note: Note): Note?
    suspend fun getAllNotes(): List<Note>
}