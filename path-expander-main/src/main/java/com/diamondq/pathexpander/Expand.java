package com.diamondq.pathexpander;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

@Command(name = "expand", mixinStandardHelpOptions = true, subcommandsRepeatable = true, version = "Expand 1.0", description = "Expands the build path produced by dependency:resolve")
public class Expand implements Runnable
{
	@SuppressWarnings("null")
	@Option(names = { "--resolvePath" }, required = true, description = "Output file from dependency:resolve")
	public String resolvePath;

	@ArgGroup(multiplicity = "0..*", exclusive = false)
	public @Nullable List<Project> projects;

	static class Project
	{
		@SuppressWarnings("null")
		@Option(names = { "--projectPath" }, required = true, description = "Root folder of the project")
		public String projectPath;

		@SuppressWarnings("null")
		@Option(names = { "--groupId" }, required = true, description = "Maven Group Id of the project")
		public String groupId;

		@SuppressWarnings("null")
		@Option(names = { "--name" }, required = true, description = "Name of the project for property resolving")
		public String name;
	}

	@SuppressWarnings("null")
	public List<ArtifactInfo> artifactList;

	// A reference to this method can be used as a custom execution strategy
	// that first calls the init() method,
	// and then delegates to the default execution strategy.
	@SuppressWarnings("deprecation")
	private int executionStrategy(ParseResult parseResult)
	{
		init(); // custom initialization to be done before executing any command or subcommand
		return new CommandLine.RunLast().execute(parseResult); // default execution strategy
	}

