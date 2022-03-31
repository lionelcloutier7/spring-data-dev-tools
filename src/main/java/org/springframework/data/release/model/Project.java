/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.release.model;

import java.util.Arrays;
import java.util.List;

import lombok.Value;

/**
 * @author Oliver Gierke
 */
@Value
public class Project {

	private final ProjectKey key;
	private final String name;
	private final List<Project> dependencies;

	public Project(String key, String name, Project... dependencies) {

		this.key = new ProjectKey(key);
		this.name = name;
		this.dependencies = Arrays.asList(dependencies);
	}

	public String getFullName() {
		return "Spring Data ".concat(name);
	}
}
