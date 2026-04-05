package com.example.flowerboutique.ui.admin.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flowerboutique.BoutiqueApplication;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AdminViewModel extends ViewModel {
    private final AppFirebase appFirebase = BoutiqueApplication.getInstance().getAppFirebase();

    private final MutableLiveData<Long> totalRevenue = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer> totalOrders = new MutableLiveData<>(0);
    private final MutableLiveData<Map<String, Long>> revenueByDate = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> orderStatusDistribution = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<Long> getTotalRevenue() { return totalRevenue; }
    public LiveData<Integer> getTotalOrders() { return totalOrders; }
    public LiveData<Map<String, Long>> getRevenueByDate() { return revenueByDate; }
    public LiveData<Map<String, Integer>> getOrderStatusDistribution() { return orderStatusDistribution; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadDashboardData() {
        isLoading.setValue(true);
        appFirebase.getOrdersCollection()
                .orderBy("created_date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        processOrders(task.getResult().getDocuments());
                    }
                });
    }

    private void processOrders(List<DocumentSnapshot> documents) {
        long revenue = 0;
        int ordersCount = documents.size();
        Map<String, Long> revByDate = new TreeMap<>(); // Dùng TreeMap để tự sắp xếp theo ngày
        Map<String, Integer> statusDist = new HashMap<>();

        // Lấy mốc 7 ngày gần đây
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        Date sevenDaysAgo = cal.getTime();

        for (DocumentSnapshot doc : documents) {
            // 1. Tính tổng doanh thu & status
            List<HashMap<String, Object>> products = (List<HashMap<String, Object>>) doc.get("products");
            if (products == null) continue;

            long orderTotal = products.stream()
                    .mapToLong(p -> ((Long) p.get("unitPrice")) * ((Long) p.get("quantity")))
                    .sum();
            
            revenue += orderTotal;

            // 2. Thống kê trạng thái
            String status = doc.getString("status");
            if (status == null) status = "N/A";
            statusDist.put(status, statusDist.getOrDefault(status, 0) + 1);

            // 3. Thống kê doanh thu theo ngày (chỉ lấy 7 ngày gần nhất)
            Date date = doc.getDate("created_date");
            if (date != null && date.after(sevenDaysAgo)) {
                String dateStr = android.text.format.DateFormat.format("dd/MM", date).toString();
                revByDate.put(dateStr, revByDate.getOrDefault(dateStr, 0L) + orderTotal);
            }
        }

        totalRevenue.setValue(revenue);
        totalOrders.setValue(ordersCount);
        revenueByDate.setValue(revByDate);
        orderStatusDistribution.setValue(statusDist);
    }
}
