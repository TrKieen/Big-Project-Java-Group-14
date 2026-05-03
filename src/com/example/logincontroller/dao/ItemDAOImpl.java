package com.example.logincontroller.dao;

import com.example.logincontroller.model.Item;
import java.util.ArrayList;
import java.util.List;

public class ItemDAOImpl implements ItemDAO {
    private final List<Item> database = new ArrayList<>();

    @Override
    public boolean addItem(Item item, String itemType, String extraInfo) {
        database.add(item);
        System.out.println("DAO: Đã INSERT " + item.getName() + " vào Database.");
        return true;
    }

    @Override
    public boolean updateItem(Item item, String itemType, String extraInfo) {
        for (int i = 0; i < database.size(); i++) {
            if (database.get(i).getId().equals(item.getId())) {
                database.set(i, item);
                System.out.println("DAO: Đã UPDATE " + item.getName() + " trong Database.");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteItem(String itemId) {
        boolean isRemoved = database.removeIf(item -> item.getId().equals(itemId));
        if (isRemoved) {
            System.out.println("DAO: Đã DELETE sản phẩm có ID: " + itemId);
        }
        return isRemoved;
    }

    @Override
    public List<Item> getAllItems() {
        return new ArrayList<>(database);
    }
}