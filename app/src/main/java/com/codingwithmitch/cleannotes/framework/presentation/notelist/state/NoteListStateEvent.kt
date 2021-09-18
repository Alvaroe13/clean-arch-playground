package com.codingwithmitch.cleannotes.framework.presentation.notelist.state

import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.state.StateEvent
import com.codingwithmitch.cleannotes.business.domain.state.StateMessage

/**
 * all possible events (action) to execute in noteList view
 */
sealed class NoteListStateEvent : StateEvent {

    class InsertNewNoteEvent(
        val title: String
    ) : NoteListStateEvent() {
        override fun errorInfo(): String = "Error inserting new note"
        override fun eventName(): String = "InsertNewNoteEvent"
        override fun shouldDisplayProgressBar(): Boolean = true
    }

    /* for testing only */
    class InsertMultipleNotesEvent(
        val numNotes: Int
    ) : NoteListStateEvent() {
        override fun errorInfo(): String = " Error inserting multiple notes"
        override fun eventName(): String = "InsertMultipleNotesEvent"
        override fun shouldDisplayProgressBar(): Boolean = true
    }

    class DeleteNoteEvent(
        val note: Note
    ): NoteListStateEvent(){
        override fun errorInfo(): String = "Error deleting note."
        override fun eventName(): String = "DeleteNoteEvent"
        override fun shouldDisplayProgressBar() = true
    }

    class DeleteMultipleNotesEvent(
        val notes: List<Note>
    ): NoteListStateEvent(){
        override fun errorInfo(): String = "Error deleting the selected notes."
        override fun eventName(): String = "DeleteMultipleNotesEvent"
        override fun shouldDisplayProgressBar() = true
    }

    class RestoreDeletedNoteEvent(
        val note: Note
    ): NoteListStateEvent() {
        override fun errorInfo(): String = "Error restoring the note that was deleted."
        override fun eventName(): String = "RestoreDeletedNoteEvent"
        override fun shouldDisplayProgressBar() = false
    }

    class SearchNotesEvent(
        val clearLayoutManagerState: Boolean = true
    ): NoteListStateEvent(){
        override fun errorInfo(): String = "Error getting list of notes."
        override fun eventName(): String = "SearchNotesEvent"
        override fun shouldDisplayProgressBar() = true
    }

    class GetNumNotesInCacheEvent: NoteListStateEvent(){
        override fun errorInfo(): String = "Error getting the number of notes from the cache."
        override fun eventName(): String =  "GetNumNotesInCacheEvent"
        override fun shouldDisplayProgressBar() = true
    }

    class CreateStateMessageEvent(
        val stateMessage: StateMessage
    ): NoteListStateEvent(){
        override fun errorInfo(): String = "Error creating a new state message."
        override fun eventName(): String = "CreateStateMessageEvent"
        override fun shouldDisplayProgressBar() = false
    }

}