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

//
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.channels.Channels;
//import java.nio.channels.ReadableByteChannel;
//import java.nio.channels.SeekableByteChannel;
//import java.nio.channels.WritableByteChannel;
//
//import com.marolabs.io.attr.EntryAttributes;
//
public class MaroFileArchiveEntry {
	// private int attr_chunk_id = -1;
	// private int data_chunk_id = -1;
	// MaroFileArchive source = null;
	//
	// public MaroFileArchiveEntry(MaroFileArchive src, int attr_chunk_id,
	// int data_chunk_id) {
	// this.attr_chunk_id = attr_chunk_id;
	// this.data_chunk_id = data_chunk_id;
	// this.source = src;
	// }
	//
	// public int getAttrChunkID() {
	// return attr_chunk_id;
	// }
	//
	// public int getDataChunkID() {
	// return data_chunk_id;
	// }
	//
	// public MaroFileArchive getSource() {
	// return source;
	// }
	//
	// @Override
	// public int hashCode() {
	// return attr_chunk_id;
	// }
	//
	// @Override
	// public boolean equals(Object obj) {
	// if (obj == null) {
	// return false;
	// }
	// if (!(obj instanceof MaroFileArchiveEntry)) {
	// return false;
	// }
	//
	// return attr_chunk_id == ((MaroFileArchiveEntry) obj).attr_chunk_id;
	// }
	//
	// //TODO
	// public EntryAttributes getAttributes()
	// {
	// return null;
	//
	// }
	//
	// //TODO
	// public void setAttributes(EntryAttributes newAttr)
	// {
	//
	// }
	//
	// public InputStream openReadStream() {
	// return Channels.newInputStream(openReadChannel());
	// }
	//
	// public OutputStream openWriteStream() {
	// return Channels.newOutputStream(openWriteChannel());
	// }
	//
	// public ReadableByteChannel openReadChannel() {
	// throw new UnsupportedOperationException();
	// // TODO
	// }
	//
	// public WritableByteChannel openWriteChannel() {
	// throw new UnsupportedOperationException();
	// // TODO
	// }
	//
	// public SeekableByteChannel openRandomAccessChannel() {
	// throw new UnsupportedOperationException();
	// // TODO
	// }
}
