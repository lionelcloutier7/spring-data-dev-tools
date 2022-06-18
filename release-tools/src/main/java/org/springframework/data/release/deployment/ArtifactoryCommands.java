/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.data.release.deployment;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.data.release.CliComponent;
import org.springframework.data.release.TimedCommand;
import org.springframework.shell.core.annotation.CliCommand;

/**
 * Commands to interact with Artifactory.
 * 
 * @author Oliver Gierke
 */
@CliComponent
@RequiredArgsConstructor
class ArtifactoryCommands extends TimedCommand {

	private final @NonNull DeploymentOperations deployment;

	@CliCommand(value = "artifactory verify", help = "Verifies authentication at Artifactory.")
	public void verify() {
		deployment.verifyAuthentication();
	}
}
