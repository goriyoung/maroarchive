/**
 * 
 * Copyright 2020 Marolabs(TM) Co,Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.marolabs.io.mar.file;

import java.io.Closeable;
import java.io.IOException;

//
//import java.io.Closeable;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeMap;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import com.marolabs.Environment;
//import com.marolabs.exception.BadFormatException;
//import com.marolabs.exception.IllegalVolumeException;
//import com.marolabs.io.PathFilters;
//import com.marolabs.io.attr.EntryAttributes;
//import com.marolabs.io.fileview.FileBrowsView;
//import com.marolabs.io.fileview.FileNavigateBrowsView;
//import com.marolabs.io.mar.MaroArchive.ChunkOutputStream;
//import com.marolabs.io.source.BufferedDataSource;
//import com.marolabs.io.source.DataSource;
//import com.marolabs.io.source.VolumeFileDataSource;
//import com.marolabs.plugin.Plugin;
//import com.marolabs.plugin.PluginManager;
//import com.marolabs.plugin.Plugins;
//import com.marolabs.util.MCallback;
//import com.marolabs.util.Utils;
//import com.marolabs.util.pool.ArrayPool;
//
////structure
////chunk(0) entry index
////chunk(1) stats: file_count[4byte],Total Size[8byte]
////chunk(2~9) reserve
////chunk(10~25) plugin_uuid(8byte):view data(var)
////chunk(?>25) ID_ENTRY_ATTR : Data ChunkID : EntryAttributes
////chunk(?>25) ID_ENTRY_DATA : File_Datas(File_len byte)
public class MaroFileArchive implements Closeable {

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}
	//
	// public static final int MAX_VIEW_NUM = 16;
	// private static final int CHK_ID_INDEX = 0;
	// private static final int CHK_ID_STATS = 1;
	// private static final int CHK_ID_VIEW_START = 10;
	// private static final int CHK_ID_FILE_START = CHK_ID_VIEW_START
	// + MAX_VIEW_NUM;
	//
	// public static final int ID_VIEW_DIR = 0;
	// public static final int ID_VIEW_SIZE = 1;
	// public static final int ID_VIEW_TIME = 2;
	// public static final int ID_VIEW_TYPE = 3;
	//
	// private static final int ID_DEFAULT_VIEW = ID_VIEW_DIR;
	//
	// private static final byte ID_ENTRY_ATTR = 1;
	// private static final byte ID_ENTRY_DATA = ID_ENTRY_ATTR << 1;
	//
	// private final MaroArchive mca;
	// private final FileBrowsView[] views = new FileBrowsView[MAX_VIEW_NUM];
	// private final List<MaroFileArchiveEntry> entry_index = new LinkedList<>();
	//
	// private int file_count = 0;
	// private long total_size = 0;
	// private boolean isClosed = false;
	//
	// // private FileBrowsViewCreator view_creator;
	// private final MaroFileArchiveConfig config;
	// private static List<Class<FileBrowsView>> browsViewPlugin;
	//
	// static {
	// browsViewPlugin = PluginManager.findPlugin(FileBrowsView.class);
	//
	// // bcos this plugin is default and internal can't be replace
	// browsViewPlugin.remove(FileNavigateBrowsView.class);
	// }
	//
	// public MaroFileArchive(DataSource out, boolean forceCreate,
	// MaroFileArchiveConfig config) throws IOException {
	// if (out instanceof BufferedDataSource) {
	// mca = new MaroArchive(out, forceCreate, config);
	// } else {
	// mca = new MaroArchive((out), forceCreate, config);
	// }
	// this.config = config;
	//
	// if (!forceCreate) {
	// // TODO check the close state
	// // if(not close normally){regenerate the stats and view index}
	// boolean need_recover = false;
	// if (need_recover) {
	// recover_entry_index();
	// recover_entry_views();
	// recover_entry_stats();
	// } else {
	// load_entry_index();
	// load_entry_stats();
	// load_entry_views();
	// }
	// } else {
	// // make a placehold
	// for (int i = 0; i < CHK_ID_FILE_START; i++) {
	// mca.addPlaceHold();
	// }
	//
	// // just init the default view by defalut
	// views[ID_DEFAULT_VIEW] = FileNavigateBrowsView.instance();
	// }
	// }
	//
	// public static MaroFileArchive create(DataSource out,
	// MaroFileArchiveConfig config) {
	// return null;
	// }
	//
	// public static MaroFileArchive open(DataSource in) {
	// return null;
	// }
	//
	// private void recover_entry_index() {
	// }
	//
	// private void recover_entry_views() {
	// }
	//
	// private void recover_entry_stats() {
	// }
	//
	// private EntryAttributes getAttributes(Path path) throws IOException {
	// if (config.getAbsolute() && !path.isAbsolute()) {
	// path = path.toAbsolutePath();
	// }
	//
	// return new EntryAttributes(path);
	// }
	//
	// public int addFileEntry(Path path, boolean delete_source)
	// throws IOException {
	// if (null != path && Files.exists(path) && Files.isRegularFile(path)) {
	// EntryAttributes attr = getAttributes(path);
	//
	// try {
	// int entry_chunk_id = addEntry(Files.newInputStream(path), attr);
	//
	// if (delete_source) {
	// Files.deleteIfExists(path);
	// }
	// return entry_chunk_id;
	// } catch (IOException e) {
	// return -1;
	// }
	// } else {
	// return -1;
	// }
	// }
	//
	// public int addDirEntry(Path path, boolean delete_source)
	// throws IOException {
	// if (null != path && Files.exists(path) && Files.isDirectory(path)) {
	// EntryAttributes attr = getAttributes(path);
	//
	// try {
	// int entry_chunk_id = addEntry(null, attr);
	// if (delete_source) {
	// Files.deleteIfExists(path);
	// }
	//
	// return entry_chunk_id;
	// } catch (IOException e) {
	// return -1;
	// }
	// } else {
	// return -1;
	// }
	// }
	//
	// public int addEntry(InputStream is, EntryAttributes attr)
	// throws IOException {
	// System.out.printf("Add %s Entry [%s] to Maro File Archive(MFA)",
	// attr.isDirectory() ? "Dir" : "File", attr);
	//
	// if (null == attr || (!attr.isDirectory() && null == is)) {
	// System.out.println(" Failed");
	// return -1;
	// }
	//
	// int data_chunk_id = -1;
	//
	// // if the data is a file write data to chunk
	// if (!attr.isDirectory()) {
	// ChunkOutputStream cos = null;
	// final int TEMP_SIZE = 1 << 12;
	// byte[] temp = ArrayPool.alloc_byte(TEMP_SIZE);
	// try {
	// cos = mca.addChunkByStream();
	//
	// cos.write(ID_ENTRY_DATA);
	// // write the file data
	// int len = 0;
	// while ((len = is.read(temp, 0, TEMP_SIZE)) > 0) {
	// cos.write(temp, 0, len);
	// }
	// } finally {
	// ArrayPool.free(temp);
	// if (null != cos) {
	// try {
	// cos.close();
	// } catch (Exception e) {
	// System.err.println(e);
	// }
	// }
	//
	// if (null != is) {
	// try {
	// is.close();
	// } catch (Exception e) {
	// System.err.println(e);
	// }
	// }
	// }
	//
	// data_chunk_id = cos.getChunkID();
	// }
	//
	// // check if the data is write success
	// if (attr.isDirectory() || data_chunk_id >= CHK_ID_FILE_START) {
	// // write the entry attributes
	// DataOutputStream dos = null;
	// ChunkOutputStream cos = null;
	// try {
	// cos = mca.addChunkByStream();
	// dos = new DataOutputStream(cos);
	// dos.write(ID_ENTRY_ATTR);
	// dos.writeInt(data_chunk_id);
	// attr.writeTo(dos);
	// } finally {
	// if (null != dos) {
	// try {
	// dos.close();
	// } catch (Exception e) {
	// System.err.println(e);
	// }
	// }
	// }
	//
	// int attr_chunk_id = cos.getChunkID();
	// // update the view
	// for (int i = 0; i < MAX_VIEW_NUM; i++) {
	// if (views[i] == null) {
	// continue;
	// }
	//
	// views[i].addEntry(attr, attr_chunk_id);
	// }
	//
	// // update stats
	// file_count++;
	// total_size += attr.size();
	//
	// return attr_chunk_id;
	// }
	//
	// return -1;
	// }
	//
	// public int addEntries(Path path, final boolean delete_src)
	// throws IOException {
	// System.out.printf("Add Files in [%s] to Maro File Archive(MFA) ", path);
	//
	// if (null == path && !Files.exists(path)) {
	// return 0;
	// }
	//
	// if (Files.isRegularFile(path)) {
	// if (addFileEntry(path, delete_src) > 0) {
	// return 1;
	// } else {
	// return 0;
	// }
	// }
	// final AtomicInteger count = new AtomicInteger();
	// Utils.vist_file_tree(path, PathFilters.FILTER_LEAF, null,
	// new MCallback<Path, Void>() {
	//
	// @Override
	// public Void call(Path param) {
	// if (param.endsWith(".DS_Store")) {
	// return null;
	// }
	//
	// if (Files.isDirectory(param)) {
	//
	// try {
	// if (addDirEntry(param, delete_src) > 0) {
	// count.incrementAndGet();
	// }
	// } catch (IOException e) {
	// System.out.println("Failed " + e);
	// }
	// } else {
	// try {
	// if (addFileEntry(param, delete_src) > 0) {
	// count.incrementAndGet();
	// }
	// } catch (IOException e) {
	// System.out.println("Failed " + e);
	// }
	// }
	//
	// return null;
	// }
	// });
	//
	// return count.get();
	// }
	//
	// // TODO
	// public void remove(MaroFileArchiveEntry entry) {
	// }
	//
	// // TODO remove
	// public void removeEntryAt(int index) {
	// if (!config.getRemove()) {
	// return;
	// }
	//
	// if (index >= file_count) {
	// throw new IndexOutOfBoundsException(
	// "Remove file @" + index + " >= " + file_count);
	// }
	//
	// EntryAttributes attr = this.getEntryAttr(index);
	// long file_size = attr.size();
	// if (file_size < 0) {
	// return;
	// }
	// int chunk_id = geChunkID(index);
	// if (chunk_id < 0) {
	// System.out.println("Remove file failed, cant locate id@" + index);
	// }
	//
	// mca.removeChunk(chunk_id);
	//
	// for (int i = 0; i < MAX_VIEW_NUM; i++) {
	// if (views[i] == null) {
	// continue;
	// }
	//
	// views[i].removeEntry(attr);
	// ;
	// }
	//
	// file_count--;
	// total_size -= file_size;
	// }
	//
	// private boolean isEntryChunk(int chunkID) {
	// if (chunkID < CHK_ID_FILE_START) {
	// return false;
	// }
	//
	// return entry_index.containsKey(chunkID);
	// }
	//
	// private int geChunkID(int entry_index) throws IOException {
	// int chunk_count = mca.getChunkCount();
	//
	// int valid_count = 0;
	// for (int i = CHK_ID_FILE_START; i < chunk_count; i++) {
	// if (mca.isValid(i) && isEntryChunk(i)) {
	// valid_count++;
	//
	// if (entry_index == valid_count - 1) {
	// return i;
	// }
	// }
	// }
	//
	// return -1;
	// }
	//
	// public Enumeration<MaroFileArchiveEntry> enumEntries() {
	// return Collections.enumeration(entry_index);
	// }
	//
	// public MaroFileArchiveEntry getEntryAt(int index) {
	// if (index >= file_count || index < 0) {
	// throw new IndexOutOfBoundsException(
	// "dont have this entry index @" + index);
	// }
	//
	// return entry_index.get(index);
	// }
	//
	// public InputStream getEntryAsStream(int index) {
	// int chunk_id = geChunkID(index);
	//
	// return getEntryAsStreamByChunkID(chunk_id);
	// }
	//
	// private InputStream getEntryAsStreamByChunkID(int entry_chunk_id) {
	//
	// if (entry_chunk_id < CHK_ID_FILE_START) {
	// return null;
	// }
	//
	// DataInputStream is = null;
	// try {
	// // load attr first and
	// is = new DataInputStream(mca.getChunkByStream(entry_chunk_id));
	//
	// // skip the entry attr
	// new EntryAttributes().readObject(is);
	// } catch (IOException e) {
	// return null;
	// }
	//
	// return is;
	// }
	//
	// public EntryAttributes getEntryAttr(int index) {
	// int chunk_id = geChunkID(index);
	//
	// if (chunk_id < CHK_ID_FILE_START) {
	// return null;
	// }
	//
	// return getEntryAttrByteChunkID(chunk_id);
	// }
	//
	// public EntryAttributes getEntryAttrByteChunkID(int chunk_id) {
	// if (chunk_id < CHK_ID_FILE_START) {
	// return null;
	// }
	//
	// DataInputStream is = null;
	// try {
	// is = new DataInputStream(mca.getChunkByStream(chunk_id));
	// EntryAttributes attr = new EntryAttributes();
	// attr.readObject(is);
	// System.out.println(attr);
	// return attr;
	// } catch (Exception e) {
	// System.out.println("get file attr failed cid@ " + chunk_id);
	// } finally {
	// if (null != is) {
	// try {
	// if (null != is) {
	// is.close();
	// }
	// } catch (IOException e) {
	// }
	// }
	// }
	//
	// return null;
	// }
	//
	// public void exact(Path dir) {
	// int chunk_count = mca.getChunkCount();
	// for (int i = CHK_ID_FILE_START; i < chunk_count; i++) {
	// if (!mca.isValid(i)) {
	// continue;
	// }
	//
	// DataInputStream is = null;
	//
	// try {
	// is = new DataInputStream(mca.getChunkByStream(i));
	// // is.skipBytes(OFF_ID_DIR);
	// // boolean isDir = is.readBoolean();
	// // is.skipBytes(SIZ_FILE_LEN);
	//
	// EntryAttributes attr = new EntryAttributes();
	// attr.readObject(is);
	// String name = attr.path().toString().replace(":", "");
	// File chunkF = new File(dir.toFile(), name);
	//
	// System.out.println("Exact : " + chunkF);
	// if (chunkF.exists())
	// chunkF.delete();
	//
	// if (!chunkF.getParentFile().exists()) {
	// chunkF.getParentFile().mkdirs();
	// }
	//
	// if (attr.isDirectory()) {
	// chunkF.mkdir();
	// continue;
	// }
	//
	// try {
	// chunkF.createNewFile();
	// } catch (IOException e1) {
	// System.out.println("exact file failed " + name);
	// continue;
	// }
	//
	// FileOutputStream os = null;
	// try {
	// os = new FileOutputStream(chunkF);
	// } catch (FileNotFoundException e1) {
	// }
	//
	// final int TEMP_SIZE = 1 << 12;
	// byte[] temp = ArrayPool.alloc_byte(TEMP_SIZE);
	// try {
	// int len = 0;
	// while ((len = is.read(temp, 0, TEMP_SIZE)) > 0) {
	// os.write(temp, 0, len);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// if (null != os)
	// os.close();
	// } catch (IOException e) {
	//
	// }
	// try {
	// if (null != is)
	// is.close();
	// } catch (IOException e) {
	// }
	//
	// ArrayPool.free(temp);
	// }
	//
	// } catch (Exception e1) {
	// System.out.println(i);
	// e1.printStackTrace();
	// } finally {
	// try {
	// if (null != is) {
	// is.close();
	// }
	// } catch (IOException e) {
	// }
	// }
	//
	// }
	// }
	//
	// public int fileCount() {
	// return file_count;
	// }
	//
	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	//
	// sb.append("MFA[");
	// sb.append("file count = " + file_count);
	// sb.append(", total size = " + this.total_size);
	// for (int i = 0; i < file_count; i++) {
	// sb.append(", ");
	// sb.append(this.getEntryAttr(i));
	// sb.append("\n");
	// }
	// sb.append("]");
	// return sb.toString();
	// }
	//
	// private void load_entry_index() throws IOException {
	// DataInputStream is = null;
	// try {
	// is = new DataInputStream(mca.getChunkByStream(CHK_ID_INDEX));
	// int entry_count = is.readInt();
	//
	// for (int i = 0; i < entry_count; i++) {
	// int attr_chunk_id = is.readInt();
	// int data_chunk_id = is.readInt();
	//
	// entry_index.add(new MaroFileArchiveEntry(this, attr_chunk_id,
	// data_chunk_id));
	//
	// }
	//
	// System.out.println("load stats file_count = " + file_count
	// + " total_size=" + total_size);
	// } finally {
	// if (null != is) {
	// try {
	// is.close();
	// } catch (IOException e) {
	// }
	// }
	// }
	// }
	//
	// private void write_entry_index() {
	// DataOutputStream dos = null;
	// try {
	// dos = new DataOutputStream(mca.modifyChunkByStream(CHK_ID_INDEX));
	// // write entry count, make read back more easy
	// dos.writeInt(entry_index.size());
	//
	// for (MaroFileArchiveEntry entry : entry_index) {
	// dos.writeInt(entry.getAttrChunkID());
	//
	// // write the data_chunk_id
	// dos.writeInt(entry.getDataChunkID());
	// }
	//
	// System.out
	// .println("write entry index count = " + entry_index.size());
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// if (null != dos) {
	// try {
	// dos.close();
	// } catch (IOException e) {
	// }
	// }
	// }
	// }
	//
	// private void load_entry_stats() throws IOException {
	// DataInputStream is = null;
	// try {
	// is = new DataInputStream(mca.getChunkByStream(CHK_ID_STATS));
	// this.file_count = is.readInt();
	// this.total_size = is.readLong();
	//
	// System.out.println("load stats file_count = " + file_count
	// + " total_size=" + total_size);
	// } finally {
	// if (null != is) {
	// try {
	// is.close();
	// } catch (IOException e) {
	// }
	// }
	// }
	// }
	//
	// private void write_entry_stats() {
	// DataOutputStream dos = null;
	// try {
	// dos = new DataOutputStream(mca.modifyChunkByStream(CHK_ID_STATS));
	// dos.writeInt(file_count);
	// dos.writeLong(total_size);
	//
	// System.out.println("write stats file_count = " + file_count
	// + " total_size=" + total_size);
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// if (null != dos) {
	// try {
	// dos.close();
	// } catch (IOException e) {
	// }
	// }
	// }
	// }
	//
	// private void load_entry_views() {
	// for (int i = CHK_ID_VIEW_START; i < CHK_ID_VIEW_START
	// + MAX_VIEW_NUM; i++) {
	// int chunk_id = i;
	// int view_id = chunk_id - CHK_ID_VIEW_START;
	// DataInputStream is = null;
	//
	// try {
	// is = new DataInputStream(mca.getChunkByStream(chunk_id));
	//
	// long uuid = is.readLong();
	// if (uuid == 0) {
	// views[view_id] = null;
	// // System.out.println("load view @" + view_id + " not
	// // exsit");
	// } else {
	// Plugin plugin = PluginManager.getPlugin(uuid);
	//
	// if (null == plugin || !(plugin instanceof FileBrowsView)) {
	// System.err.println("View Store Damaged or not found.");
	// } else {
	// views[view_id] = (FileBrowsView) plugin;
	// System.out
	// .println("load view @" + view_id + " success");
	// }
	// }
	// } catch (IOException e) {
	// System.err.println("load view @" + view_id + " failed");
	// e.printStackTrace();
	// } finally {
	// if (null != is) {
	// try {
	// is.close();
	// } catch (IOException e) {
	// }
	// }
	// }
	// }
	// }
	//
	// private void write_entry_views() {
	// // write view to the first chunk
	// for (int i = CHK_ID_VIEW_START; i < CHK_ID_VIEW_START
	// + MAX_VIEW_NUM; i++) {
	// int chunk_id = i;
	// int view_id = chunk_id - CHK_ID_VIEW_START;
	//
	// DataOutputStream dos = null;
	// try {
	// dos = new DataOutputStream(mca.modifyChunkByStream(chunk_id));
	// if (views[view_id] != null) {
	// dos.writeLong(
	// Plugins.getPluginUUID(views[view_id].getClass()));
	// views[view_id].writeTo(dos);
	// System.out.println("write view @" + view_id);
	// } else {
	// dos.writeLong(0);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// if (null != dos) {
	// try {
	// dos.close();
	// } catch (IOException e) {
	// }
	// }
	// }
	//
	// }
	// }
	//
	// @Override
	// public void close() {
	// if (!isClosed) {
	//
	// write_entry_index();
	// write_entry_stats();
	// write_entry_views();
	//
	// mca.close();
	// isClosed = true;
	// }
	//
	// System.out.println(Arrays.asList(views));
	// }
	//
	// public static void main(String[] args)
	// throws IOException, BadFormatException, IllegalVolumeException {
	// Path r = Paths.get("/Volumes/Ramdisk");
	// Path l = Paths.get("/Volumes/Ramdisk/test/exact");
	// System.out.println(r.relativize(l));
	// System.out.println(l.relativize(r));
	//
	// boolean exact = true;
	// boolean create = true;
	//
	// boolean verify = false;
	// boolean print = false;
	//
	// final Path root = Paths.get("/Volumes/Ramdisk");
	// Path path_mfa = root.resolve("mfa").resolve("1.mfa");
	//
	// MaroFileArchiveConfig config = new MaroFileArchiveConfig();
	// config.setCompress(false);
	// // config.setPassword("marolabs");
	// MaroFileArchive mfa = null;
	//
	// Path path_src = root.resolve("test");
	// if (create) {
	// mfa = new MaroFileArchive(
	// VolumeFileDataSource.create(path_mfa, 1 << 24), true,
	// config);
	// mfa.addEntries(path_src, false);
	// mfa.close();
	// }
	//
	// final Path path_exact = root.resolve("exact");
	// if (exact) {
	// mfa = new MaroFileArchive(VolumeFileDataSource.open(path_mfa),
	// false, config);
	// mfa.exact(path_exact);
	// mfa.close();
	// }
	// if (print) {
	// mfa = new MaroFileArchive(VolumeFileDataSource.open(path_mfa),
	// false, config);
	// System.out.println(mfa);
	// mfa.close();
	// }
	// if (verify) {
	// Utils.vist_file_tree(path_src, null, null,
	// new MCallback<Path, Void>() {
	//
	// @Override
	// public Void call(Path param) {
	// // System.out.print(count.getAndIncrement()+"\r");
	// Path src_abs = param.toAbsolutePath();
	//
	// Path exact_abs = path_exact.resolve(
	// src_abs.subpath(0, src_abs.getNameCount()));
	//
	// if (!Files.exists(exact_abs)) {
	// System.out.println("miss file : " + exact_abs);
	// return null;
	// }
	//
	// if ((Files.isDirectory(src_abs) != Files
	// .isDirectory(exact_abs))) {
	// System.out.println("not dir : " + exact_abs);
	// return null;
	// }
	//
	// if ((Files.isRegularFile(src_abs) != Files
	// .isRegularFile(exact_abs))) {
	// System.out.println("not file : " + exact_abs);
	// return null;
	// }
	//
	// try {
	// if (Files.isRegularFile(src_abs)) {
	// if (Files.size(exact_abs) != Files
	// .size(src_abs)) {
	// System.out.println(
	// "not same size : " + exact_abs);
	// return null;
	// }
	//
	// InputStream src_is = Files
	// .newInputStream(src_abs);
	// InputStream exc_is = Files
	// .newInputStream(exact_abs);
	//
	// int c = -1;
	// while (((c = src_is.read()) >= 0)) {
	// int c1 = exc_is.read();
	//
	// if (c1 != c) {
	// System.out.println("File damaged : "
	// + exact_abs);
	// return null;
	// }
	// }
	//
	// src_is.close();
	// exc_is.close();
	// }
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// return null;
	// }
	// });
	// }
	// Environment.exec_cached.shutdown();
	// }
	//
}
