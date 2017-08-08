module Wbs.Setup.EclipseClasspath where

import Data.List
import Data.String.Utils (replace)

import Text.XML.HXT.Core

import Wbs.Config

writeClasspath :: World -> IO ()
writeClasspath world = do

	let build = wldBuild world
	let libraries = wldLibraries world

	let makeOriginalSrcEntry =
		mkelem "classpathentry" [
			sattr "kind" "src",
			sattr "path" "src"
		] []

	let makeGeneratedSrcEntry =
		mkelem "classpathentry" [
			sattr "kind" "src",
			sattr "path" "work/generated"
		] [
			mkelem "attributes" [] [
				mkelem "attribute" [
					sattr "name" "ignore_optional_problems",
					sattr "value" "true"
				] []
			]
		]

	let makeConEntry =
		mkelem "classpathentry" [
			sattr "kind" "con",
			sattr "path" "org.eclipse.jdt.launching.JRE_CONTAINER"
		] []

	let makeLibEntry library =

		mkelem "classpathentry" [
			sattr "kind" "lib",
			sattr "path" libPath,
			if libSource library
				then sattr "sourcepath" sourcePath
				else zeroArrow
		] []

		where

			libPath = concat [
				"binaries/libraries/",
				libName library,
				"-",
				libType library,
				"-",
				libVersion library,
				".jar" ]

			sourcePath = concat [
				"/root/projects/wbs-platform/wbs-binaries/libraries/",
				libName library,
				"-source-",
				libVersion library,
				".jar" ]

	let makeEclipseGeneratedSrcEntry =
		mkelem "classpathentry" [
			sattr "kind" "src",
			sattr "path" "work/eclipse/generated"
		] [
			mkelem "attributes" [] [
				mkelem "attribute" [
					sattr "name" "optional",
					sattr "value" "true"
				] []
			]
		]

	let makeOutputEntry =
		mkelem "classpathentry" [
			sattr "kind" "output",
			sattr "path" "work/eclipse/bin"
		] []

	let makeClasspath =
		root [] [
			mkelem "classpath" [] (
				[ makeOriginalSrcEntry ] ++
				[ makeGeneratedSrcEntry ] ++
				[ makeConEntry ] ++
				map makeLibEntry libraries ++
				[ makeEclipseGeneratedSrcEntry ] ++
				[ makeOutputEntry ]
			)
		]

	let writeClasspath =
		writeDocument [withIndent yes] ".classpath"

	runX (makeClasspath >>> writeClasspath)

	return ()
