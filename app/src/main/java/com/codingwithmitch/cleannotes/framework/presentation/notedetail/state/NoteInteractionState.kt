package com.codingwithmitch.cleannotes.framework.presentation.notedetail.state

sealed class NoteInteractionState {

    class EditState : NoteInteractionState() {
        override fun toString(): String = "EditState"
    }

    class DefaultState : NoteInteractionState() {
        override fun toString(): String = "DefaultState"
    }
}