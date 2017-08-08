module Main where

import qualified Language.Java.Syntax as JSyntax
import qualified Language.Java.Parser as JParser
import qualified Wbs.Config as Config

main :: IO ()
main = do

	world <- Config.loadWorld

	source <- readFile "src/wbs/platform/user/model/UserRec.java"

	let javaTemp = JParser.parser JParser.compilationUnit source

	let java = case javaTemp of
		Left err -> error $ show err
		Right java -> java

	let JSyntax.CompilationUnit packageTemp imports types = java
	let Just (JSyntax.PackageDecl packageName) = packageTemp

	putStrLn $ "package: " ++ (show packageName)

	return ()