	private void init()
	{
		try
		{
			artifactList = parse();
		}
		catch (final IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static void main(@NonNull String[] args) throws Throwable
	{
		final Expand expand = new Expand();
		final CommandLine commandLine = new CommandLine(expand)
				.setExecutionStrategy(expand::executionStrategy);

		for (final ExpandAction action : ServiceLoader.load(ExpandAction.class))
		{
			commandLine.addSubcommand(action);
		}
		commandLine.execute(args);
	}

	@Override
	public void run()
	{
		for (final ArtifactInfo ai : artifactList)
		{
			System.out.println(ai.toString());
		}
	}

	public static class ProjectInfo
	{
		public final String name;
		public final Map<String, File> folderMap;

		protected ProjectInfo(String pName, Map<String, File> pFolderMap)
		{
			super();
			name = pName;
			folderMap = pFolderMap;
		}
	}

	private List<ArtifactInfo> parse() throws IOException
	{
		/* Scan the project root for all folders */

		final Map<String, ProjectInfo> map = new HashMap<>();
		final List<Project> localProjects = projects;
		if ((localProjects != null) && (localProjects.isEmpty() == false))
		{
			for (final Project project : localProjects)
			{
				final Path projectRootPath = Paths.get(project.projectPath);
				Files.walkFileTree(projectRootPath, new FileVisitor<Path>()
				{

					/**
					 * @see java.nio.file.FileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
					 */
					@Override
					public FileVisitResult preVisitDirectory(Path pDir, BasicFileAttributes pAttrs) throws IOException
					{
						final Path relative = projectRootPath.relativize(pDir);
						final File canonFile = relative.toFile();
						final String fileName = canonFile.getName();
						if (fileName.equals(".svn") == true)
						{
							return FileVisitResult.SKIP_SUBTREE;
						}
						ProjectInfo projectInfo = map.get(project.groupId);
						if (projectInfo == null)
						{
							projectInfo = new ProjectInfo(project.name, new HashMap<>());
							map.put(project.groupId, projectInfo);
						}
						projectInfo.folderMap.merge(fileName, canonFile, (e, n) -> e);
						return FileVisitResult.CONTINUE;
					}

					/**
					 * @see java.nio.file.FileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
					 */
					@Override
					public FileVisitResult visitFile(Path pFile, BasicFileAttributes pAttrs) throws IOException
					{
						return FileVisitResult.CONTINUE;
					}

					/**
					 * @see java.nio.file.FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
					 */
					@Override
					public FileVisitResult visitFileFailed(Path pFile, IOException pExc) throws IOException
					{
						throw pExc;
					}

					/**
					 * @see java.nio.file.FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
					 */
					@Override
					public FileVisitResult postVisitDirectory(Path pDir, @Nullable IOException pExc) throws IOException
					{
						if (pExc != null)
						{
							throw pExc;
						}
						return FileVisitResult.CONTINUE;
					}
				});
			}
		}

		final File file = new File(resolvePath);
		if (file.exists() == false)
		{
			throw new IllegalArgumentException("Unable to find the file " + file.getAbsolutePath());
		}

		/* Read the file */

		final List<ArtifactInfo> results = new ArrayList<>();
		try (FileReader fr = new FileReader(file))
		{
			try (BufferedReader br = new BufferedReader(fr))
			{
				String line;
				String repoPath = null;
				while ((line = br.readLine()) != null)
				{
					line = line.trim();
					final int o1 = line.indexOf(':');
					if (o1 == -1)
					{
						continue;
					}
					final int o2 = line.indexOf(':', o1 + 1);
					if (o2 == -1)
					{
						continue;
					}
					final int o3 = line.indexOf(':', o2 + 1);
					if (o3 == -1)
					{
						continue;
					}
					final int o4 = line.indexOf(':', o3 + 1);
					if (o4 == -1)
					{
						continue;
					}
					final int o5 = line.indexOf(':', o4 + 1);
					if (o5 == -1)
					{
						continue;
					}
					final int o6 = line.indexOf(':', o5 + 1);
					boolean withClassifier;
					if (o6 != -1)
					{
						final String testScope = line.substring(o5 + 1, o6);
						if (("compile".equals(testScope) == true)
								|| ("provided".equals(testScope) == true)
								|| ("runtime".equals(testScope) == true)
								|| ("test".equals(testScope) == true)
								|| ("system".equals(testScope) == true)
								|| ("import".equals(testScope) == true))
						{
							withClassifier = true;
						}
						else
						{
							withClassifier = false;
						}
					}
					else
					{
						withClassifier = false;
					}
					@SuppressWarnings("null")
					final @NonNull String[] parts = new String[7];
					parts[0] = line.substring(0, o1);
					parts[1] = line.substring(o1 + 1, o2);
					parts[2] = line.substring(o2 + 1, o3);
					if (withClassifier == true)
					{
						parts[3] = line.substring(o3 + 1, o4);
						parts[4] = line.substring(o4 + 1, o5);
						parts[5] = line.substring(o5 + 1, o6);
						parts[6] = line.substring(o6 + 1);
					}
					else
					{
						parts[4] = line.substring(o3 + 1, o4);
						parts[5] = line.substring(o4 + 1, o5);
						parts[6] = line.substring(o5 + 1);
					}
					if (parts[6].endsWith(" (optional)") == true)
					{
						parts[6] = parts[6].substring(0, parts[6].length() - " (optional)".length());
					}
					if (repoPath == null)
					{
						/* Determine the repo path */

						final String[] groupParts = parts[0].split("\\.");
						final StringBuilder sb = new StringBuilder();
						sb.append(File.separator);
						for (final String s : groupParts)
						{
							sb.append(s).append(File.separator);
						}
						sb.append(parts[1]).append(File.separator);
						final String fixedPath = sb.toString();
						final int offset = parts[6].indexOf(fixedPath);
						if (offset == -1)
						{
							throw new IllegalArgumentException("Unable to find the known path \"" + fixedPath
									+ "\" within the full filename path of \"" + parts[6] + "\"");
						}
						repoPath = parts[6].substring(0, offset);
					}

					if (parts[6].startsWith(repoPath) == false)
					{
						throw new IllegalArgumentException(
								"The file name path \"" + parts[6] + "\" does not start with the repo path \"" + repoPath + "\"");
					}
					String relativePath = parts[6].substring(repoPath.length());

					final ProjectInfo projectInfo = map.get(parts[0]);
					String projectStr = null;
					String projectName = null;
					if (projectInfo != null)
					{
						final File projectFile = projectInfo.folderMap.get(parts[1]);
						if (projectFile != null)
						{
							projectStr = projectFile.toString();
							projectName = projectInfo.name;
						}
					}

					if (File.separator.equals("/") == false)
					{
						parts[6] = parts[6].replace(File.separator, "/");
						relativePath = relativePath.replace(File.separator, "/");
						if (projectStr != null)
						{
							projectStr = projectStr.replace(File.separator, "/");
						}
					}
					results.add(new ArtifactInfo(parts[0], parts[1], parts[5], parts[4], parts[2], parts[3], parts[6], relativePath,
							projectStr, projectName));
				}
			}
		}

		return results;
	}
}
