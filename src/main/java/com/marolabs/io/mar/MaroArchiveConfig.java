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

import com.marolabs.crypto.CryptoUtils;

public class MaroArchiveConfig {
	private boolean enable_dedpue = false;
	private boolean enable_remove = true;
	private boolean enable_cache = false;
	private boolean readonly = false;
	private boolean is_stream = false;
	private boolean alignment = false;

	private byte[] primary_password = null;
	private int encrypt_alg = 0;
	private boolean enable_compress = false;
	private int compress_alg = 0;

	public MaroArchiveConfig() {
	}

	public MaroArchiveConfig(MaroArchiveProfile profile) {
		// TODO
	}

	public void setAlignment(boolean enable) {
		alignment = enable;
	}

	public boolean isAlignment() {
		return alignment;
	}

	public void setPassword(String primaryPwd) {
		if (primaryPwd == null || primaryPwd.length() == 0) {
			return;
		}

		primary_password = CryptoUtils.fingerprint(primaryPwd);
	}

	public void enableStream(boolean enable) {
		is_stream = enable;
	}

	public boolean isStream() {
		return is_stream;
	}

	public byte[] getPasskey() {
		return primary_password;
	}

	public void setEncryptAlgrithm(int alg) {
		encrypt_alg = alg;
	}

	public int getEncryptAlgrithm() {
		return encrypt_alg;
	}

	public void setCompressAlgrithm(int alg) {
		compress_alg = alg;
	}

	public int getCompressAlgrithm() {
		return compress_alg;
	}

	public void setCompress(boolean enable) {
		enable_compress = enable;
	}

	public boolean getCompress() {
		return enable_compress;
	}

	public void setDedupe(boolean enable) {
		enable_dedpue = enable;

		if (enable) {
			enable_cache = true;
		}
	}

	public boolean getDedupe() {
		return enable_dedpue;
	}

	public void setCache(boolean enable) {
		if (enable_dedpue) {
			enable_cache = true;
		} else {
			enable_cache = enable;
		}
	}

	// dont use TODO
	public boolean getCache() {
		return enable_cache;
	}

	public void setRemove(boolean enable) {
		enable_remove = enable;
	}

	// TODO dont use
	public boolean getRemove() {
		return enable_remove;
	}

	// TODO dont use
	public void setReadOnly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isReadOnly() {
		return readonly;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MaroArchiveConfig[");
		sb.append("enable_dedpue =");
		sb.append(enable_dedpue);
		sb.append(',');

		sb.append("enable_remove =");
		sb.append(enable_remove);
		sb.append(',');

		sb.append("primary_password =");
		sb.append(primary_password == null ? null : "*******");
		sb.append(',');

		sb.append("encrypt_alg =");
		sb.append(encrypt_alg);
		sb.append(',');

		sb.append("enable_compress =");
		sb.append(enable_compress);
		sb.append(',');

		sb.append("compress_alg =");
		sb.append(compress_alg);
		sb.append(',');

		sb.append("enable_cache =");
		sb.append(enable_cache);

		sb.append("]");
		return sb.toString();
	}
}
