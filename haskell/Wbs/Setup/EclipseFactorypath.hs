module Wbs.Setup.EclipseFactorypath where

import Data.List
import Data.String.Utils (replace)

import Text.XML.HXT.Core

import Wbs.Config

writeFactorypath :: World -> IO ()
writeFactorypath world = do

	let build = wldBuild world

	let makeEntry kind id =
		mkelem "factorypathentry" [
			sattr "kind" kind,
			sattr "id" id,
			sattr "enabled" "true",
			sattr "runInBatchMode" "false"
		] []

	let makeAnnotationsEntry =
		makeEntry "PLUGIN" "org.eclipse.jst.ws.annotations.core"

	let makeFactorypath =
		root [] [
			mkelem "factorypath" [] [
				makeAnnotationsEntry
			]
		]

	let writeFactorypath =
		writeDocument [withIndent yes] ".factorypath"

	runX (makeFactorypath >>> writeFactorypath)

	return ()
