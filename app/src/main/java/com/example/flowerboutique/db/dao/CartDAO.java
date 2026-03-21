package com.example.flowerboutique.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import com.example.flowerboutique.db.entities.CartEntity;

@Dao
public interface CartDAO {

    // Lấy toàn bộ sản phẩm trong giỏ hàng (Trẻ về LiveData để UI tự cập nhật)
    @Query("SELECT * FROM cart_table")
    LiveData<List<CartEntity>> getAllInCart();

    // Thêm một sản phẩm vào giỏ
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCart(CartEntity cartEntity);

    // Xóa toàn bộ giỏ hàng (Gọi khi đặt hàng thành công)
    @Query("DELETE FROM cart_table")
    void deleteAll();
}