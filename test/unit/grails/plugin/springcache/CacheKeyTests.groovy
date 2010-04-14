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

	@Test void cacheKeysDifferForDifferentMethodNamesOnSameTarget() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method1"))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method2"))

		assertThat key1, not(equalTo(key2))
		assertThat key1.hashCode(), not(equalTo(key2.hashCode()))
	}

	@Test void cacheKeysDifferForSameMethodWithDifferentArguments() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", "b"]))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", "c"]))

		assertThat key1, not(equalTo(key2))
		assertThat key1.hashCode(), not(equalTo(key2.hashCode()))
	}

	@Test void cacheKeysDifferForSameMethodWithAndWithoutArguments() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method"))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["x"]))

		assertThat key1, not(equalTo(key2))
		assertThat key1.hashCode(), not(equalTo(key2.hashCode()))
	}

	@Test void cacheKeysDifferForSameMethodOnDifferentTargets() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method"))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_2, "method"))

		assertThat key1, not(equalTo(key2))
		assertThat key1.hashCode(), not(equalTo(key2.hashCode()))
	}

//	void "Cache keys are distinguished by the name and arguments of the invoked method"() {
//		when: "cache keys are generated"
//		def key1 = CacheKey.generate(joinPoint1)
//		def key2 = CacheKey.generate(joinPoint2)
//
//		then: "the keys should not be equal for different method name and argument combinations"
//		key1 != key2
//
//		and: "the keys' hashCodes should not be equal"
//		key1.hashCode() != key2.hashCode()
//
//		where:
//		joinPoint1 << [mockJoinPoint(TARGET_1, "method1"), mockJoinPoint(TARGET_1, "method", ["a"]), mockJoinPoint(TARGET_1, "method", ["a", "b"]), mockJoinPoint(TARGET_1, "method"), mockJoinPoint(TARGET_1, "method")]
//		joinPoint2 << [mockJoinPoint(TARGET_1, "method2"), mockJoinPoint(TARGET_1, "method", ["b"]), mockJoinPoint(TARGET_1, "method", ["a", "c"]), mockJoinPoint(TARGET_1, "method", ["x"]), mockJoinPoint(TARGET_2, "method")]
//	}

	@Test void cacheKeysEqualForRepeatedCallsToSameMethod() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method"))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method"))

		assertThat key1, equalTo(key2)
		assertThat key1.hashCode(), equalTo(key2.hashCode())
	}

	@Test void cacheKeysEqualForRepeatedCallsToSameMethodWithArguments() {
		def key1 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", "b"]))
		def key2 = CacheKey.generate(mockJoinPoint(TARGET_1, "method", ["a", "b"]))

		assertThat key1, equalTo(key2)
		assertThat key1.hashCode(), equalTo(key2.hashCode())
	}

//	void "Cache keys are consistent for repeated method calls"() {
//		when: "cache keys are generated"
//		def key1 = CacheKey.generate(joinPoint1)
//		def key2 = CacheKey.generate(joinPoint2)
//
//		then: "the keys should be equal for multiple method calls with the same arguments"
//		key1 == key2
//
//		and: "the keys' hashCodes should be equal"
//		key1.hashCode() == key2.hashCode()
//
//		where:
//		joinPoint1 << [mockJoinPoint(TARGET_1, "method"), mockJoinPoint(TARGET_1, "method", ["a", "b"])]
//		joinPoint2 << [mockJoinPoint(TARGET_1, "method"), mockJoinPoint(TARGET_1, "method", ["a", "b"])]
//	}

	static JoinPoint mockJoinPoint(Object target, String methodName, List args = []) {
		def joinPoint = [:]
		joinPoint.getTarget = {-> target }
		joinPoint.getSignature = {-> [getName: {-> methodName }] as Signature }
		joinPoint.getArgs = {-> args as Object[] }
		return joinPoint as JoinPoint
	}

}