package com.alif.mykos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alif.mykos.utils.OccupiedRoomExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [KamarEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun kamarDao(): KamarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "mykos_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seedInitialRoomsIfNeeded(appContext)
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            seedInitialRoomsIfNeeded(appContext)
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            seedInitialRoomsIfNeeded(appContext)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun seedInitialRoomsIfNeeded(context: Context) {
            val prefs = context.getSharedPreferences("mykos_seed_prefs", Context.MODE_PRIVATE)
            if (prefs.getBoolean("seeded_16_rooms", false)) return

            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                val dao = getInstance(context).kamarDao()
                if (dao.getOccupiedKamarSync().isEmpty()) {
                    dao.insertAllKamar(OccupiedRoomExporter.getInitialSampleRooms())
                }
                prefs.edit().putBoolean("seeded_16_rooms", true).apply()
            }
        }
    }
}