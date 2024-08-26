package com.tobiasmaneschijn.mcjsmod.javascript.filesystem;

import com.google.gson.Gson;
import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IFileSystem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class VirtualFileSystem implements IFileSystem {
    private VirtualFile root;
    private static final Gson GSON = new Gson();

    public VirtualFileSystem() {
        this.root = new VirtualFile("", true);
    }

    private String normalizePath(String path) {
        // Replace backslashes with forward slashes for consistency
        path = path.replace("\\", "/");

        // Handle current directory
        path = path.replaceAll("^\\./", "").replaceAll("/\\./", "/");

        // Remove trailing slash
        if (path.endsWith("/") && !path.equals("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // Handle empty path or just "."
        if (path.isEmpty() || path.equals(".")) {
            return "/";
        }

        // Ensure the path starts with a slash
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // Remove any double slashes
        path = path.replaceAll("//+", "/");

        return path;
    }

    @Override
    public void createFile(String path, String content) throws FileSystemException {
        path = normalizePath(path);
        MCJSMod.LOGGER.info("Creating file: " + path);
        String[] parts = path.split("/");
        VirtualFile parent = navigateToParent(parts);
        String fileName = parts[parts.length - 1];
        if (parent.children.containsKey(fileName)) {
            throw new FileSystemException("File already exists: " + path);
        }

        parent.children.put(fileName, new VirtualFile(fileName, false, content));
    }

    @Override
    public void createDirectory(String path) throws FileSystemException {
        path = normalizePath(path);
        MCJSMod.LOGGER.info("Creating directory: " + path);
        String[] parts = path.split("/");
        VirtualFile parent = navigateToParent(parts);
        String dirName = parts[parts.length - 1];
        if (parent.children.containsKey(dirName)) {
            throw new FileSystemException("Directory already exists: " + path);
        }
        parent.children.put(dirName, new VirtualFile(dirName, true));
    }

    @Override
    public String readFile(String path) throws FileSystemException {
        path = normalizePath(path);
        MCJSMod.LOGGER.info("Reading file: " + path);
        VirtualFile file = getFile(path);
        if (file.isDirectory) {
            throw new FileSystemException("Cannot read directory: " + path);
        }
        return file.content;
    }

    @Override
    public void writeFile(String path, String content) throws FileSystemException {
        path = normalizePath(path);
        MCJSMod.LOGGER.info("Writing file: " + path);
        createParentDirectories(path);
        String[] parts = path.split("/");
        VirtualFile parent = navigateToParent(parts);
        String fileName = parts[parts.length - 1];
        if (parent.children.containsKey(fileName)) {
            if (parent.children.get(fileName).isDirectory) {
                throw new FileSystemException("Cannot write to a directory: " + path);
            }
            parent.children.get(fileName).content = content;
        } else {
            parent.children.put(fileName, new VirtualFile(fileName, false, content));
        }
    }

    private void createParentDirectories(String path) throws FileSystemException {
        String[] parts = normalizePath(path).split("/");
        VirtualFile current = root;
        for (int i = 1; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.children.containsKey(part)) {
                MCJSMod.LOGGER.info("Creating directory: " + part);
                current.children.put(part, new VirtualFile(part, true));
            }
            current = current.children.get(part);
            if (!current.isDirectory) {
                throw new FileSystemException("Path component is not a directory: " + part);
            }
        }
    }

    @Override
    public void deleteFile(String path) throws FileSystemException {
        path = normalizePath(path);
        String[] parts = path.split("/");
        VirtualFile parent = navigateToParent(parts);
        String fileName = parts[parts.length - 1];
        if (!parent.children.containsKey(fileName)) {
            throw new FileSystemException("File not found: " + path);
        }
        VirtualFile file = parent.children.get(fileName);
        if (file.isDirectory) {
            throw new FileSystemException("Cannot delete directory using deleteFile: " + path);
        }
        parent.children.remove(fileName);
    }

    @Override
    public void deleteDirectory(String path) throws FileSystemException {
        path = normalizePath(path);
        String[] parts = path.split("/");
        VirtualFile parent = navigateToParent(parts);
        String dirName = parts[parts.length - 1];
        if (!parent.children.containsKey(dirName)) {
            throw new FileSystemException("Directory not found: " + path);
        }
        VirtualFile dir = parent.children.get(dirName);
        if (!dir.isDirectory) {
            throw new FileSystemException("Not a directory: " + path);
        }
        if (!dir.children.isEmpty()) {
            throw new FileSystemException("Directory not empty: " + path);
        }
        parent.children.remove(dirName);
    }

    @Override
    public List<String> listFiles(String path) throws FileSystemException {
        path = normalizePath(path);
        VirtualFile dir = getFile(path);
        if (!dir.isDirectory) {
            throw new FileSystemException("Not a directory: " + path);
        }
        return new ArrayList<>(dir.children.keySet());
    }

    @Override
    public boolean exists(String path) {
        path = normalizePath(path);
        boolean exists = false;
        try {
            getFile(path);
            exists = true;
        } catch (FileSystemException e) {
            // File doesn't exist
        }
        MCJSMod.LOGGER.info("Checking if exists: " + path + " - " + exists);
        return exists;
    }

    @Override
    public boolean isDirectory(String path) {
        path = normalizePath(path);
        try {
            return getFile(path).isDirectory;
        } catch (FileSystemException e) {
            return false;
        }
    }

    @Override
    public void move(String sourcePath, String destinationPath) throws FileSystemException {
        sourcePath = normalizePath(sourcePath);
        destinationPath = normalizePath(destinationPath);
        VirtualFile sourceFile = getFile(sourcePath);
        String[] destParts = destinationPath.split("/");
        VirtualFile destParent = navigateToParent(destParts);
        String destName = destParts[destParts.length - 1];

        if (destParent.children.containsKey(destName)) {
            throw new FileSystemException("Destination already exists: " + destinationPath);
        }

        String[] sourceParts = sourcePath.split("/");
        VirtualFile sourceParent = navigateToParent(sourceParts);
        String sourceName = sourceParts[sourceParts.length - 1];

        sourceParent.children.remove(sourceName);
        destParent.children.put(destName, sourceFile);
        sourceFile.name = destName;
    }

    @Override
    public void copy(String sourcePath, String destinationPath) throws FileSystemException {
        sourcePath = normalizePath(sourcePath);
        destinationPath = normalizePath(destinationPath);
        VirtualFile sourceFile = getFile(sourcePath);
        String[] destParts = destinationPath.split("/");
        VirtualFile destParent = navigateToParent(destParts);
        String destName = destParts[destParts.length - 1];

        if (destParent.children.containsKey(destName)) {
            throw new FileSystemException("Destination already exists: " + destinationPath);
        }

        VirtualFile newFile = sourceFile.clone();
        newFile.name = destName;
        destParent.children.put(destName, newFile);
    }

    private VirtualFile getFile(String path) throws FileSystemException {
        path = normalizePath(path);
        if (path.equals("/")) {
            return root;
        }
        String[] parts = path.split("/");
        VirtualFile current = root;
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (!current.children.containsKey(part)) {
                throw new FileSystemException("Path not found: " + path);
            }
            current = current.children.get(part);
        }
        return current;
    }

    private VirtualFile navigateToParent(String[] parts) throws FileSystemException {
        VirtualFile current = root;
        for (int i = 1; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.children.containsKey(part)) {
                throw new FileSystemException("Path not found: " + String.join("/", parts));
            }
            current = current.children.get(part);
            if (!current.isDirectory) {
                throw new FileSystemException("Not a directory: " + String.join("/", parts));
            }
        }
        return current;
    }

    public void  save(CompoundTag tag) {
        tag.putString("fileSystem", GSON.toJson(root));
    }

    public void load(CompoundTag tag) {
        if (tag.contains("fileSystem", Tag.TAG_STRING)) {
            String json = tag.getString("fileSystem");
            this.root = GSON.fromJson(json, VirtualFile.class);
        }
    }

    private static class VirtualFile {
        String name;
        boolean isDirectory;
        String content;
        Map<String, VirtualFile> children;

        VirtualFile(String name, boolean isDirectory) {
            this(name, isDirectory, "");
        }

        VirtualFile(String name, boolean isDirectory, String content) {
            this.name = name;
            this.isDirectory = isDirectory;
            this.content = content;
            this.children = isDirectory ? new HashMap<>() : null;
        }

        public VirtualFile clone() {
            VirtualFile clone = new VirtualFile(this.name, this.isDirectory, this.content);
            if (this.isDirectory) {
                for (Map.Entry<String, VirtualFile> entry : this.children.entrySet()) {
                    clone.children.put(entry.getKey(), entry.getValue().clone());
                }
            }
            return clone;
        }
    }
}