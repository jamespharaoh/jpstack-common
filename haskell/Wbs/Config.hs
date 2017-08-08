{-# LANGUAGE Arrows, NoMonomorphismRestriction #-}

module Wbs.Config where

import           Control.Monad (foldM)
import           Control.Monad.IO.Class (liftIO)

import           Data.String.Utils (replace)

import           Text.XML.HXT.Core

-------------------- records

data BuildPlugin =
	BuildPlugin {
		bplName :: String,
		bplPackage :: String
	}

data BuildGitLink =
	BuildGitLink {
		bglName :: String,
		bglSource :: String,
		bglLocal :: String,
		bglTarget :: String,
		bglPaths :: [ String ]
	}

data Build =
	Build {
		bldName :: String,
		bldPlugins :: [ BuildPlugin ],
		bldGitLinks :: [ BuildGitLink ]
	}

data Plugin =
	Plugin {
		plgName :: String,
		plgPackage :: String,
		plgSqlSchemas :: [ String ],
		plgSqlDatas :: [ String ]
	}

data Library =
	Library {
		libName :: String,
		libType :: String,
		libVersion :: String,
		libSource :: Bool
	}

data World =
	World {
		wldBuild :: Build,
		wldPlugins :: [ Plugin ],
		wldLibraries :: [ Library ]
	}

-------------------- misc

parseXML file = readDocument [] file
atTag tag = getChildren >>> isElem >>> hasName tag
optTag tag = atTag tag `orElse` mkelem "tag" [] []

boolFromStr :: String -> [Bool]
boolFromStr "yes" = [True]
boolFromStr "no" = [False]
boolFromStr "" = []
boolFromStr _ = error "Invalid bool"

attrStr = getAttrValue
attrBool x = attrStr x >>> arrL boolFromStr

getAttrArray elemName attrName =
	listA $ atTag elemName >>> proc parent -> do
		name <- getAttrValue attrName -< parent
		returnA -< name

-------------------- loadBuild

loadBuild :: IO (Build)
loadBuild = do

	let getPlugins = atTag "plugin" >>> proc pluginTag -> do

		name <- getAttrValue "name" -< pluginTag
		package <- getAttrValue "package" -< pluginTag

		returnA -< BuildPlugin {
			bplName = name,
			bplPackage = package
		}

	let getBuild = atTag "wbs-build" >>> proc buildTag -> do

		name <- getAttrValue "name" -< buildTag

		pluginsTag <- atTag "plugins" -< buildTag
		plugins <- listA getPlugins -< pluginsTag

		returnA -< Build {
			bldName = name,
			bldPlugins = plugins
		}

	[ build ] <-
		runX (parseXML "wbs-build.xml" >>> getBuild)

	return build

-------------------- loadPlugin

loadPlugin :: Build -> BuildPlugin -> IO Plugin
loadPlugin build buildPlugin = do

	let getPlugin = atTag "plugin" >>> proc pluginTag -> do

		name <- getAttrValue "name" -< pluginTag
		package <- getAttrValue "package" -< pluginTag

		sqlScriptsTag <- optTag "sql-scripts" -< pluginTag
		sqlSchemas <- getAttrArray "sql-schema" "name" -< sqlScriptsTag
		sqlDatas <- getAttrArray "sql-data" "name" -< sqlScriptsTag

		returnA -< Plugin {
			plgName = name,
			plgPackage = package,
			plgSqlSchemas = sqlSchemas,
			plgSqlDatas = sqlDatas
		}

	let pluginPath =
		"src/" ++
		(replace "." "/" $ bplPackage buildPlugin) ++
		"/" ++
		(bplName buildPlugin) ++
		"-plugin.xml"

	[ plugin ] <-
		runX (parseXML pluginPath >>> getPlugin)

	return plugin

-------------------- loadLibraries

loadLibraries :: IO [Library]
loadLibraries = do

	let getLibraries =
		atTag "libraries" >>>
		atTag "library" >>> proc libraryTag -> do

			name <- attrStr "name" -< libraryTag
			type_ <- attrStr "type" -< libraryTag
			version <- attrStr "version" -< libraryTag
			source <- attrBool "source" `withDefault` False -< libraryTag

			returnA -< Library {
				libName = name,
				libType = type_,
				libVersion = version,
				libSource = source
			}

	let librariesPath = "etc/libraries.xml"

	runX (parseXML librariesPath >>> getLibraries)

-------------------- loadWorld

loadWorld :: IO World
loadWorld = do

	build <- loadBuild

	plugins <-
		mapM (loadPlugin build) $
			bldPlugins build

	libraries <- loadLibraries

	return World {
		wldBuild = build,
		wldPlugins = plugins,
		wldLibraries = libraries
	}
