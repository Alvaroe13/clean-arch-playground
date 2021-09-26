package com.codingwithmitch.cleannotes.framework.presentation.notelist

import android.content.SharedPreferences
import android.os.Parcelable
import androidx.lifecycle.LiveData
import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.model.NoteFactory
import com.codingwithmitch.cleannotes.business.domain.state.*
import com.codingwithmitch.cleannotes.business.interactors.notelist.DeleteMultipleNotes.Companion.DELETE_NOTES_YOU_MUST_SELECT
import com.codingwithmitch.cleannotes.business.interactors.notelist.NoteListInteractors
import com.codingwithmitch.cleannotes.framework.datasource.cache.database.NOTE_FILTER_DATE_CREATED
import com.codingwithmitch.cleannotes.framework.datasource.cache.database.NOTE_ORDER_DESC
import com.codingwithmitch.cleannotes.framework.datasource.preferences.PreferenceKeys
import com.codingwithmitch.cleannotes.framework.presentation.common.BaseViewModel
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListInteractionManager
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListStateEvent
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListToolbarState
import com.codingwithmitch.cleannotes.framework.presentation.notelist.state.NoteListViewState
import com.codingwithmitch.cleannotes.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

const val DELETE_PENDING_ERROR = "There is already a pending delete operation."
const val NOTE_PENDING_DELETE_BUNDLE_KEY = "pending_delete"

