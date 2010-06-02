/*
 * Copyright 2009 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springcache

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.Signature
import org.junit.Test
import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.CoreMatchers.not
import static org.junit.Assert.assertThat

class CacheKeyTests {

	static final TARGET_1 = new Object()
	static final TARGET_2 = new Object()

	@Test
	void cacheKeysDifferForDifferentMethodNamesOnSameTarget() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method1"))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method2"))

		assertThat key1, not(equalTo(key2))
		assertThat key1.hashCode(), not(equalTo(key2.hashCode()))
	}

	@Test
	void cacheKeysDifferForSameMethodWithDifferentArguments() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", "b"]))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", "c"]))

		assertThat key1, not(equalTo(key2))
		assertThat key1.hashCode(), not(equalTo(key2.hashCode()))
	}

	@Test
	void cacheKeysDifferForSameMethodWithAndWithoutArguments() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method"))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["x"]))

		assertThat key1, not(equalTo(key2))
		assertThat key1.hashCode(), not(equalTo(key2.hashCode()))
	}

	@Test
	void cacheKeysDifferForSameMethodOnDifferentTargets() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method"))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_2, "method"))

		assertThat key1, not(equalTo(key2))
		assertThat key1.hashCode(), not(equalTo(key2.hashCode()))
	}

	@Test
	void cacheKeysEqualForRepeatedCallsToSameMethod() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method"))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method"))

		assertThat key1, equalTo(key2)
		assertThat key1.hashCode(), equalTo(key2.hashCode())
	}

	@Test
	void cacheKeysEqualForRepeatedCallsToSameMethodWithArguments() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", "b"]))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", "b"]))

		assertThat key1, equalTo(key2)
		assertThat key1.hashCode(), equalTo(key2.hashCode())
	}

	@Test
	void cacheKeysCanBeGeneratedForNullMethodArgumentsAndAreEqual() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", null]))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", null]))

		assertThat key1, equalTo(key2)
		assertThat key1.hashCode(), equalTo(key2.hashCode())
	}

	@Test
	void cacheKeysAreNotEqualForDifferentMethodArgumentsContainingNulls() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", null]))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["b", null]))

		assertThat key1, not(equalTo(key2))
		assertThat key1.hashCode(), not(equalTo(key2.hashCode()))
	}

	@Test
	void cacheKeysEqualForRepeatedCallsToSameMethodWithObjectArrayArguments() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", ["a"] as Object[]]))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", ["a"] as Object[]]))

		assertThat key1, equalTo(key2)
		assertThat key1.hashCode(), equalTo(key2.hashCode())
	}

	@Test
	void cacheKeysNotEqualForRepeatedCallsToSameMethodWithDifferentObjectArrayArguments() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", ["a"] as Object[]]))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", ["b"] as Object[]]))

		assertThat key1, not(equalTo(key2))
		assertThat key1.hashCode(), not(equalTo(key2.hashCode()))
	}

	@Test
	void cacheKeysEqualForRepeatedCallsToSameMethodWithArrayArgumentsContainingNull() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", [null] as Object[]]))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", [null] as Object[]]))

		assertThat key1, equalTo(key2)
		assertThat key1.hashCode(), equalTo(key2.hashCode())
	}

	@Test
	void cacheKeysEqualForRepeatedCallsToSameMethodWithPrimitiveArrayArguments() {
		def asserter = {
			def clone = it.clone()
			
			def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", [it]))
			def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", [clone]))

			assertThat key1, equalTo(key2)
			assertThat key1.hashCode(), equalTo(key2.hashCode())
		}
		
		asserter([1] as int[])
		asserter([true] as boolean[])
		asserter("abc" as char[])
	}

	static JoinPoint mockJoinPoint(Object target, String methodName, List args = []) {
		def joinPoint = [:]
		joinPoint.getTarget = {-> target }
		joinPoint.getSignature = {-> [getName: {-> methodName }] as Signature }
		joinPoint.getArgs = {-> args as Object[] }
		return joinPoint as JoinPoint
	}

}