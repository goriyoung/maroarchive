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
package com.marolabs.number;

public abstract class MutableInteger<T extends MutableInteger<T>> extends MutableNumber<T> {
	public abstract T mod(T val);

	public abstract T shiftLeft(int bits);

	public abstract T shiftRight(int bits);

	public abstract T and(T val);

	public abstract T or(T val);

	public abstract T xor(T val);
}
