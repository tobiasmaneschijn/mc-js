package com.tobiasmaneschijn.mcjsmod.javascript.interfaces;

import com.tobiasmaneschijn.mcjsmod.javascript.filesystem.FileSystemException;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

public interface IFileSystem {
    void createFile(String path, String content) throws FileSystemException;
    void createDirectory(String path) throws FileSystemException;
    String readFile(String path) throws FileSystemException;
    void writeFile(String path, String content) throws FileSystemException;
    void deleteFile(String path) throws FileSystemException;
    void deleteDirectory(String path) throws FileSystemException;
    List<String> listFiles(String path) throws FileSystemException;
    boolean exists(String path);
    boolean isDirectory(String path);
    void move(String sourcePath, String destinationPath) throws FileSystemException;
    void copy(String sourcePath, String destinationPath) throws FileSystemException;
    void save(CompoundTag tag);
    void load(CompoundTag tag);
}
