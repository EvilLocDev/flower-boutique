package com.example.flowerboutique.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;
import com.example.flowerboutique.db.entities.CartEntity;

@Dao
public interface CartDAO {

    // Lấy toàn bộ sản phẩm trong giỏ hàng (Trả về LiveData để UI tự cập nhật)
    @Query("SELECT * FROM cart_table")
    LiveData<List<CartEntity>> getAllInCart();

    // Thêm một sản phẩm vào giỏ
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCart(CartEntity cartEntity);

    // Xóa toàn bộ giỏ hàng (Gọi khi đặt hàng thành công)
    @Query("DELETE FROM cart_table")
    void deleteAll();

    // Sửa List<Long> thành long[] để Room generate code đúng
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertInCart(CartEntity... carts);

    @Query("UPDATE cart_table SET quantity = quantity + :i WHERE product_id = :id")
    void modifyQuantity(int i, String id);

    @Transaction
    default void increaseProductInCart(String id) {
        CartEntity cart = new CartEntity();
        cart.setProduct_id(id);
        cart.setQuantity(1);
        long[] resultIds = insertInCart(cart);

        if (resultIds.length > 0 && resultIds[0] == -1) {
            modifyQuantity(1, id);
        }
    }

    // Bổ sung vào trong interface CartDAO
    @Query("UPDATE cart_table SET quantity = quantity - 1 WHERE product_id = :id AND quantity > 1")
    void decreaseQuantity(String id);

    @Query("DELETE FROM cart_table WHERE product_id = :id")
    void deleteItem(String id);
}
