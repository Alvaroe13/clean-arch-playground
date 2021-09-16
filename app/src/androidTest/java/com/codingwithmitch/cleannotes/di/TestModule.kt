package com.codingwithmitch.cleannotes.di

import androidx.room.Room
import com.codingwithmitch.cleannotes.framework.datasource.cache.database.NoteDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.codingwithmitch.cleannotes.framework.presentation.TestBaseApplication
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object TestModule {

    /**
     * by using "inMemoryDatabaseBuilder" room creates a table before the test and after
     * the test is done it removes it from the device ( normally used for testing )
     */
    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDb(app: TestBaseApplication): NoteDatabase {
        return Room
            .inMemoryDatabaseBuilder(app, NoteDatabase::class.java)
            .fallbackToDestructiveMigration()
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

}