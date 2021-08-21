package com.codingwithmitch.cleannotes.business.data.remote.implementation

import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.domain.model.Note
import javax.inject.Inject
import javax.inject.Singleton

/**
 * delegates all the work to its correspondent class
 */
@Singleton
class NoteRemoteSourceImpl
@Inject
constructor(
    private val firestoreService: FirestoreService
) : NoteRemoteDataSource {

    override suspend fun insertOrUpdateNote(note: Note) = firestoreService.insertOrUpdate(note)

    override suspend fun deleteNote(primaryKey: String) = firestoreService.deleteNote(primaryKey)

    override suspend fun insertDeletedNote(note: Note) = firestoreService.insertDeletedNote(note)

    override suspend fun insertDeletedNotes(notes: List<Note>) = firestoreService.insertDeletedNotes(notes)

    override suspend fun deleteDeletedNote(note: Note) = firestoreService.deleteDeletedNote(note)

    override suspend fun getDeletedNotes(): List<Note> = firestoreService.getDeletedNotes()

    override suspend fun deleteAllNotes() = firestoreService.deleteAllNotes()

    override suspend fun searchNote(note: Note) = firestoreService.searchNote(note)

    override suspend fun insertOrUpdates(note: Note) = firestoreService.insertOrUpdates(note)

    override suspend fun getAllNotes(): List<Note> = firestoreService.getAllNotes()
}