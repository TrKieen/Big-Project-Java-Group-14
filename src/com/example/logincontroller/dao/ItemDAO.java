package com.example.logincontroller.dao;
import com.example.logincontroller.model.Item;
import java.util.List;

public interface ItemDAO {
    boolean addItem(Item item, String itemType, String extraInfo);
    boolean updateItem(Item item, String itemType, String extraInfo);
    boolean deleteItem(String itemId);
    List<Item> getAllItems();
}