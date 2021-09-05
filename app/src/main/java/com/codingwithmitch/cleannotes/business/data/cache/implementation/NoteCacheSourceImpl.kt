package com.codingwithmitch.cleannotes.business.data.cache.implementation

import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.framework.datasource.cache.abstraction.NoteDaoService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The reason for building this class is to delegate the actions to the DAO, meaning that whenever
 * "NteCacheDataSource" class is called, it will triggered this class and this class will
 * trigger the DAO.
 */
@Singleton
class NoteCacheSourceImpl
@Inject
constructor(
    private val noteDaoService: NoteDaoService
) : NoteCacheDataSource {

    override suspend fun insertNote(note: Note): Long = noteDaoService.insertNote(note)

    override suspend fun deleteNote(primaryKey: String): Int = noteDaoService.deleteNote(primaryKey)

    override suspend fun deleteNotes(noteList: List<Note>): Int =
        noteDaoService.deleteNotes(noteList)

    override suspend fun updateNote(
        primaryKey: String,
        newTitle: String,
        newBody: String?
    ): Int = noteDaoService.updateNote(primaryKey, newTitle, newBody)

    override suspend fun searchNotes(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Note> {
        TODO("Check filterAndOrder and make query")
    }

    override suspend fun searchNoteById(primaryKey: String): Note? =
        noteDaoService.searchNoteById(primaryKey)

    override suspend fun getNumNotes(): Int = noteDaoService.getNumNotes()

    override suspend fun getAllNotes(): List<Note> = noteDaoService.getAllNotes()

    override suspend fun insertNotes(notes: List<Note>): LongArray =
        noteDaoService.insertNotes(notes)
}