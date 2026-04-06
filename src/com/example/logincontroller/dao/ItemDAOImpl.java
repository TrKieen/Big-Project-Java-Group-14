package com.example.logincontroller.dao;
import com.example.logincontroller.model.Item;
import java.util.ArrayList;
import java.util.List;

public class ItemDAOImpl implements ItemDAO {

    @Override
    public boolean addItem(Item item, String itemType, String extraInfo) {
        System.out.println("DAO: Đã INSERT " + item.getName() + " vào Database.");
        return true;
    }

    @Override
    public boolean updateItem(Item item, String itemType, String extraInfo) {
        System.out.println("DAO: Đã UPDATE " + item.getName() + " trong Database.");
        return true;
    }

    @Override
    public boolean deleteItem(String itemId) {
        System.out.println("DAO: Đã DELETE sản phẩm có ID: " + itemId);
        return true;
    }

    @Override
    public List<Item> getAllItems() {
        return new ArrayList<>();
    }
}