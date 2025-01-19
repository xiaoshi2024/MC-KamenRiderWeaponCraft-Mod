//package com.xiaoshi2022.kamen_rider_weapon_craft.util;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//
//import java.io.FileWriter;
//import java.io.IOException;
//
//public class BookEntryGenerator {
//
//    /**
//     * 生成图书条目JSON并保存到文件
//     *
//     * @param name     条目名称
//     * @param icon     图标名称
//     * @param category 分类名称
//     * @param text     条目文本内容
//     * @param filePath 文件保存路径
//     */
//    public static void generateBookEntry(String name, String icon, String category, String text, String filePath) {
//        // 创建根JSON对象
//        JsonObject root = new JsonObject();
//        root.addProperty("name", name);
//        root.addProperty("icon", icon);
//        root.addProperty("category", category);
//
//        // 创建页面数组
//        JsonArray pages = new JsonArray();
//        JsonObject page = new JsonObject();
//        page.addProperty("type", "patchouli:text");
//        page.addProperty("text", text);
//        pages.add(page);
//
//        // 将页面数组添加到根对象
//        root.add("pages", pages);
//
//        // 使用Gson将JSON对象转换为字符串
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        String jsonString = gson.toJson(root);
//
//        // 将JSON字符串写入文件
//        try (FileWriter writer = new FileWriter(filePath)) {
//            writer.write(jsonString);
//            System.out.println("图书条目JSON已生成并保存到: " + filePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void main(String[] args) {
//        String name = "New Test Entry";
//        String icon = "minecraft:writable_book";
//        String category = "your_book_namespace_change_me:test_category";
//        String text = "This is a newly generated test entry.";
//        String filePath = "new_test_entry.json";
//
//        generateBookEntry(name, icon, category, text, filePath);
//    }
//}
