package com.diamondq.pathexpander.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.diamondq.pathexpander.ArtifactInfo;
import com.diamondq.pathexpander.Expand;

import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

public abstract class AbstractPathBuilder implements Runnable
{

	@ParentCommand
	public Expand parent;

	@Option(names = { "--outputFile" }, description = "The location to store the output. If not provided then the stdout is used")
	public @Nullable String outputPath;

	@SuppressWarnings("null")
	public AbstractPathBuilder()
	{
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			if (outputPath == null)
			{
				try (Writer w = new OutputStreamWriter(System.out))
				{
					try (BufferedWriter bw = new BufferedWriter(w))
					{
						execute(bw);
					}
				}
			}
			else
			{
				final File file = new File(outputPath);
				if (file.getParentFile().exists() == false)
				{
					file.getParentFile().mkdirs();
				}

				try (FileWriter fw = new FileWriter(file))
				{
					try (BufferedWriter bw = new BufferedWriter(fw))
					{
						execute(bw);
					}
				}
			}
		}
		catch (final IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private void execute(BufferedWriter bw) throws IOException
	{
		before(bw);
		boolean isFirst = true;
		for (final ArtifactInfo info : parent.artifactList)
		{
			if (beforeArtifact(bw, info, isFirst) == false)
			{
				continue;
			}
			onArtifact(bw, info, isFirst);
			afterArtifact(bw, info, isFirst);
			if (isFirst == true)
			{
				isFirst = false;
			}
		}
		after(bw);
	}

	protected boolean beforeArtifact(BufferedWriter pBufferedWriter, ArtifactInfo pInfo, boolean pIsFirst) throws IOException
	{
		return true;
	}

	protected void onArtifact(BufferedWriter pBufferedWriter, ArtifactInfo pInfo, boolean pIsFirst) throws IOException
	{

	}

	protected void afterArtifact(BufferedWriter pBufferedWriter, ArtifactInfo pInfo, boolean pIsFirst) throws IOException
	{

	}

	protected void before(BufferedWriter pBufferedWriter) throws IOException
	{

	}

	protected void after(BufferedWriter pBufferedWriter) throws IOException
	{

	}

	protected void onProject(BufferedWriter pBufferedWriter, ArtifactInfo pInfo, boolean pIsFirst) throws IOException
	{
		pBufferedWriter.append("${").append(pInfo.projectName).append("}");
		final String path = Objects.requireNonNull(pInfo.projectRelativePath);
		if (path.startsWith("/") == false)
		{
			pBufferedWriter.append("/");
		}
		pBufferedWriter.append(path).append("/").append("target").append("/").append("classes");
	}
}
