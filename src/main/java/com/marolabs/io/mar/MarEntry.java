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
package com.marolabs.io.mar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.marolabs.io.mar.file.attr.EntryAttributes;

public class MarEntry {
	private final MaroArchive archive;
	private MarEntryConfig entry_config;
	private int entryID;
	private BlockedChunk chunk;

	MarEntry(MaroArchive maroArchive, int entryID) {
		archive = maroArchive;
		this.entryID = entryID;

		entry_config = new MarEntryConfig();
		entry_config.setEntryType(MarEntryType.INDEXED_BLOCK);
		entry_config.setCompress(archive.config.getCompress());
		entry_config.setPassKey(archive.config.getPasskey());

		chunk = archive.data_access.getChunk(entryID);
	}

	MarEntry(MaroArchive maroArchive, int entryID, MarEntryConfig config) {
		archive = maroArchive;
		this.entryID = entryID;

		this.entry_config = config;
	}

	public void setConfig(MarEntryConfig config) {
		if (config != null) {
			entry_config = config;
		}
	}

	public MarEntryConfig getConfig() {
		return entry_config;
	}

	public int getID() {
		return entryID;
	}

	public long size() {
		return chunk.data_len_out;
	}

	public void setAttributes(EntryAttributes attrs) {
		try {
			archive.data_access.setAttrs(chunk, attrs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public EntryAttributes getAttributes() {
		try {
			return archive.data_access.getAttrs(chunk);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public InputStream openReadStream() {
		return Channels.newInputStream(openReadChannel());
	}

	public OutputStream openWriteStream() {
		return Channels.newOutputStream(openWriteChannel());
	}

	public ReadableByteChannel openReadChannel() {
		if (entry_config.getEntryType() == MarEntryType.INDEXED_BLOCK) {
			return new BlockedChunkReadChannel(archive, entryID, entry_config.getPassKey());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public WritableByteChannel openWriteChannel() {
		if (entry_config.getEntryType() == MarEntryType.INDEXED_BLOCK) {
			return new BlockedChunkWriteChannel(archive, entryID, entry_config.isCompress(), entry_config.getPassKey());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public SeekableByteChannel openRandomAccessChannel() {
		throw new UnsupportedOperationException();
		// TODO
	}
}