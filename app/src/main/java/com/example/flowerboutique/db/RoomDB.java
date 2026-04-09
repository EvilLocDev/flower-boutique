package com.example.flowerboutique.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.flowerboutique.db.entities.CartEntity;
import com.example.flowerboutique.db.dao.CartDAO;

@Database(entities = {CartEntity.class}, version = 2, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {

    public abstract CartDAO cartDAO();

    private static volatile RoomDB INSTANCE;

    public static RoomDB getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (RoomDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    RoomDB.class, "flowerboutique_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}