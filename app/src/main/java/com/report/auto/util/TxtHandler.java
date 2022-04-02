package com.report.auto.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**读取\写入txt文件的工具*/
public class TxtHandler {
    public static String read(String pathname){
        if(pathname==null||pathname.isEmpty())return null;
        File file=new File(pathname);
        if(file.exists()&&file.isFile()){
            try {
                BufferedReader reader=new BufferedReader(new FileReader(file));
                String line;
                StringBuilder text= new StringBuilder();
                while ((line=reader.readLine())!=null){
                    text.append(line).append("\n");
                }
                reader.close();
                return text.toString();
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static List<String> readLines(String pathname){
        List<String> lines=new ArrayList<>();
        if(pathname==null||pathname.isEmpty())return lines;
        File file=new File(pathname);
        if(file.exists()&&file.isFile()){
            try {
                BufferedReader reader=new BufferedReader(new FileReader(file));
                String line;
                while ((line=reader.readLine())!=null){
                    lines.add(line);
                }
                reader.close();
                return lines;
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lines;
    }
    public static interface Operation<T>{
        void doOperation(T value);
    }
    public static void readLinesAndDoOperation(String pathname,Operation<String> op){
        if(pathname==null||pathname.isEmpty())return;
        File file=new File(pathname);
        if(file.exists()&&file.isFile()){
            try {
                BufferedReader reader=new BufferedReader(new FileReader(file));
                String line;
                while ((line=reader.readLine())!=null){
                    op.doOperation(line);
                }
                reader.close();
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static boolean write(String pathname,String content,boolean append){
        File file=new File(pathname);
        try {
        if(file.exists()){
            if(file.isDirectory())return false;
        }else {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        BufferedWriter writer=new BufferedWriter(new FileWriter(file,append));
        writer.write(content);
        writer.flush();
        writer.close();
        return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean writeCollection(String pathname,Collection<String> c,boolean append){
        if(c==null)return false;
        File file=new File(pathname);
        try {
            if(file.exists()){
                if(file.isDirectory())return false;
            }else {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            BufferedWriter writer=new BufferedWriter(new FileWriter(file,append));
            for (String line : c) {
                writer.write(line + "\n");
            }
            writer.flush();
            writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
