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
package org.springframework.data.release.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.release.io.CommandResult;
import org.springframework.data.release.io.OsCommandOperations;
import org.springframework.data.release.io.Workspace;
import org.springframework.data.release.model.ArtifactVersion;
import org.springframework.data.release.model.Iteration;
import org.springframework.data.release.model.Module;
import org.springframework.data.release.model.ModuleIteration;
import org.springframework.data.release.model.Project;
import org.springframework.data.release.model.Train;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Component to execut Git related operations.
 * 
 * @author Oliver Gierke
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GitOperations {

	private static final Logger LOGGER = HandlerUtils.getLogger(GitOperations.class);

	private final GitServer server = new GitServer();
	private final OsCommandOperations osCommandOperations;
	private final Workspace workspace;

	public GitProject getGitProject(Project project) {
		return new GitProject(project, server);
	}

	/**
	 * Checks out all projects of the given {@link Train} at the tags for the given {@link Iteration}.
	 * 
	 * @param train
	 * @param iteration
	 * @throws Exception
	 */
	public void checkout(Train train, Iteration iteration) throws Exception {

		update(train);

		for (ModuleIteration module : train.getModuleIterations(iteration)) {

			Project project = module.getProject();
			ArtifactVersion artifactVersion = ArtifactVersion.from(module);

			Tag tag = findTagFor(project, artifactVersion);

			if (tag == null) {
				throw new IllegalStateException(String.format("No tag found for version %s of project %s, aborting.",
						artifactVersion, project));
			}

			osCommandOperations.executeCommand(String.format("git checkout %s", tag), project).get();
		}

		LOGGER.info(String.format("Successfully checked out iteration %s for release train %s.", iteration.getName(),
				train.getName()));
	}

	public void prepare(Train train, Iteration iteration) throws Exception {

		for (ModuleIteration module : train.getModuleIterations(iteration)) {

			Branch branch = Branch.from(module);

			update(module.getProject());

			String checkoutCommand = String.format("git checkout %s", branch);
			osCommandOperations.executeCommand(checkoutCommand, module.getProject()).get();

			String updateCommand = String.format("git pull origin %s", branch);
			osCommandOperations.executeCommand(updateCommand, module.getProject()).get();
		}
	}

	public void update(Train train) throws Exception {

		List<Future<CommandResult>> executions = new ArrayList<>();

		for (Module module : train) {
			executions.add(update(module.getProject()));
		}

		for (Future<CommandResult> execution : executions) {
			execution.get();
		}
	}

	public Future<CommandResult> update(Project project) throws Exception {

		GitProject gitProject = new GitProject(project, server);
		String repositoryName = gitProject.getRepositoryName();

		if (workspace.hasProjectDirectory(project)) {

			LOGGER.info(String.format("Found existing repository %s. Obtaining latest changes…", repositoryName));

			return osCommandOperations.executeCommand("git checkout master && git fetch --tags && git pull origin master",
					project);

		} else {

			LOGGER.info(String.format("No repository found for project %s. Cloning repository from %s…", repositoryName,
					gitProject.getProjectUri()));

			File projectDirectory = workspace.getProjectDirectory(project);
			String command = String.format("git clone %s %s", gitProject.getProjectUri(), projectDirectory.getName());

			return osCommandOperations.executeCommand(command);
		}
	}

	public Tags getTags(Project project) throws Exception {

		String result = osCommandOperations.executeForResult("git tag -l", project);
		List<Tag> tags = new ArrayList<>();

		for (String line : result.split("\n")) {

			if (!StringUtils.isEmpty(line)) {
				tags.add(new Tag(line));
			}
		}

		return new Tags(tags);
	}

	/**
	 * Returns the {@link Tag} that represents the {@link ArtifactVersion} of the given {@link Project}.
	 * 
	 * @param project
	 * @param version
	 * @return
	 * @throws IOException
	 */
	private Tag findTagFor(Project project, ArtifactVersion version) throws Exception {

		for (Tag tag : getTags(project)) {

			if (tag.toArtifactVersion().equals(version)) {
				return tag;
			}
		}

		return null;
	}
}
