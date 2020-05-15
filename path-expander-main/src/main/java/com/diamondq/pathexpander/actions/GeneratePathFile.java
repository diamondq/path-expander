package com.diamondq.pathexpander.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import com.diamondq.pathexpander.ArtifactInfo;
import com.diamondq.pathexpander.ExpandAction;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "pathFile", description = "Generates a Dev path that can be used from within Eclipse")
public class GeneratePathFile extends AbstractPathBuilder implements ExpandAction
{
	@Option(names = { "--format" }, description = "The format to write. Options = ${COMPLETION-CANDIDATES}")
	public FormatTypes format = FormatTypes.CLASSPATH;

	@Option(names = { "--dev" }, description = "Whether to use the dev paths")
	public boolean withDev = false;

	public GeneratePathFile()
	{
	}

	/**
	 * @see com.diamondq.pathexpander.actions.AbstractPathBuilder#beforeArtifact(java.io.BufferedWriter,
	 *      com.diamondq.pathexpander.ArtifactInfo, boolean)
	 */
	@Override
	protected boolean beforeArtifact(BufferedWriter pBufferedWriter, ArtifactInfo pInfo, boolean pIsFirst) throws IOException
	{
		if (pIsFirst == false)
		{
			switch (format)
			{
				case CLASSPATH:
				{
					pBufferedWriter.append(File.pathSeparator);
					break;
				}
				case TEXT:
				{
					pBufferedWriter.newLine();
					break;
				}
				default:
					throw new IllegalStateException();

			}
		}
		return true;
	}

	/**
	 * @see com.diamondq.pathexpander.actions.AbstractPathBuilder#onArtifact(java.io.BufferedWriter, com.diamondq.pathexpander.ArtifactInfo,
	 *      boolean)
	 */
	@Override
	protected void onArtifact(BufferedWriter pBufferedWriter, ArtifactInfo pInfo, boolean pIsFirst) throws IOException
	{
		if ((withDev == true) && (pInfo.projectRelativePath != null))
		{
			onProject(pBufferedWriter, pInfo, pIsFirst);
		}
		else
		{
			pBufferedWriter.append("${env.M2_HOME}");
			pBufferedWriter.append(pInfo.repoRelativePath);
		}
	}

}