@ExperimentalCoroutinesApi
@FlowPreview
class NoteListViewModel
constructor(
    private val noteListInteractors: NoteListInteractors,
    private val noteFactory: NoteFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel<NoteListViewState>() {


    val noteListInteractionManager = NoteListInteractionManager()

    val toolbarState: LiveData<NoteListToolbarState>
        get() = noteListInteractionManager.toolbarState


    init {
        setNoteFilter(
            sharedPreferences.getString(
                PreferenceKeys.NOTE_FILTER,
                NOTE_FILTER_DATE_CREATED
            )
        )
        setNoteOrder(
            sharedPreferences.getString(
                PreferenceKeys.NOTE_ORDER,
                NOTE_ORDER_DESC
            )
        )
    }


    /**
     * gate for incoming response with info to be presented to the user
     */
    override fun handleNewData(data: NoteListViewState) {
        data.let { viewState ->
            viewState.noteList?.let { noteList ->
                setNoteListData(noteList)
            }

            viewState.numNotesInCache?.let { numNotes ->
                setNumNotesInCache(numNotes)
            }

            viewState.newNote?.let { note ->
                setNote(note)
            }

            viewState.notePendingDelete?.let { restoredNote ->
                restoredNote.note?.let { note ->
                    setRestoredNoteId(note)
                }
                setNotePendingDelete(null)
            }
        }
    }

    /**
     * gate for firing off events
     */
    override fun setStateEvent(stateEvent: StateEvent) {

        setQueryExhausted(false)

        //printLogD("NoteListVM" , "stateEvent= ${stateEvent.eventName()} ")

        val job: Flow<DataState<NoteListViewState>?> = when (stateEvent) {

            is NoteListStateEvent.GetAllNotesEvent -> {
                noteListInteractors.getAllNotes.getAllNotes(
                    stateEvent = stateEvent
                )
            }

            is NoteListStateEvent.InsertNewNoteEvent -> {
                noteListInteractors.insertNewNote.insertNewNote(
                    title = stateEvent.title,
                    stateEvent = stateEvent
                )
            }

            is NoteListStateEvent.DeleteNoteEvent -> {
                noteListInteractors.deleteNote.deleteNote(
                    note = stateEvent.note,
                    stateEvent = stateEvent
                )
            }

            is NoteListStateEvent.DeleteMultipleNotesEvent -> {
                noteListInteractors.deleteMultipleNotes.deleteNotes(
                    notes = stateEvent.notes,
                    stateEvent = stateEvent
                )
            }

            is NoteListStateEvent.RestoreDeletedNoteEvent -> {
                noteListInteractors.restoreDeletedNote.restoreDeletedNote(
                    note = stateEvent.note,
                    stateEvent = stateEvent
                )
            }

            is NoteListStateEvent.SearchNotesEvent -> {
                if (stateEvent.clearLayoutManagerState) {
                    clearLayoutManagerState()
                }
                noteListInteractors.searchNotes.searchNotes(
                    query = getSearchQuery(),
                    filterAndOrder = getOrder() + getFilter(),
                    page = getPage(),
                    stateEvent = stateEvent
                )
            }

            is NoteListStateEvent.GetNumNotesInCacheEvent -> {
                noteListInteractors.getNumNotes.getNumNotes(
                    stateEvent = stateEvent
                )
            }

            is NoteListStateEvent.CreateStateMessageEvent -> {
                emitStateMessageEvent(
                    stateMessage = stateEvent.stateMessage,
                    stateEvent = stateEvent
                )
            }

            else -> {
                emitInvalidStateEvent(stateEvent)
            }
        }


        launchJob(stateEvent, job)
    }

    /*
    State
    */
    fun getSelectedNotes() = noteListInteractionManager.getSelectedNotes()

    fun setToolbarState(state: NoteListToolbarState) =
        noteListInteractionManager.setToolbarState(state)

    fun isMultiSelectionStateActive() = noteListInteractionManager.isMultiSelectionStateActive()

    fun addOrRemoveNoteFromSelectedList(note: Note) =
        noteListInteractionManager.addOrRemoveNoteFromSelectedList(note)

    fun isNoteSelected(note: Note): Boolean = noteListInteractionManager.isNoteSelected(note)

    fun clearSelectedNotes() = noteListInteractionManager.clearSelectedNotes()

    fun getFilter(): String = getCurrentViewStateOrNew().filter ?: NOTE_FILTER_DATE_CREATED

    fun getOrder(): String = getCurrentViewStateOrNew().order  ?: NOTE_ORDER_DESC

    fun getSearchQuery(): String = getCurrentViewStateOrNew().searchQuery ?: ""

    private fun getPage(): Int = getCurrentViewStateOrNew().page ?: 1

    fun getNoteListSize() = getCurrentViewStateOrNew().noteList?.size ?: 0

    private fun getNumNotesInCache() = getCurrentViewStateOrNew().numNotesInCache ?: 0

    // for debugging
    fun getActiveJobs() = dataChannelManager.getActiveJobs()

    fun getLayoutManagerState(): Parcelable? = getCurrentViewStateOrNew().layoutManagerState


    private fun findListPositionOfNote(note: Note?): Int {
        val viewState = getCurrentViewStateOrNew()
        viewState.noteList?.let { noteList ->
            for ((index, item) in noteList.withIndex()) {
                if (item.id == note?.id) {
                    return index
                }
            }
        }
        return 0
    }

    fun isPaginationExhausted() = getNoteListSize() >= getNumNotesInCache()

    fun isQueryExhausted(): Boolean {
        printLogD(
            "NoteListViewModel",
            "is query exhasuted? ${getCurrentViewStateOrNew().isQueryExhausted ?: true}"
        )
        return getCurrentViewStateOrNew().isQueryExhausted ?: true
    }

    override fun initNewViewState(): NoteListViewState = NoteListViewState()

    /*
    Setters
    */
    private fun setNoteListData(notesList: ArrayList<Note>) {
        val update = getCurrentViewStateOrNew()
        update.noteList = notesList
        setViewState(update)
    }

    fun setQueryExhausted(isExhausted: Boolean) {
        val update = getCurrentViewStateOrNew()
        update.isQueryExhausted = isExhausted
        setViewState(update)
    }

    // can be selected from Recyclerview or created new from dialog
    fun setNote(note: Note?) {
        val update = getCurrentViewStateOrNew()
        update.newNote = note
        setViewState(update)
    }

    fun setQuery(query: String?) {
        val update = getCurrentViewStateOrNew()
        update.searchQuery = query
        setViewState(update)
    }


    // if a note is deleted and then restored, the id will be incorrect.
    // So need to reset it here.
    private fun setRestoredNoteId(restoredNote: Note) {
        val update = getCurrentViewStateOrNew()
        update.noteList?.let { noteList ->
            for ((index, note) in noteList.withIndex()) {
                if (note.title.equals(restoredNote.title)) {
                    noteList.remove(note)
                    noteList.add(index, restoredNote)
                    update.noteList = noteList
                    break
                }
            }
        }
        setViewState(update)
    }

    private fun removePendingNoteFromList(note: Note?) {
        val update = getCurrentViewStateOrNew()
        val list = update.noteList
        if (list?.contains(note) == true) {
            list.remove(note)
            update.noteList = list
            setViewState(update)
        }
    }

    fun setNotePendingDelete(note: Note?) {
        val update = getCurrentViewStateOrNew()
        if (note != null) {
            update.notePendingDelete = NoteListViewState.NotePendingDelete(
                note = note,
                positionInList = findListPositionOfNote(note)
            )
        } else {
            update.notePendingDelete = null
        }
        setViewState(update)
    }

    private fun setNumNotesInCache(numNotes: Int) {
        val update = getCurrentViewStateOrNew()
        update.numNotesInCache = numNotes
        setViewState(update)
    }

    fun createNewNote(
        id: String? = null,
        title: String,
        body: String? = null
    ) = noteFactory.createSingleNote(id, title, body)

    private fun resetPage() {
        val update = getCurrentViewStateOrNew()
        update.page = 1
        setViewState(update)
    }

    fun clearList() {
        printLogD("ListViewModel", "clearList")
        val update = getCurrentViewStateOrNew()
        update.noteList = ArrayList()
        setViewState(update)
    }

    // workaround for tests
    // can't submit an empty string because SearchViews SUCK
    fun clearSearchQuery() {
        setQuery("")
        clearList()
        loadFirstPage()
    }

    private fun incrementPageNumber() {
        val update = getCurrentViewStateOrNew()
        val page = update.copy().page ?: 1
        update.page = page.plus(1)
        setViewState(update)
    }

    fun setLayoutManagerState(layoutManagerState: Parcelable) {
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = layoutManagerState
        setViewState(update)
    }

    fun clearLayoutManagerState() {
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = null
        setViewState(update)
    }

    fun setNoteFilter(filter: String?) {
        filter?.let {
            val update = getCurrentViewStateOrNew()
            update.filter = filter
            setViewState(update)
        }
    }

    fun setNoteOrder(order: String?) {
        val update = getCurrentViewStateOrNew()
        update.order = order
        setViewState(update)
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(PreferenceKeys.NOTE_FILTER, filter)
        editor.apply()

        editor.putString(PreferenceKeys.NOTE_ORDER, order)
        editor.apply()
    }

    /*
    StateEvent Triggers
    */

    fun deleteNotes() {
        if (getSelectedNotes().size > 0) {
            setStateEvent(NoteListStateEvent.DeleteMultipleNotesEvent(getSelectedNotes()))
            removeSelectedNotesFromList()
        } else {
            setStateEvent(
                NoteListStateEvent.CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = DELETE_NOTES_YOU_MUST_SELECT,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Info()
                        )
                    )
                )
            )
        }
    }

    private fun removeSelectedNotesFromList() {
        val update = getCurrentViewStateOrNew()
        update.noteList?.removeAll(getSelectedNotes())
        setViewState(update)
        clearSelectedNotes()
    }

    fun isDeletePending(): Boolean {
        val pendingNote = getCurrentViewStateOrNew().notePendingDelete
        if (pendingNote != null) {
            setStateEvent(
                NoteListStateEvent.CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = DELETE_PENDING_ERROR,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Info()
                        )
                    )
                )
            )
            return true
        } else {
            return false
        }
    }


    fun undoDelete() {
        // replace note in viewstate
        val update = getCurrentViewStateOrNew()
        update.notePendingDelete?.let { note ->
            if (note.positionInList != null && note.note != null) {
                update.noteList?.add(
                    note.positionInList as Int,
                    note.note as Note
                )
                setStateEvent(NoteListStateEvent.RestoreDeletedNoteEvent(note.note as Note))
            }
        }
        setViewState(update)
    }

    fun beginPendingDelete(note: Note) {
        setNotePendingDelete(note)
        removePendingNoteFromList(note)
        setStateEvent(
            NoteListStateEvent.DeleteNoteEvent(
                note = note
            )
        )
    }

    fun loadFirstPage() {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(NoteListStateEvent.GetAllNotesEvent())
        printLogD(
            "NoteListViewModel",
            "loadFirstPage: ${getCurrentViewStateOrNew().searchQuery}"
        )
    }

    fun loadAllNotes(){
        setQueryExhausted(false)
        setStateEvent(NoteListStateEvent.GetAllNotesEvent())
    }

    fun nextPage() {
        if (!isQueryExhausted()) {
            printLogD("NoteListViewModel", "attempting to load next page...")
            clearLayoutManagerState()
            incrementPageNumber()
            setStateEvent(NoteListStateEvent.SearchNotesEvent())
        }
    }

    fun retrieveNumNotesInCache() {
        setStateEvent(NoteListStateEvent.GetNumNotesInCacheEvent())
    }

    fun refreshSearchQuery() {
        setQueryExhausted(false)
        setStateEvent(NoteListStateEvent.SearchNotesEvent(false))
    }
}