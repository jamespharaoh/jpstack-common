module Wbs.Setup.EclipseProject where

import Data.List
import Data.String.Utils (replace)

import Text.XML.HXT.Core

import Wbs.Config

writeProject :: World -> IO ()
writeProject world = do

	let build = wldBuild world

	let telem name value = mkelem name [] [ txt value ]

	let makeName = telem "name" $ bldName build
	let makeComment = telem "comment" ""
	let makeProjects = eelem "projects"

	let makeBuildSpec =

		selem "buildSpec" [

			selem "buildCommand" [
				telem "name" "org.eclipse.ui.externaltools.ExternalToolBuilder",
				telem "triggers" "clean,full,incremental",
				selem "arguments" [
					selem "dictionary" [
						telem "key" "LaunchConfigHandle",
						telem "value" "<project>/.externalToolBuilders/Code generator.launch"
					],
					selem "dictionary" [
						telem "key" "incclean",
						telem "value" "true"
					]
				]
			],

			selem "buildCommand" [
				telem "name" "org.eclipse.jdt.core.javabuilder",
				eelem "arguments"
			],

			selem "buildCommand" [
				telem "name" "com.stateofflow.eclipse.metrics.MetricsBuilder",
				eelem "arguments"
			]

		]

	let makeNatures =
		selem "natures" [
			telem "nature" "org.eclipse.jdt.core.javanature",
			telem "nature" "com.stateofflow.eclipse.metrics.MetricsNature"
		]

	let makeFilteredResources =
		selem "filteredResources" [
			selem "filter" [
				telem "id" "1378377069204",
				telem "name" "",
				selem "matcher" [
					telem "id" "org.eclipse.ui.ide.multiFilter",
					telem "arguments" "1.0-projectRelativePath-matches-false-false-bin"
				]
			]
		]

	let makeProject =
		root [] [
			mkelem "projectDescription" [] [
				makeName,
				makeComment,
				makeProjects,
				makeBuildSpec,
				makeNatures,
				makeFilteredResources
			]
		]

	let writeProject = writeDocument [withIndent yes] ".project"

	runX (makeProject >>> writeProject)

	return ()
