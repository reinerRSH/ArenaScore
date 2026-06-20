package com.example.arena.Model.Di

import android.content.Context
import androidx.room.PrimaryKey
import androidx.room.Room
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.Model.Di.Data.ArenaDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
         return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun privideArenaDatabase(@ApplicationContext context: Context): ArenaDatabase{
        return Room.databaseBuilder(
            context,
            ArenaDatabase::class.java,
            "arena_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUSerDao(database: ArenaDatabase): UserDao{
        return database.userDao()
    }
}
