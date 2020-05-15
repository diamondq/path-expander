package com.diamondq.pathexpander;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ArtifactInfo
{
	public final String groupId;
	public final String artifactId;
	public final String scope;
	public final String version;
	public final String type;
	public final @Nullable String classifier;
	public final String filePath;

	public final String repoRelativePath;
	public final @Nullable String projectRelativePath;
	public final @Nullable String projectName;

	protected ArtifactInfo(String pGroupId, String pArtifactId, String pScope, String pVersion, String pType, @Nullable String pClassifier,
			String pFilePath, String pRepoRelativePath, @Nullable String pProjectRelativePath, @Nullable String pProjectName)
	{
		super();
		groupId = pGroupId;
		artifactId = pArtifactId;
		scope = pScope;
		type = pType;
		version = pVersion;
		classifier = pClassifier;
		filePath = pFilePath;
		repoRelativePath = pRepoRelativePath;
		projectRelativePath = pProjectRelativePath;
		projectName = pProjectName;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("ArtifactInfo(");
		sb.append("groupId=").append(groupId);
		sb.append(", artifactId=").append(artifactId);
		sb.append(", scope=").append(scope);
		sb.append(", version=").append(version);
		sb.append(", type=").append(type);
		sb.append(", classifier=").append(classifier);
		sb.append(", filePath=").append(filePath);
		sb.append(", repoRelativePath=").append(repoRelativePath);
		sb.append(", projectRelativePath=").append(projectRelativePath);
		sb.append(", projectName=").append(projectName);
		sb.append(")");
		return sb.toString();
	}
}
