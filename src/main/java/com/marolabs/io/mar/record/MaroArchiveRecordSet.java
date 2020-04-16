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
package com.marolabs.io.mar.record;

import com.marolabs.data.record.NumberedRecordSet;
import com.marolabs.data.record.RecordNumber;
import com.marolabs.data.record.RecordSetException;
import com.marolabs.data.record.StoredRecordSet;
import com.marolabs.data.record.Txn;
import com.marolabs.data.record.TxnConfig;
import com.marolabs.io.CloseableIterator;
import com.marolabs.io.mar.MaroArchive;
import com.marolabs.io.serial.Serializer;

public class MaroArchiveRecordSet<V> implements NumberedRecordSet<V>, StoredRecordSet<RecordNumber, V> {
	private MaroArchive mar;
	private Serializer serializer;

	@Override
	public boolean exist(Txn txn, RecordNumber key) {
		return mar.exists(key.intValue());
	}

	@Override
	public V get(Txn txn, RecordNumber key) {
		return null;
		// return mar.getEntry(entryID);
	}

	@Override
	public void put(Txn txn, RecordNumber key, V value) {
		// TODO Auto-generated method stub

	}

	@Override
	public int size(Txn txn) {
		return 0;
	}

	@Override
	public void remove(Txn txn, RecordNumber key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear(Txn txn) {

	}

	@Override
	public void flush() {
	}

	@Override
	public void truncate(Txn txn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		if (null != mar) {
			mar.close();
		}
	}

	@Override
	public CloseableIterator<RecordNumber> keyIterator(Txn txn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CloseableIterator<Record<RecordNumber, V>> iterator(Txn txn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecordNumber append(Txn txn, V val) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Txn beginTransaction(Txn parentTxn, TxnConfig config) throws RecordSetException {
		// TODO Auto-generated method stub
		return null;
	}
}
