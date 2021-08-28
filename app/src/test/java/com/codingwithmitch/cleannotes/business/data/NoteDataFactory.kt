package com.codingwithmitch.cleannotes.business.data

import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class NoteDataFactory(
    private val testClassLoader: ClassLoader
) {

    fun produceListOfNotes(): List<Note> {
        return Gson()
            .fromJson(
                getNotesFromFile( JSON_FILE_NAME ),
                object : TypeToken<List<Note>>() {}.type
            )
    }

    fun produceHashMapOfNotes(noteList: List<Note>): HashMap<String, Note> {
        val map = HashMap<String, Note>()
        noteList.forEach {
            map[it.id] = it
        }
        return map
    }

    fun produceEmptyListOfNotes(): List<Note> = ArrayList()

    fun getNotesFromFile(fileName: String): String =
        testClassLoader.getResource(fileName).readText()

    companion object {
        const val JSON_FILE_NAME = "note_list.json"
    }
}