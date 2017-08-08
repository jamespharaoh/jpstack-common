-- TODO:
-- - verify plugin dependencies
-- - establish and use correct plugin order
-- - output eclipse configs
-- - output web configs

module Main where

import Text.XML.HXT.Core

import qualified Wbs.Config as Config

import qualified Wbs.Setup.AntProject as AntProject

import qualified Wbs.Setup.EclipseClasspath as EclipseClasspath
import qualified Wbs.Setup.EclipseFactorypath as EclipseFactorypath
import qualified Wbs.Setup.EclipseProject as EclipseProject

main :: IO ()
main = do

	world <- Config.loadWorld

	AntProject.writeBuildFile world

	EclipseClasspath.writeClasspath world
	EclipseFactorypath.writeFactorypath world
	EclipseProject.writeProject world

	return ()
