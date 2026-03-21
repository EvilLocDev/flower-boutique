package com.example.flowerboutique.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// TODO: Đảm bảo bạn đã import đúng đường dẫn của CartEntity và CartDAO
import com.example.flowerboutique.db.entities.CartEntity;
import com.example.flowerboutique.db.dao.CartDAO;

// Khai báo các bảng (entities) có trong Database và phiên bản (version)
@Database(entities = {CartEntity.class}, version = 1, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {

    // Khai báo các DAO (Data Access Object) để thao tác với dữ liệu
    public abstract CartDAO cartDAO();

}